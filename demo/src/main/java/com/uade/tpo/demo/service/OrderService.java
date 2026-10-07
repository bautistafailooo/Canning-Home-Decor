package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;

public interface OrderService {

    // Historial de compras del usuario autenticado
    List<Order> getOrders(User user);

    Order getOrderById(User user, Long orderId) throws OrderNotFoundException;
}