package com.example.new_toy_store.promotion.application.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class PromotionRequest {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã khuyến mãi chỉ chứa chữ hoa, số và dấu gạch dưới")
    @Size(max = 50, message = "Mã khuyến mãi không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 255, message = "Tên chương trình không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Loại khuyến mãi không được để trống")
    private String type;

    @NotBlank(message = "Phạm vi áp dụng không được để trống")
    private String scope;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @Min(value = 0, message = "Giá trị giảm không được âm")
    private Double discountValue;

    @Min(value = 0, message = "Số tiền giảm tối đa không được âm")
    private Double maxDiscountAmount;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu không được âm")
    private Double minOrderValue;

    @Min(value = 1, message = "ID sản phẩm không hợp lệ")
    private Integer targetProductId;

    @Min(value = 1, message = "Giới hạn lượt sử dụng tối thiểu phải từ 1 lượt trở lên")
    private Integer usageLimit;

    private LocalDateTime startDate;

    @Future(message = "Ngày kết thúc phải ở trong tương lai")
    private LocalDateTime endDate;

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getScope() { return scope; }
    public Double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public Double getMinOrderValue() { return minOrderValue; }
    public Integer getTargetProductId() { return targetProductId; }
    public Integer getUsageLimit() { return usageLimit; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
}