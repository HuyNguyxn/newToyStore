package com.example.new_toy_store.product.domain;

import jakarta.persistence.*;

import java.security.PublicKey;
import java.time.LocalDateTime;

@Entity
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="product_name")
    private String productName;

    @Column(name="description")
    private String description;

    @Column(name="price")
    private Double price;

    @Column(name="stock")
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @Column(name="category")
    private String category;

    @Column(name="created_at")
    private LocalDateTime createdAt;


    public Product(){}

    public Product(int id, String productName, String description, Double price,
                   int stock, String category, LocalDateTime createdAt) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


}
