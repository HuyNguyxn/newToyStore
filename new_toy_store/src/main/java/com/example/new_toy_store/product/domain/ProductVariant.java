package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_variant_product_id", columnList = "product_id")
        }
)
public class ProductVariant extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariantType type;

    @Column(nullable = false)
    private double price;

    @Column(name = "cost_price", nullable = false)
    private double costPrice = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Inventory inventory;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> attributes = new ArrayList<>();

    protected ProductVariant() {}

    public static ProductVariant createDefaultPlaceholder(int initialStock, double price) {
        ProductVariant variant = new ProductVariant();
        variant.type = VariantType.DEFAULT;
        variant.price = price;
        variant.initInventory(initialStock);
        return variant;
    }

    public static ProductVariant createRealVariant(int initialStock, double price, boolean isMaster) {
        ProductVariant variant = new ProductVariant();
        variant.type = isMaster ? VariantType.MASTER : VariantType.REGULAR;
        variant.price = price;
        variant.initInventory(initialStock);
        return variant;
    }

    void setProduct(Product product) {
        this.product = product;
    }

    public void makeMaster() {
        if (!this.type.canChangeTo(VariantType.MASTER)) {
            throw InvalidProductOperationException.invalidVariantTransition(this.type.getDisplayName(), VariantType.MASTER.getDisplayName());
        }
        this.type = VariantType.MASTER;
    }

    public void makeRegular() {
        if (!this.type.canChangeTo(VariantType.REGULAR)) {
            throw InvalidProductOperationException.invalidVariantTransition(this.type.getDisplayName(), VariantType.REGULAR.getDisplayName());
        }
        this.type = VariantType.REGULAR;
    }

    private void initInventory(int initialStock) {
        this.inventory = new Inventory(initialStock);
        this.inventory.setVariant(this);
    }

    public void addAttribute(String name, String value) {
        if (!this.type.canAddAttributes()) {
            throw InvalidProductOperationException.cannotAddAttributes(this.type.getDisplayName());
        }
        ProductAttributeValue attribute = new ProductAttributeValue(name, value);
        attribute.setVariant(this);
        this.attributes.add(attribute);
    }

    public void importStock(int addedQuantity, double importPrice) {
        if (addedQuantity <= 0 || importPrice < 0) {
            throw InvalidProductOperationException.invalidImportData();
        }

        int currentStock = this.inventory != null ? this.inventory.getStockQuantity() : 0;

        if (currentStock <= 0) {
            this.costPrice = Math.max(0.0, Math.round(importPrice * 100.0) / 100.0);
        } else {
            double totalOldValue = (double) currentStock * this.costPrice;
            double totalNewValue = (double) addedQuantity * importPrice;
            int totalNewStock = currentStock + addedQuantity;

            double mac = (totalOldValue + totalNewValue) / totalNewStock;
            this.costPrice = Math.max(0.0, Math.round(mac * 100.0) / 100.0);
        }

        if (this.inventory != null) {
            this.inventory.addStock(addedQuantity);
        }
    }

    public void updatePrice(double newPrice) {
        if (newPrice < 0) {
            throw InvalidProductOperationException.negativePrice();
        }
        this.price = newPrice;
    }

    @Override
    public void delete() {
        super.delete();
        if (this.inventory != null) {
            this.inventory.delete();
        }
        this.attributes.forEach(BaseAuditEntity::delete);
    }

    public String generateAttributesSnapshot() {
        if (this.type == VariantType.DEFAULT || this.attributes.isEmpty()) {
            return "Phiên bản tiêu chuẩn";
        }
        return this.attributes.stream()
                .map(attr -> attr.getAttributeName() + ": " + attr.getAttributeValue())
                .collect(Collectors.joining(", "));
    }

    public Integer getId() { return id; }
    public Long getVersion() { return version; }
    public VariantType getType() { return type; }
    public double getPrice() { return price; }
    public double getCostPrice() { return costPrice; }
    public Product getProduct() { return product; }
    public Inventory getInventory() { return inventory; }
    public List<ProductAttributeValue> getAttributes() { return Collections.unmodifiableList(attributes); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProductVariant v && id != null && id.equals(v.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}