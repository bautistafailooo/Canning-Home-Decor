package com.uade.tpo.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Historial del usuario, de la mas reciente a la mas vieja
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Para que nadie pueda ver la orden de otro
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}