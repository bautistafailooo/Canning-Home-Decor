package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;

public interface ProductService {

    Page<Product> getProducts(PageRequest pageRequest);

    Page<Product> getProductsByCategory(Long categoryId, PageRequest pageRequest);

    Page<Product> getProductsBySeller(Long sellerId, PageRequest pageRequest);
    
    Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, PageRequest pageRequest);

    Optional<Product> getProductById(Long productId);

    Product createProduct(ProductRequest productRequest);

    Product updateProduct(Long productId, ProductRequest productRequest) throws ProductNotFoundException;

    void deleteProduct(Long productId) throws ProductNotFoundException;

    Product updateStock(Long productId, Integer newStock) throws ProductNotFoundException;

    // Descuenta stock al confirmar una compra; valida que haya stock suficiente
    Product decreaseStock(Long productId, Integer quantity) throws ProductNotFoundException, InsufficientStockException;
}