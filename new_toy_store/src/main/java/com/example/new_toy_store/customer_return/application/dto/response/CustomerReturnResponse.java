package com.example.new_toy_store.customer_return.application.dto.response;

import java.util.List;

public class CustomerReturnResponse {
    private Integer id;
    private Integer orderId;
    private double returnShippingFee;
    private double totalRefundAmount;
    private String status;
    private String statusDisplayName;
    private List<String> availableActions;
    private List<String> proofImages;
    private List<CustomerReturnItemResponse> items;
    private List<CustomerReturnHistoryResponse> histories;

    public CustomerReturnResponse() {}

    public CustomerReturnResponse(Integer id, Integer orderId, double returnShippingFee, double totalRefundAmount, String status, String statusDisplayName) {
        this.id = id; this.orderId = orderId; this.returnShippingFee = returnShippingFee; this.totalRefundAmount = totalRefundAmount; this.status = status; this.statusDisplayName = statusDisplayName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public double getReturnShippingFee() { return returnShippingFee; }
    public void setReturnShippingFee(double returnShippingFee) { this.returnShippingFee = returnShippingFee; }
    public double getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(double totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }
    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
    public List<String> getProofImages() { return proofImages; }
    public void setProofImages(List<String> proofImages) { this.proofImages = proofImages; }
    public List<CustomerReturnItemResponse> getItems() { return items; }
    public void setItems(List<CustomerReturnItemResponse> items) { this.items = items; }
    public List<CustomerReturnHistoryResponse> getHistories() { return histories; }
    public void setHistories(List<CustomerReturnHistoryResponse> histories) { this.histories = histories; }
}