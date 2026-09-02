package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> getProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest);
    }

    public Page<Product> getProductsByCategory(Long categoryId, PageRequest pageRequest) {
        return productRepository.findByCategoryId(categoryId, pageRequest);
    }

    public Page<Product> getProductsBySeller(Long sellerId, PageRequest pageRequest) {
        return productRepository.findBySellerId(sellerId, pageRequest);
    }

    public Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, PageRequest pageRequest) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageRequest);
    }

    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public Product createProduct(ProductRequest productRequest) {
        Product product = new Product();
        applyRequestToProduct(product, productRequest);
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, ProductRequest productRequest) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        applyRequestToProduct(product, productRequest);
        return productRepository.save(product);
    }

    public void deleteProduct(Long productId) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        productRepository.delete(product);
    }

    public Product updateStock(Long productId, Integer newStock) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        product.setStock(newStock);
        return productRepository.save(product);
    }

    public Product decreaseStock(Long productId, Integer quantity)
            throws ProductNotFoundException, InsufficientStockException {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getStock() < quantity)
            throw new InsufficientStockException();

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    private void applyRequestToProduct(Product product, ProductRequest request) {
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0.0);

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }

        if (request.getSellerId() != null) {
            User seller = new User();
            seller.setId(request.getSellerId());
            product.setSeller(seller);
        }
    }
}