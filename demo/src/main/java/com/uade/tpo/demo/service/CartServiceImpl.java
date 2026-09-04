package com.uade.tpo.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
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

    public Order getCart(User user) {
        return getOrCreateCart(user);
    }

    public Order addItem(User user, CartItemRequest request)
            throws ProductNotFoundException, InsufficientStockException {
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

        return getOrCreateCart(user);
    }

    public Order updateItem(User user, Long itemId, Integer quantity)
            throws OrderNotFoundException, InsufficientStockException {
        Order cart = getOrCreateCart(user);
        OrderItem item = findItemInCart(cart, itemId);

        if (quantity <= 0) {
            orderItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity)
                throw new InsufficientStockException();
            item.setQuantity(quantity);
            orderItemRepository.save(item);
        }

        return getOrCreateCart(user);
    }

    public Order removeItem(User user, Long itemId) throws OrderNotFoundException {
        Order cart = getOrCreateCart(user);
        OrderItem item = findItemInCart(cart, itemId);
        orderItemRepository.delete(item);
        return getOrCreateCart(user);
    }

    public Order checkout(User user) throws OrderNotFoundException, InsufficientStockException {
        Order cart = getOrCreateCart(user);

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
                    return orderRepository.save(newCart);
                });
    }

    private OrderItem findItemInCart(Order cart, Long itemId) throws OrderNotFoundException {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);
    }
}