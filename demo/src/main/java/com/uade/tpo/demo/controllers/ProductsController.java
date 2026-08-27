package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.service.ProductService;

@RestController
@RequestMapping("products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId) {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        if (categoryId != null)
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageRequest));

        if (sellerId != null)
            return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageRequest));

        return ResponseEntity.ok(productService.getProducts(pageRequest));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        Optional<Product> result = productService.getProductById(productId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest productRequest) {
        Product result = productService.createProduct(productRequest);
        return ResponseEntity.created(URI.create("/products/" + result.getId())).body(result);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductRequest productRequest) throws ProductNotFoundException {
        return ResponseEntity.ok(productService.updateProduct(productId, productRequest));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer stock) throws ProductNotFoundException {
        return ResponseEntity.ok(productService.updateStock(productId, stock));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Long productId) throws ProductNotFoundException {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}