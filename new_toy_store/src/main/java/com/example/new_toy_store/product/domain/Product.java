package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.category.domain.Category;
import jakarta.persistence.*;

import java.security.PublicKey;
import java.time.LocalDateTime;

@Entity
@Table(name="products", uniqueConstraints = @UniqueConstraint(columnNames = "product_name"))
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
    private Category category;

    @Column(name="created_at")
    private LocalDateTime createdAt;


    public Product(){}

    public Product(int id, String productName, String description, Double price,
                   int stock, LocalDateTime createdAt) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
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

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


}
