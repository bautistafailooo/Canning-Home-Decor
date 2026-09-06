package com.uade.tpo.demo.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.InvalidQuantityException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public Order getCart(User user) {
        return refresh(getOrCreateCart(user));
    }

    @Transactional(rollbackFor = Exception.class)
    public Order addItem(User user, CartItemRequest request)
            throws ProductNotFoundException, InsufficientStockException, InvalidQuantityException {

        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new InvalidQuantityException();

        Order cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        OrderItem item = orderItemRepository.findByOrderIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        int currentQuantity = item != null ? item.getQuantity() : 0;
        int newQuantity = currentQuantity + request.getQuantity();

        if (product.getStock() < newQuantity)
            throw new InsufficientStockException();

        if (item == null) {
            item = new OrderItem();
            item.setOrder(cart);
            item.setProduct(product);
        }
        item.setQuantity(newQuantity);
        orderItemRepository.save(item);

        return refresh(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order updateItem(User user, Long itemId, Integer quantity)
            throws OrderNotFoundException, InsufficientStockException {
        Order cart = refresh(getOrCreateCart(user));
        OrderItem item = findItemInCart(cart, itemId);

        if (quantity == null || quantity <= 0) {
            orderItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity)
                throw new InsufficientStockException();
            item.setQuantity(quantity);
            orderItemRepository.save(item);
        }

        return refresh(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order removeItem(User user, Long itemId) throws OrderNotFoundException {
        Order cart = refresh(getOrCreateCart(user));
        OrderItem item = findItemInCart(cart, itemId);
        orderItemRepository.delete(item);
        return refresh(cart);
    }

    // rollbackFor = Exception.class es CLAVE: por defecto Spring solo hace
    // rollback ante RuntimeException. InsufficientStockException es checked,
    // asi que sin esto un checkout que falla a mitad de camino dejaria stock
    // descontado de los productos ya procesados.
    @Transactional(rollbackFor = Exception.class)
    public Order checkout(User user)
            throws OrderNotFoundException, InsufficientStockException, EmptyCartException {
        Order cart = refresh(getOrCreateCart(user));

        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new EmptyCartException();

        try {
            for (OrderItem item : cart.getItems()) {
                productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
            }
        } catch (ProductNotFoundException e) {
            throw new OrderNotFoundException();
        }

        cart.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(cart);
    }

    private Order getOrCreateCart(User user) {
        return orderRepository.findByUserIdAndStatus(user.getId(), OrderStatus.CART)
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(user);
                    newCart.setStatus(OrderStatus.CART);
                    newCart.setItems(new ArrayList<>());
                    return orderRepository.save(newCart);
                });
    }

    // Relee los items desde la base. Sin esto, Hibernate devuelve la coleccion
    // que ya tenia cacheada y un item recien borrado sigue apareciendo.
    private Order refresh(Order cart) {
        cart.setItems(orderItemRepository.findByOrderId(cart.getId()));
        return cart;
    }

    private OrderItem findItemInCart(Order cart, Long itemId) throws OrderNotFoundException {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);
    }
}