package com.example.new_toy_store.product.application.dto.response;

public class ProductEnumOptionResponse {

    private final String code;
    private final String name;
    private final String displayName;
    private final Boolean visible;
    private final Boolean purchasable;
    private final Boolean canAddAttributes;

    public ProductEnumOptionResponse(
            String code,
            String name,
            String displayName,
            Boolean visible,
            Boolean purchasable,
            Boolean canAddAttributes
    ) {
        this.code = code;
        this.name = name;
        this.displayName = displayName;
        this.visible = visible;
        this.purchasable = purchasable;
        this.canAddAttributes = canAddAttributes;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public Boolean getVisible() { return visible; }
    public Boolean getPurchasable() { return purchasable; }
    public Boolean getCanAddAttributes() { return canAddAttributes; }
}
