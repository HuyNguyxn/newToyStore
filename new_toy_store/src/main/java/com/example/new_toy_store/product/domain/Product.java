package com.example.new_toy_store.product.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_name", columnNames = "product_name"),
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id")
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private int stock;

    // ❗ loose coupling
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Product() {}

    public Product(String productName, String description, Double price, int stock, Integer categoryId) {
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Invalid name");

        if (price == null || price <= 0)
            throw new IllegalArgumentException("Invalid price");

        if (categoryId == null)
            throw new IllegalArgumentException("Category required");

        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.createdAt = LocalDateTime.now();
    }

    public void changeCategory(Integer newCategoryId) {
        if (newCategoryId == null) throw new IllegalArgumentException("Invalid category");
        this.categoryId = newCategoryId;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Invalid quantity");
        if (stock < quantity) throw new IllegalStateException("Not enough stock");
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Invalid quantity");
        stock += quantity;
    }

    public void changePrice(Double newPrice) {
        if (newPrice == null || newPrice <= 0)
            throw new IllegalArgumentException("Invalid price");
        this.price = newPrice;
    }

    public Integer getId() { return id; }
    public String getProductName() { return productName; }
    public Double getPrice() { return price; }
    public int getStock() { return stock; }
    public Integer getCategoryId() { return categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Product p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}