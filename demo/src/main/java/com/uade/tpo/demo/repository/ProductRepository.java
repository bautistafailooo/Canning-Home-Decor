package com.uade.tpo.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Una unica consulta que combina todos los filtros.
    // El patron ":param IS NULL OR <condicion>" hace que cada filtro
    // desaparezca de la busqueda cuando no se envia, sin necesidad de
    // escribir una query distinta para cada combinacion posible.
    @Query("SELECT p FROM Product p WHERE "
            + "(:categoryId IS NULL OR p.category.id = :categoryId) AND "
            + "(:sellerId IS NULL OR p.seller.id = :sellerId) AND "
            + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
            + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
            + "(:search IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("search") String search,
            Pageable pageable);
}