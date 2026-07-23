package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.category.domain.exception.InvalidCategoryOperationException;
import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
        },
        indexes = {
                @Index(name = "idx_category_slug", columnList = "slug"),
                @Index(name = "idx_category_status", columnList = "status"),
                @Index(name = "idx_category_path", columnList = "path"),
                @Index(name = "idx_category_parent_order", columnList = "parent_id, display_order")
        }
)
public class Category extends BaseRootEntity {

    public static final int MAX_DEPTH = 3;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false, length = 500)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status = CategoryStatus.VISIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();

    protected Category() {}

    public Category(String name, String slug, String description, String iconUrl, Integer displayOrder) {
        if (name == null || name.trim().isEmpty() || slug == null || slug.trim().isEmpty()) throw InvalidCategoryOperationException.emptyNameOrSlug();
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconUrl = iconUrl;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.path = "/" + slug + "/";
    }

    public void hide() { this.status = CategoryStatus.HIDDEN; }
    public void show() { this.status = CategoryStatus.VISIBLE; }

    public void assignParent(Category parentCategory) {
        if (parentCategory != null) {
            if (this.equals(parentCategory)) throw InvalidCategoryOperationException.selfParenting(this.id);
            if (parentCategory.getLevel() >= MAX_DEPTH) throw InvalidCategoryOperationException.maxDepthExceeded(MAX_DEPTH);

            Category currentAncestor = parentCategory;
            while (currentAncestor != null) {
                if (this.equals(currentAncestor)) throw InvalidCategoryOperationException.circularReference(this.id, parentCategory.getId());
                currentAncestor = currentAncestor.getParent();
            }
            this.level = parentCategory.getLevel() + 1;
            this.path = parentCategory.getPath() + this.slug + "/";
        } else {
            this.level = 1;
            this.path = "/" + this.slug + "/";
        }
        this.parent = parentCategory;
    }

    public void removeParent() {
        this.parent = null;
        this.level = 1;
        this.path = "/" + this.slug + "/";
    }

    public void update(String name, String slug, String description, String iconUrl, Integer displayOrder) {
        if (name != null && !name.trim().isEmpty()) this.name = name;
        if (slug != null && !slug.trim().isEmpty()) {
            this.slug = slug;
            this.path = (this.parent != null ? this.parent.getPath() : "/") + this.slug + "/";
        }
        this.description = description;
        if (iconUrl != null) this.iconUrl = iconUrl;
        if (displayOrder != null) this.displayOrder = displayOrder;
    }

    @Override
    public void delete() {
        this.status = CategoryStatus.DELETED;
        super.delete();
        this.slug = this.slug + "-da_xoa-" + System.currentTimeMillis();
        this.subCategories.forEach(Category::delete);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getLevel() { return level; }
    public String getPath() { return path; }
    public CategoryStatus getStatus() { return status; }
    public Category getParent() { return parent; }
    public List<Category> getSubCategories() { return Collections.unmodifiableList(subCategories); }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Category c && id != null && id.equals(c.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}
