package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;

public interface CartService {

    Order getCart(User user);

    Order addItem(User user, CartItemRequest request) throws ProductNotFoundException, InsufficientStockException;

    Order updateItem(User user, Long itemId, Integer quantity)
            throws OrderNotFoundException, InsufficientStockException;

    Order removeItem(User user, Long itemId) throws OrderNotFoundException;

    Order checkout(User user) throws OrderNotFoundException, InsufficientStockException;
}