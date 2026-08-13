package com.example.new_toy_store.promotion.api;

import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/promotions")
@Validated
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id,
            @Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotion(@PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromotionResponse> getPromotionByCode(
            @PathVariable @NotBlank(message = "Mã khuyến mãi không được để trống") String code) {
        return ResponseEntity.ok(promotionService.getPromotionByCode(code));
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponse>> getPromotions(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(promotionService.getPromotions(scope, active, keyword, pageable));
    }

    @GetMapping("/available-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PromotionResponse>> getAvailableOrderPromotions(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Tổng tiền giỏ hàng không được âm") double cartTotal) {
        return ResponseEntity.ok(promotionService.getAvailableOrderPromotions(cartTotal));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePromotion(
            @PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id) {
        promotionService.activatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromotion(
            @PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(
            @PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/consume")
    public ResponseEntity<Void> consumePromotion(@RequestParam @NotBlank(message = "Mã khuyến mãi không được để trống") String promoCode) {
        promotionService.consumePromotion(promoCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releasePromotion(@RequestParam @NotBlank(message = "Mã khuyến mãi không được để trống") String promoCode) {
        promotionService.releasePromotion(promoCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calculate-product")
    public ResponseEntity<Double> calculateProductDiscount(
            @RequestParam @NotNull(message = "ID sản phẩm không được để trống") Integer productId,
            @RequestParam @Min(value = 0, message = "Giá gốc sản phẩm không được âm") double originalPrice) {
        double discount = promotionService.calculateProductDiscount(productId, originalPrice);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/calculate-order")
    public ResponseEntity<Double> calculateOrderDiscount(
            @RequestParam @NotBlank(message = "Mã khuyến mãi không được để trống") String promoCode,
            @RequestParam @Min(value = 0, message = "Tổng tiền giỏ hàng không được âm") double cartTotal) {
        double discount = promotionService.calculateOrderDiscount(promoCode, cartTotal);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/calculate-shipping")
    public ResponseEntity<Double> calculateShippingDiscount(
            @RequestParam @NotBlank(message = "Mã khuyến mãi không được để trống") String promoCode,
            @RequestParam @Min(value = 0, message = "Phí vận chuyển hiện tại không được âm") double currentShippingFee,
            @RequestParam @Min(value = 0, message = "Tổng tiền giỏ hàng không được âm") double cartTotal) {
        double discount = promotionService.calculateShippingDiscount(promoCode, currentShippingFee, cartTotal);
        return ResponseEntity.ok(discount);
    }

    @PostMapping("/active-for-products")
    public ResponseEntity<List<PromotionResponse>> getActivePromotionsForProducts(
            @RequestBody @NotEmpty(message = "Danh sách ID sản phẩm không được để trống") Set<Integer> productIds) {
        List<PromotionResponse> activePromotions = promotionService.getActivePromotionsForProducts(productIds);
        return ResponseEntity.ok(activePromotions);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleLogicExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("Lỗi", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
