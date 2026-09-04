package com.uade.tpo.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.service.CartService;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<Order> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<Order> addItem(
            @AuthenticationPrincipal User user,
            @RequestBody CartItemRequest request) throws ProductNotFoundException, InsufficientStockException {
        return ResponseEntity.ok(cartService.addItem(user, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Order> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) throws OrderNotFoundException, InsufficientStockException {
        return ResponseEntity.ok(cartService.updateItem(user, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Order> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) throws OrderNotFoundException {
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal User user)
            throws OrderNotFoundException, InsufficientStockException {
        return ResponseEntity.ok(cartService.checkout(user));
    }
}