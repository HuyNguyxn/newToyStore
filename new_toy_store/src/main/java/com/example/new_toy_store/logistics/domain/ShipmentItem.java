package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentDataException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "shipment_items",
        indexes = {
                @Index(name = "idx_shipment_item_shipment", columnList = "shipment_id"),
                @Index(name = "idx_shipment_item_product", columnList = "product_id"),
                @Index(name = "idx_shipment_item_variant", columnList = "variant_id")
        }
)
public class ShipmentItem extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "variant_snapshot", nullable = false)
    private String variantSnapshot;

    @Column(nullable = false)
    private int quantity;

    protected ShipmentItem() {}

    public ShipmentItem(Integer productId, Integer variantId, String productNameSnapshot, String variantSnapshot, int quantity) {
        if (productId == null) throw new InvalidShipmentDataException("productId", "Product id must not be empty.");
        if (variantId == null) throw new InvalidShipmentDataException("variantId", "Variant id must not be empty.");
        if (productNameSnapshot == null || productNameSnapshot.trim().isEmpty()) {
            throw new InvalidShipmentDataException("productNameSnapshot", "Product name snapshot must not be empty.");
        }
        if (quantity <= 0) throw new InvalidShipmentDataException("quantity", "Shipment item quantity must be greater than 0.");
        this.productId = productId;
        this.variantId = variantId;
        this.productNameSnapshot = productNameSnapshot.trim();
        this.variantSnapshot = variantSnapshot == null || variantSnapshot.trim().isEmpty() ? "N/A" : variantSnapshot.trim();
        this.quantity = quantity;
    }

    void setShipment(Shipment shipment) { this.shipment = shipment; }

    public Integer getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getVariantSnapshot() { return variantSnapshot; }
    public int getQuantity() { return quantity; }
}
