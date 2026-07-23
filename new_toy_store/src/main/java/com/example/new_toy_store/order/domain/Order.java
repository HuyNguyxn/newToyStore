package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.order.domain.exception.InvalidOrderDataException;
import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_created_at", columnList = "created_at"),
                @Index(name = "idx_order_user_id", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "status")
        }
)
public class Order extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false)
    private double totalAmount = 0.0;

    @Column(nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "promo_code", length = 50)
    private String promoCode;

    @Column(name = "discount_amount", nullable = false)
    private double discountAmount = 0.0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderHistory> histories = new ArrayList<>();

    protected Order() {}

    public Order(Integer userId, String shippingAddress) {
        if (userId == null) throw new InvalidOrderDataException("userId", "ID người dùng không được để trống");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) throw new InvalidOrderDataException("shippingAddress", "Địa chỉ giao hàng không được để trống");
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.recordHistory(this.status, "Đơn hàng được tạo mới");
    }

    public void addItem(Integer productId, Integer variantId, String productName, String variantAttributesSnapshot, int quantity, double price) {
        OrderItem item = new OrderItem(productId, variantId, productName, variantAttributesSnapshot, quantity, price);
        item.setOrder(this);
        this.items.add(item);
        calculateTotal();
    }

    public void applyPromoCode(String promoCode, double discountAmount) {
        this.promoCode = promoCode;
        this.discountAmount = Math.max(0.0, Math.round(discountAmount * 100.0) / 100.0);
        calculateTotal();
    }

    private void calculateTotal() {
        double rawTotal = items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        double finalTotal = rawTotal - this.discountAmount;
        this.totalAmount = Math.max(0.0, Math.round(finalTotal * 100.0) / 100.0);
    }

    public void updateShippingAddress(String newAddress, String note) {
        if (newAddress == null || newAddress.trim().isEmpty()) throw new InvalidOrderDataException("shippingAddress", "Địa chỉ giao hàng mới không được để trống");
        if (!this.status.canModifyShippingInfo()) throw new InvalidOrderOperationException(this.status.getDisplayName(), "Cập nhật địa chỉ giao hàng");
        String oldAddress = this.shippingAddress;
        this.shippingAddress = newAddress.trim();
        String historyNote = note != null && !note.trim().isEmpty() ? note : "Cập nhật địa chỉ từ [" + oldAddress + "] thành [" + this.shippingAddress + "]";
        recordHistory(this.status, historyNote);
    }

    public void changeStatus(OrderStatus newStatus, String note) {
        this.status = newStatus;
        recordHistory(this.status, note != null && !note.trim().isEmpty() ? note : "Cập nhật trạng thái: " + newStatus.getDisplayName());
    }

    private void recordHistory(OrderStatus status, String note) {
        OrderHistory history = new OrderHistory(status, note);
        history.setOrder(this);
        this.histories.add(history);
    }

    public void confirm(String note) { status.confirm(this, note); }
    public void ship(String note) { status.ship(this, note); }
    public void complete(String note) { status.complete(this, note); }
    public void cancel(String note) { status.cancel(this, note); }
    public void refundPartially(String note) { status.refundPartially(this, note); }
    public void refundFully(String note) { status.refundFully(this, note); }

    @Override
    public void delete() {
        if (!this.status.canBeDeleted()) throw new InvalidOrderOperationException(this.status.getDisplayName(), "Xóa đơn hàng");
        super.delete();
        this.items.forEach(OrderItem::delete);
        this.histories.forEach(OrderHistory::delete);
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public double getDiscountAmount() { return discountAmount; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public List<OrderHistory> getHistories() { return Collections.unmodifiableList(histories); }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Order other && id != null && id.equals(other.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}
