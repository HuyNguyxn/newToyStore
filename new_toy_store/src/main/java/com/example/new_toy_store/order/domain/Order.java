package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.order.common.BaseAuditEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_created_at", columnList = "created_at")
        }
)
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(String status) {
        this.status = OrderStatus.from(status);
    }

    public void addItem(Integer productId, String productName, int quantity, double price) {
        OrderItem item = new OrderItem(productId, productName, quantity, price);
        item.setOrder(this);
        this.items.add(item);
    }

    public void removeItem(OrderItem item) {
        if (item == null || item.getOrder() != this) return;
        item.setOrder(null);
        this.items.remove(item);
    }

    public double totalPrice() {
        return items.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }


    public void changeStatus(OrderStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Invalid status");

        this.status = status;
    }


    public void confirm() { status.confirm(this); }
    public void ship() { status.ship(this); }
    public void complete() { status.complete(this); }
    public void cancel() { status.cancel(this); }


    public Integer getId() { return id; }
    public OrderStatus getStatus() { return status; }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Order other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}