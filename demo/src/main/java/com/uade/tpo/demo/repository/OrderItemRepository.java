package com.uade.tpo.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    // Permite releer los items desde la base y evitar devolver una coleccion
    // desactualizada que Hibernate tenga cacheada.
    List<OrderItem> findByOrderId(Long orderId);
}