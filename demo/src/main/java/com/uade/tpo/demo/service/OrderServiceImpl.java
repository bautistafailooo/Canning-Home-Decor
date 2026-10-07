package com.uade.tpo.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<Order> getOrders(User user) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        orders.forEach(o -> o.setItems(orderItemRepository.findByOrderId(o.getId())));
        return orders;
    }

    // Se busca por id Y por usuario, asi nadie puede ver la orden de otro
    @Transactional(readOnly = true)
    public Order getOrderById(User user, Long orderId) throws OrderNotFoundException {
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(OrderNotFoundException::new);
        order.setItems(orderItemRepository.findByOrderId(order.getId()));
        return order;
    }
}