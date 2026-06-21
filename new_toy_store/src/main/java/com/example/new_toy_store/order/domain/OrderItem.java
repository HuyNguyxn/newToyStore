package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_variant", columnNames = {"order_id", "variant_id"})
        },
        indexes = {
                @Index(name = "idx_order_item_order", columnList = "order_id"),
                @Index(name = "idx_order_item_product", columnList = "product_id")
        }
)
public class OrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Column(name = "variant_attributes_snapshot", nullable = false)
    private String variantAttributesSnapshot;

    protected OrderItem() {}

    public OrderItem(Integer productId, Integer variantId, String productName, String variantAttributesSnapshot, int quantity, double price) {
        if (productId == null || variantId == null) {
            throw new IllegalArgumentException("ID sản phẩm và biến thể không được để trống");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Giá không hợp lệ");
        }
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantAttributesSnapshot = variantAttributesSnapshot;
        this.quantity = quantity;
        this.price = price;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public Integer getId() { return id; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Order getOrder() { return order; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof OrderItem other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}