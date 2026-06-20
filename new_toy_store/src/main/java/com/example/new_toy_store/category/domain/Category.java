package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_slug", columnList = "slug"),
                @Index(name = "idx_category_parent_id", columnList = "parent_id"),
                @Index(name = "idx_category_status", columnList = "status")
        }
)
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status = CategoryStatus.VISIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();

    protected Category() {}

    public Category(String name, String slug, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn tĩnh không được để trống");
        }
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public void hide() {
        this.status = CategoryStatus.HIDDEN;
    }

    public void show() {
        this.status = CategoryStatus.VISIBLE;
    }

    public void assignParent(Category parentCategory) {
        if (parentCategory != null) {
            if (this.equals(parentCategory)) {
                throw new IllegalArgumentException("Không thể tự nhận chính mình làm danh mục cha");
            }

            Category currentAncestor = parentCategory;
            while (currentAncestor != null) {
                if (this.equals(currentAncestor)) {
                    throw new IllegalArgumentException("Phát hiện lỗi vòng lặp trong cấu trúc. Danh mục này hiện đang là cấp trên của danh mục mục tiêu.");
                }
                currentAncestor = currentAncestor.getParent();
            }
        }
        this.parent = parentCategory;
    }

    public void removeParent() {
        this.parent = null;
    }

    public void update(String name, String slug, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (slug != null && !slug.trim().isEmpty()) {
            this.slug = slug;
        }
        this.description = description;
    }

    @Override
    public void delete() {
        super.delete();
        this.slug = this.slug + "-da_xoa-" + System.currentTimeMillis();
        this.subCategories.forEach(BaseAuditEntity::delete);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public CategoryStatus getStatus() { return status; }
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