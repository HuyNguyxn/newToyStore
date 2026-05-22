package com.example.new_toy_store.order.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String productName;

    private int quantity;

    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {}

    public OrderItem(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Integer getId() { return id; }

    public String getProductName() { return productName; }

    public int getQuantity() { return quantity; }

    public double getPrice() { return price; }

    public Order getOrder() { return order; }

    public void setOrder(Order newOrder) {
        if (this.order == newOrder) return;
        if (this.order != null) {
            this.order.getItems().remove(this);
        }
        this.order = newOrder;
        if (newOrder != null && !newOrder.getItems().contains(this)) {
            newOrder.getItems().add(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem other = (OrderItem) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}