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

    // Busqueda combinada: cualquier filtro puede venir en null y se ignora.
    Page<Product> searchProducts(Long categoryId, Long sellerId, Double minPrice,
                                 Double maxPrice, String search, boolean includeInactive,
                                 PageRequest pageRequest);

    Optional<Product> getProductById(Long productId);

    Product createProduct(ProductRequest productRequest);

    Product updateProduct(Long productId, ProductRequest productRequest) throws ProductNotFoundException;

    // Da de baja el producto: sale del catalogo, pero la fila queda.
    void deleteProduct(Long productId) throws ProductNotFoundException;

    Product setActive(Long productId, boolean active) throws ProductNotFoundException;

    Product updateStock(Long productId, Integer newStock) throws ProductNotFoundException;

    // Descuenta stock al confirmar una compra; valida que haya stock suficiente
    Product decreaseStock(Long productId, Integer quantity) throws ProductNotFoundException, InsufficientStockException;
}