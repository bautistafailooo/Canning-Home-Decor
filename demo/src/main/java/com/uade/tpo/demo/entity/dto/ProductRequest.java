package com.uade.tpo.demo.entity.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductRequest {
    private String description;
    private Double price;
    private Integer stock;
    private Double discount;
    private Long categoryId;
    private Long sellerId;
    private List<String> imageUrls;
}