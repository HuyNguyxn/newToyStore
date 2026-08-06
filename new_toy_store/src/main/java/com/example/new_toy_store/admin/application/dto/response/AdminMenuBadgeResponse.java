package com.example.new_toy_store.admin.application.dto.response;

public class AdminMenuBadgeResponse {

    private long pendingOrders;
    private long pendingCustomerReturns;
    private long pendingSupplierReturns;
    private long lowStockVariants;

    public AdminMenuBadgeResponse() {}

    public AdminMenuBadgeResponse(long pendingOrders, long pendingCustomerReturns, long pendingSupplierReturns, long lowStockVariants) {
        this.pendingOrders = pendingOrders;
        this.pendingCustomerReturns = pendingCustomerReturns;
        this.pendingSupplierReturns = pendingSupplierReturns;
        this.lowStockVariants = lowStockVariants;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getPendingCustomerReturns() {
        return pendingCustomerReturns;
    }

    public void setPendingCustomerReturns(long pendingCustomerReturns) {
        this.pendingCustomerReturns = pendingCustomerReturns;
    }

    public long getPendingSupplierReturns() {
        return pendingSupplierReturns;
    }

    public void setPendingSupplierReturns(long pendingSupplierReturns) {
        this.pendingSupplierReturns = pendingSupplierReturns;
    }

    public long getLowStockVariants() {
        return lowStockVariants;
    }

    public void setLowStockVariants(long lowStockVariants) {
        this.lowStockVariants = lowStockVariants;
    }
}
