package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.service.ProductService;

@RestController
@RequestMapping("products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    // El catalogo es publico, asi que el usuario puede venir en null.
    // Solo el administrador puede ver los productos dados de baja.
    private boolean esAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    // Todos los filtros son opcionales y se combinan entre si.
    // Ejemplos:
    //   /products
    //   /products?categoryId=1
    //   /products?categoryId=1&minPrice=50000&maxPrice=200000
    //   /products?search=roble
    //   /products?minPrice=500000
    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive,
            @AuthenticationPrincipal User user) {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        // includeInactive se honra solo si lo pide un administrador.
        // Sin esto, cualquiera agregaria ?includeInactive=true a la URL
        // y veria los productos que se dieron de baja.
        boolean verDeBaja = includeInactive && esAdmin(user);

        return ResponseEntity.ok(productService.searchProducts(
                categoryId, sellerId, minPrice, maxPrice, search, verDeBaja, pageRequest));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        Optional<Product> result = productService.getProductById(productId);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();

        // Un producto dado de baja no esta en el catalogo, pero su link
        // sigue existiendo. Para cualquiera que no sea el administrador
        // se responde como si no existiera, asi tampoco se puede agregar
        // al carrito entrando de memoria a /productos/12.
        Product product = result.get();
        if (!product.getActive() && !esAdmin(user))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(product);
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

    // Vuelve a publicar un producto dado de baja, o lo da de baja.
    @PatchMapping("/{productId}/active")
    public ResponseEntity<Product> setActive(
            @PathVariable Long productId,
            @RequestParam boolean active) throws ProductNotFoundException {
        return ResponseEntity.ok(productService.setActive(productId, active));
    }

    // No borra la fila: da de baja. Ver ProductServiceImpl.
    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Long productId) throws ProductNotFoundException {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}