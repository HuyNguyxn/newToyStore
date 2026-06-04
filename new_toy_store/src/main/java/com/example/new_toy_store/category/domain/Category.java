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
                @Index(name = "idx_category_parent_id", columnList = "parent_id")
        }
)
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();

    protected Category() {}

    public Category(String name, String slug, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug is required");
        }
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public void assignParent(Category parentCategory) {
        /*
        // Code cũ: Chỉ chặn được trường hợp danh mục tự nhận chính nó làm cha trực tiếp
        if (parentCategory != null && parentCategory.getId().equals(this.id)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        */

        // Code mới: Deep Cycle Detection Algorithm (Ngăn chặn vòng lặp vô tận đồ thị A -> B -> C -> A)
        if (parentCategory != null) {
            Category currentAncestor = parentCategory;
            while (currentAncestor != null) {
                if (currentAncestor.getId() != null && currentAncestor.getId().equals(this.id)) {
                    throw new IllegalArgumentException("Detected cyclic dependency in category hierarchy. This category is already an ancestor of the target parent.");
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
        this.subCategories.forEach(BaseAuditEntity::delete);
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