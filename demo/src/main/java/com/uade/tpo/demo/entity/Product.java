package com.uade.tpo.demo.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    // Porcentaje de descuento sobre el precio (0 a 100). 0 = sin descuento.
    @Column
    private Double discount = 0.0;

    // Una o mas fotos del producto, guardadas como URL.
    // Hibernate crea la tabla product_images automaticamente.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    // Usuario vendedor que publico el producto
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private User seller;

    @Transient
    public Double getFinalPrice() {
        if (price == null)
            return null;
        double discountPercentage = discount != null ? discount : 0.0;
        return price * (1 - discountPercentage / 100.0);
    }

    // El enunciado pide que la falta de stock quede indicada.
    @Transient
    public boolean isAvailable() {
        return stock != null && stock > 0;
    }
}