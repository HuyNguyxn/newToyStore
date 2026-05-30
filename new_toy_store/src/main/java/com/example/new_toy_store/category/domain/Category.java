package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.product.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "category_name")
)
public class Category extends BaseAuditEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private List<Product> products = new ArrayList<>();

    protected Category() {}

    public Category(String categoryName, String description) {
        if (categoryName == null || categoryName.isBlank())
            throw new IllegalArgumentException("Invalid name");

        this.categoryName = categoryName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }


    public void rename(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Invalid name");
        this.categoryName = newName;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Category c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}