package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "categories")
@SQLRestriction("deleted_at IS NULL")
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();

    protected Category() {}

    public Category(String name, String slug, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug is required");
        }
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public void updateInfo(String name, String slug, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (slug != null && !slug.trim().isEmpty()) {
            this.slug = slug;
        }
        this.description = description;
    }

    public void assignParent(Category parentCategory) {
        if (parentCategory != null && parentCategory.getId().equals(this.id)) {
            throw new IllegalStateException("A category cannot be its own parent");
        }
        this.parent = parentCategory;
    }

    public void removeParent() {
        this.parent = null;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Category getParent() { return parent; }
    public List<Category> getSubCategories() { return Collections.unmodifiableList(subCategories); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Category c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}