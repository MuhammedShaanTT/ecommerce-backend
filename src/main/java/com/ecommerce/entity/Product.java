package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private LocalDateTime createdAt;

    // seller who added the product
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
