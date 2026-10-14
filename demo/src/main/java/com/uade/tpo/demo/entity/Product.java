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
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
// equals y hashCode SOLO por id, para evitar la recursion infinita que
// provoca la relacion bidireccional Product <-> Category.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Ojo con el nombre: description es el NOMBRE del producto
    // ("Sillon de pana dos cuerpos"), no su descripcion. Viene asi
    // desde el principio y lo mismo pasa en Category.
    @Column
    private String description;

    // La descripcion de verdad: el texto largo que se lee en el
    // detalle. Es opcional, y 1000 caracteres alcanzan de sobra para
    // un parrafo o dos sobre materiales y medidas.
    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    // Baja logica. Borrar de verdad un producto que ya figura en una
    // orden rompe la clave foranea de order_item, y ademas se perderia
    // el historial de quien lo compro. Dar de baja lo saca del catalogo
    // y deja la fila donde esta.
    //
    // columnDefinition pone el DEFAULT TRUE en la tabla: asi los
    // productos que ya existian quedan publicados cuando Hibernate
    // agrega la columna, en vez de darse de baja todos juntos.
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

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
    @ToString.Exclude
    private Category category;

    // Usuario vendedor que publico el producto
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    @ToString.Exclude
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