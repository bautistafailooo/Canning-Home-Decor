package com.uade.tpo.demo.entity;

/**
 * Estado de una orden. El estado CART ya no existe: el carrito es ahora
 * una entidad propia (Cart) y una Order se crea recien al confirmar.
 */
public enum OrderStatus {
    COMPLETED,
    CANCELLED
}