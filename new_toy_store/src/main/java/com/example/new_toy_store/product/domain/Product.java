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
    private Integer id;

    @Column(name="product_name", nullable = false)
    private String productName;

    private String description;

    private Double price;

    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

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

    public Integer getId() {
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

    public void changeCategory(Category newCategory) {
        if (this.category == newCategory) {
            return;
        }
        if (this.category != null) {
            this.category.getProducts().remove(this);
        }
        this.category = newCategory;
        if (newCategory != null && !newCategory.getProducts().contains(this)) {
            newCategory.getProducts().add(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product other = (Product) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
