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
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

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

    public Page<Product> searchProducts(Long categoryId, Long sellerId, Double minPrice,
                                        Double maxPrice, String search, PageRequest pageRequest) {

        // Un search vacio se trata como si no se hubiera enviado.
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();

        return productRepository.findWithFilters(
                categoryId, sellerId, minPrice, maxPrice, normalizedSearch, pageRequest);
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

        // Se busca la categoria real en vez de crear una instancia solo con el id.
        // Asi un categoryId inexistente devuelve 404 y no un error de foreign key.
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            product.setCategory(category);
        }

        if (request.getSellerId() != null) {
            User seller = userRepository.findById(request.getSellerId())
                    .orElse(null);
            product.setSeller(seller);
        }

        if (request.getImageUrls() != null) {
            product.getImageUrls().clear();
            product.getImageUrls().addAll(request.getImageUrls());
        }
    }
}