package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "customer_return_histories")
public class CustomerReturnHistory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_return_id", nullable = false)
    private CustomerReturn customerReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private CustomerReturnStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private CustomerReturnStatus newStatus;

    @Column(name = "action_by", nullable = false)
    private String actionBy;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(length = 500)
    private String note;

    protected CustomerReturnHistory() {}

    public CustomerReturnHistory(CustomerReturnStatus oldStatus, CustomerReturnStatus newStatus, String actionBy, String note) {
        if (newStatus == null) throw InvalidCustomerReturnDataException.emptyField("Trạng thái mới");
        if (actionBy == null || actionBy.trim().isEmpty()) throw InvalidCustomerReturnDataException.emptyField("Người thao tác");

        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actionBy = actionBy;
        this.actionDate = LocalDateTime.now();
        this.note = note;
    }

    void assignToReturn(CustomerReturn customerReturn) {
        this.customerReturn = customerReturn;
    }

    public Integer getId() { return id; }
    public CustomerReturn getCustomerReturn() { return customerReturn; }
    public CustomerReturnStatus getOldStatus() { return oldStatus; }
    public CustomerReturnStatus getNewStatus() { return newStatus; }
    public String getActionBy() { return actionBy; }
    public LocalDateTime getActionDate() { return actionDate; }
    public String getNote() { return note; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CustomerReturnHistory p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}