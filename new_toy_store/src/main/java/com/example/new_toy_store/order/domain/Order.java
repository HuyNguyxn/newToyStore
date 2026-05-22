package com.example.new_toy_store.order.domain;

import jakarta.persistence.*;
import org.aspectj.weaver.ast.Or;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void removeItem(OrderItem item){
        if(item.getOrder() != this) return;
        item.setOrder(null);
        this.items.remove(item);
    }
    public Double totalPrice(){
        return items.stream()
                .mapToDouble(i-> i.getPrice() * i.getQuantity())
                .sum();
    }

    public Integer getId() { return id; }

    public String getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order other = (Order) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}