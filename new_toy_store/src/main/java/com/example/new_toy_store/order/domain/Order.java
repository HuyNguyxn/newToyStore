package com.example.new_toy_store.order.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "items")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(String status) {
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }


    public void addItem(String productName, int quantity, double price) {
        OrderItem item = new OrderItem(productName, quantity, price);
        item.setOrder(this);
        this.items.add(item);
    }

    public Long getId() { return id; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    public void setStatus(String status) { this.status = status; }
}