package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.order.domain.exception.InvalidOrderDataException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "order_histories",
        indexes = {@Index(name = "idx_order_history_order", columnList = "order_id")}
)
public class OrderHistory extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected OrderHistory() {}

    public OrderHistory(OrderStatus status, String note) {
        if (status == null) throw new InvalidOrderDataException("status", "Trạng thái không được để trống");
        if (note == null || note.trim().isEmpty()) throw new InvalidOrderDataException("note", "Ghi chú không được để trống");
        this.status = status;
        this.note = note;
    }

    void setOrder(Order order) { this.order = order; }

    public Integer getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public String getNote() { return note; }
    public Order getOrder() { return order; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof OrderHistory other && id != null && id.equals(other.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}
