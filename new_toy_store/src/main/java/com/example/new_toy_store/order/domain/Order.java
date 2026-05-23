package com.example.new_toy_store.order.domain;

import jakarta.persistence.*;
import org.aspectj.weaver.ast.Or;

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
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public Order(String status) {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("Invalid status");

        this.status = status;
        this.createdAt = LocalDateTime.now();
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
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public void changeStatus(String status) {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("Invalid status");
        this.status = status;
    }

    // ===== GETTER =====

    public Integer getId() { return id; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

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