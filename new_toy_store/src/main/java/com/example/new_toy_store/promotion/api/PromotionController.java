package com.example.new_toy_store.promotion.api;

import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/promotions")
@Validated
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

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromotion(
            @PathVariable @Min(value = 1, message = "ID khuyến mãi không hợp lệ") Integer id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
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

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleLogicExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("Lỗi", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}