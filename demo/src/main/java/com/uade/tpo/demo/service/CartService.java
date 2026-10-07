package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.InvalidQuantityException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;

public interface CartService {

    Cart getCart(User user);

    Cart addItem(User user, CartItemRequest request)
            throws ProductNotFoundException, InsufficientStockException, InvalidQuantityException;

    Cart updateItem(User user, Long itemId, Integer quantity)
            throws OrderNotFoundException, InsufficientStockException;

    Cart removeItem(User user, Long itemId) throws OrderNotFoundException;

    Cart clear(User user);

    // Confirma la compra: crea una Order a partir del carrito y lo vacia
    Order checkout(User user)
            throws EmptyCartException, InsufficientStockException, ProductNotFoundException;
}