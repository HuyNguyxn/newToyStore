package com.example.new_toy_store.admin.application.dto.response;

public class AdminMenuBadgeResponse {

    private long pendingOrders;
    private long pendingCustomerReturns;
    private long pendingSupplierReturns;
    private long pendingImportNotes;
    private long lowStockVariants;
    private long pendingPayments;
    private long cancelledOrders;
    private long slowSellingProducts;
    private long lowRatingReviews;

    public AdminMenuBadgeResponse() {}

    public AdminMenuBadgeResponse(
            long pendingOrders,
            long pendingCustomerReturns,
            long pendingSupplierReturns,
            long pendingImportNotes,
            long lowStockVariants,
            long pendingPayments,
            long cancelledOrders,
            long slowSellingProducts,
            long lowRatingReviews
    ) {
        this.pendingOrders = pendingOrders;
        this.pendingCustomerReturns = pendingCustomerReturns;
        this.pendingSupplierReturns = pendingSupplierReturns;
        this.pendingImportNotes = pendingImportNotes;
        this.lowStockVariants = lowStockVariants;
        this.pendingPayments = pendingPayments;
        this.cancelledOrders = cancelledOrders;
        this.slowSellingProducts = slowSellingProducts;
        this.lowRatingReviews = lowRatingReviews;
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

    public long getPendingImportNotes() {
        return pendingImportNotes;
    }

    public void setPendingImportNotes(long pendingImportNotes) {
        this.pendingImportNotes = pendingImportNotes;
    }

    public long getLowStockVariants() {
        return lowStockVariants;
    }

    public void setLowStockVariants(long lowStockVariants) {
        this.lowStockVariants = lowStockVariants;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public long getSlowSellingProducts() {
        return slowSellingProducts;
    }

    public void setSlowSellingProducts(long slowSellingProducts) {
        this.slowSellingProducts = slowSellingProducts;
    }

    public long getLowRatingReviews() {
        return lowRatingReviews;
    }

    public void setLowRatingReviews(long lowRatingReviews) {
        this.lowRatingReviews = lowRatingReviews;
    }
}
