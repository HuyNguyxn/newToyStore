package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "customer_return_items")
public class CustomerReturnItem extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_return_id", nullable = false)
    private CustomerReturn customerReturn;

    @Column(name = "order_item_id", nullable = false)
    private Integer orderItemId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private ReturnReasonCode reasonCode;

    @Column(name = "expected_refund_amount", nullable = false)
    private double expectedRefundAmount;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    protected CustomerReturnItem() {}

    public CustomerReturnItem(Integer orderItemId, Integer productId, Integer variantId, Integer quantity, ReturnReasonCode reasonCode, double expectedRefundAmount) {
        if (orderItemId == null) throw InvalidCustomerReturnDataException.emptyField("Mã sản phẩm trong đơn");
        if (productId == null) throw InvalidCustomerReturnDataException.emptyField("Mã sản phẩm");
        if (variantId == null) throw InvalidCustomerReturnDataException.emptyField("Mã biến thể");
        if (quantity == null || quantity <= 0) throw InvalidCustomerReturnDataException.emptyField("Số lượng trả");
        if (reasonCode == null) throw InvalidCustomerReturnDataException.emptyField("Lý do trả hàng");

        this.orderItemId = orderItemId;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.reasonCode = reasonCode;
        this.expectedRefundAmount = expectedRefundAmount;
    }

    void assignToReturn(CustomerReturn customerReturn) {
        this.customerReturn = customerReturn;
    }

    public Integer getId() { return id; }
    public CustomerReturn getCustomerReturn() { return customerReturn; }
    public Integer getOrderItemId() { return orderItemId; }
    public Integer getQuantity() { return quantity; }
    public ReturnReasonCode getReasonCode() { return reasonCode; }
    public double getExpectedRefundAmount() { return expectedRefundAmount; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CustomerReturnItem p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}