package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "customer_return_images")
public class CustomerReturnImage extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_return_id", nullable = false)
    private CustomerReturn customerReturn;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    protected CustomerReturnImage() {}

    public CustomerReturnImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw InvalidCustomerReturnDataException.emptyField("Đường dẫn hình ảnh chứng minh");
        }
        this.imageUrl = imageUrl;
    }

    void assignToReturn(CustomerReturn customerReturn) {
        this.customerReturn = customerReturn;
    }

    public Integer getId() { return id; }
    public CustomerReturn getCustomerReturn() { return customerReturn; }
    public String getImageUrl() { return imageUrl; }
}