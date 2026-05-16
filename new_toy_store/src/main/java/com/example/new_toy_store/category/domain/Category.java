package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.product.domain.Product;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "category", uniqueConstraints = @UniqueConstraint(columnNames = "category_name"))
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
    public Category(){}

    public Category(String description, LocalDateTime createdAt, String categoryName) {
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.categoryName = categoryName;
    }
    public void rename(String newName) {
        this.categoryName = newName;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

//    public void addProduct(Product product) {
//        this.products.add(product);
//        product.assignCategory(this);
//    }
}
