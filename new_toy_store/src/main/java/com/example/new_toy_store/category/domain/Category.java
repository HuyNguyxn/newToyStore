package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.product.domain.Product;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "category", uniqueConstraints = @UniqueConstraint(columnNames = "category_name"))
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    private String description;

    private LocalDateTime createdAt;

    private List<Product> products = new ArrayList<>();
    public Category(){}

    public Category(String description, LocalDateTime createdAt, String categoryName) {
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.categoryName = categoryName;
    }

    public Integer getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void rename(String newName) {
        this.categoryName = newName;
    }

    public void changeDescription(String description) {
        this.description = description;
    }


    public List<Product> getProducts() {
        return products;
    }

    public void addProduct(Product product) {
        if (product.getCategory() != this) {
            product.changeCategory(this);
        }
    }
    public void removeProduct(Product product){
        if (products.contains(product)){
            product.changeCategory(null);
        }
    }
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null ||getClass() !=o.getClass() ) return false;
        Category other = (Category) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
