package com.example.new_toy_store.supplier_return.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SupplierReturnInspectionRequest {

    @NotEmpty(message = "Danh sách sản phẩm đồng kiểm không được rỗng")
    @Valid
    private List<ItemInspection> items;

    public List<ItemInspection> getItems() {
        return items;
    }

    public void setItems(List<ItemInspection> items) {
        this.items = items;
    }

    public static class ItemInspection {

        @NotNull(message = "Mã dòng hàng không được rỗng")
        private Integer itemId;

        @Min(value = 0, message = "Số lượng chấp nhận không được nhỏ hơn 0")
        private int acceptedQuantity;

        private String discrepancyReason;

        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
        public int getAcceptedQuantity() { return acceptedQuantity; }
        public void setAcceptedQuantity(int acceptedQuantity) { this.acceptedQuantity = acceptedQuantity; }
        public String getDiscrepancyReason() { return discrepancyReason; }
        public void setDiscrepancyReason(String discrepancyReason) { this.discrepancyReason = discrepancyReason; }
    }
}
