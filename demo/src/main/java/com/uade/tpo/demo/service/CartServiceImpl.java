package com.uade.tpo.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.CartItem;
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
import com.uade.tpo.demo.repository.CartItemRepository;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public Cart getCart(User user) {
        return refresh(getOrCreateCart(user));
    }

    @Transactional(rollbackFor = Exception.class)
    public Cart addItem(User user, CartItemRequest request)
            throws ProductNotFoundException, InsufficientStockException, InvalidQuantityException {

        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new InvalidQuantityException();

        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        // Si el producto ya estaba en el carrito, se suma a lo que habia
        int currentQuantity = item != null ? item.getQuantity() : 0;
        int newQuantity = currentQuantity + request.getQuantity();

        if (product.getStock() < newQuantity)
            throw new InsufficientStockException();

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return refresh(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public Cart updateItem(User user, Long itemId, Integer quantity)
            throws OrderNotFoundException, InsufficientStockException {

        Cart cart = refresh(getOrCreateCart(user));
        CartItem item = findItemInCart(cart, itemId);

        if (quantity == null || quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity)
                throw new InsufficientStockException();
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return refresh(cart);
    }

    @Transactional(rollbackFor = Exception.class)
    public Cart removeItem(User user, Long itemId) throws OrderNotFoundException {
        Cart cart = refresh(getOrCreateCart(user));
        CartItem item = findItemInCart(cart, itemId);
        cartItemRepository.delete(item);
        return refresh(cart);
    }

    @Transactional
    public Cart clear(User user) {
        Cart cart = refresh(getOrCreateCart(user));
        cartItemRepository.deleteAll(cart.getItems());
        cart.setItems(new ArrayList<>());
        return cart;
    }

    /**
     * Confirma la compra. Es transaccional: o se completa toda la operacion
     * o no se aplica ninguna parte.
     *
     * rollbackFor = Exception.class es necesario porque Spring, por defecto,
     * solo revierte ante RuntimeException, y nuestras excepciones de negocio
     * son chequeadas.
     */
    @Transactional(rollbackFor = Exception.class)
    public Order checkout(User user)
            throws EmptyCartException, InsufficientStockException, ProductNotFoundException {

        Cart cart = refresh(getOrCreateCart(user));

        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new EmptyCartException();

        // Se valida el stock de TODO antes de tocar nada, para fallar
        // antes de empezar a descontar.
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getStock() < item.getQuantity())
                throw new InsufficientStockException();
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.COMPLETED);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        for (CartItem item : cart.getItems()) {
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItemRepository.save(orderItem);
        }

        // El carrito queda vacio y listo para una nueva compra
        cartItemRepository.deleteAll(cart.getItems());

        order.setItems(orderItemRepository.findByOrderId(order.getId()));
        return order;
    }

    // ---------- helpers ----------

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    // Relee los items desde la base. Sin esto, Hibernate devuelve la
    // coleccion cacheada y un item recien borrado sigue apareciendo.
    private Cart refresh(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        cart.setItems(items);
        return cart;
    }

    private CartItem findItemInCart(Cart cart, Long itemId) throws OrderNotFoundException {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(OrderNotFoundException::new);
    }
}