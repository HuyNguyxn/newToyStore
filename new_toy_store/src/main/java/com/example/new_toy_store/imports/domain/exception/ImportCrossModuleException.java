package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

public class ImportCrossModuleException extends ImportDomainException {

    private ImportCrossModuleException(String errorCode, String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, contextData);
    }

    public static ImportCrossModuleException invalidSupplier(Integer supplierId, String reason) {
        return new ImportCrossModuleException(
                "IMPORT_INVALID_SUPPLIER_REFERENCE",
                "Dữ liệu nhà cung cấp gửi sang luồng nhập kho không hợp lệ.",
                Map.of(
                        "supplierId", supplierId,
                        "module", "supplier",
                        "reason", reason
                )
        );
    }

    public static ImportCrossModuleException invalidProduct(Integer productId, Integer variantId, String reason) {
        return new ImportCrossModuleException(
                "IMPORT_INVALID_PRODUCT_REFERENCE",
                "Dữ liệu sản phẩm hoặc biến thể gửi sang luồng nhập kho không hợp lệ.",
                Map.of(
                        "productId", productId,
                        "variantId", variantId,
                        "module", "product",
                        "reason", reason
                )
        );
    }

    public static ImportCrossModuleException missingProducts(Set<Integer> requestedProductIds, Set<Integer> foundProductIds) {
        return new ImportCrossModuleException(
                "IMPORT_PRODUCT_REFERENCES_NOT_FOUND",
                "Một hoặc nhiều sản phẩm gửi sang luồng nhập kho không tồn tại.",
                Map.of(
                        "requestedProductIds", requestedProductIds,
                        "foundProductIds", foundProductIds,
                        "module", "product",
                        "reason", "PRODUCT_NOT_FOUND"
                )
        );
    }
}
