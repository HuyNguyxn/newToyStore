package com.example.new_toy_store.user.application.dto.response;

public class UserEnumOptionResponse {

    private final String code;
    private final String name;
    private final String displayName;
    private final Boolean canLogin;
    private final Boolean canPlaceOrder;
    private final Boolean canModifyData;
    private final Boolean canManageProducts;
    private final Boolean canManageOrders;

    public UserEnumOptionResponse(
            String code,
            String name,
            String displayName,
            Boolean canLogin,
            Boolean canPlaceOrder,
            Boolean canModifyData,
            Boolean canManageProducts,
            Boolean canManageOrders
    ) {
        this.code = code;
        this.name = name;
        this.displayName = displayName;
        this.canLogin = canLogin;
        this.canPlaceOrder = canPlaceOrder;
        this.canModifyData = canModifyData;
        this.canManageProducts = canManageProducts;
        this.canManageOrders = canManageOrders;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public Boolean getCanLogin() { return canLogin; }
    public Boolean getCanPlaceOrder() { return canPlaceOrder; }
    public Boolean getCanModifyData() { return canModifyData; }
    public Boolean getCanManageProducts() { return canManageProducts; }
    public Boolean getCanManageOrders() { return canManageOrders; }
}
