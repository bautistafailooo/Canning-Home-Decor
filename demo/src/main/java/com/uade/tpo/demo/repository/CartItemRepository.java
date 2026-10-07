package com.uade.tpo.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Para saber si el producto ya estaba en el carrito y sumar cantidades
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // Relee los items desde la base, para no devolver la coleccion
    // que Hibernate pueda tener cacheada
    List<CartItem> findByCartId(Long cartId);
}