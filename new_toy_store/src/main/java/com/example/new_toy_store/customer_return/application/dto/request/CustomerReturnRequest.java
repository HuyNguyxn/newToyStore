package com.example.new_toy_store.customer_return.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CustomerReturnRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer orderId;

    @NotEmpty(message = "Danh sách sản phẩm trả không được để trống")
    @Valid
    private List<CustomerReturnItemRequest> items;

    @Size(max = 5, message = "Chỉ được phép tải lên tối đa 5 hình ảnh chứng minh")
    private List<String> proofImageUrls;

    @Size(max = 500, message = "Ghi chú lý do không được vượt quá 500 ký tự")
    private String reasonNote;

    public CustomerReturnRequest() {}

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public List<CustomerReturnItemRequest> getItems() { return items; }
    public void setItems(List<CustomerReturnItemRequest> items) { this.items = items; }

    public List<String> getProofImageUrls() { return proofImageUrls; }
    public void setProofImageUrls(List<String> proofImageUrls) { this.proofImageUrls = proofImageUrls; }

    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }
}