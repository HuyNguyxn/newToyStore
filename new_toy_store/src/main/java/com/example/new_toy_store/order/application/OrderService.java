package com.example.new_toy_store.order.application;

import com.example.new_toy_store.global.event.OrderCancelledEvent;
import com.example.new_toy_store.global.event.OrderCancelledItemPayload;
import com.example.new_toy_store.global.event.OrderCreatedEvent;
import com.example.new_toy_store.global.event.OrderCreatedItemPayload;
import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.OrderSpecification;
import com.example.new_toy_store.order.application.dto.request.OrderFilterRequest;
import com.example.new_toy_store.order.application.dto.request.OrderItemRequest;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.request.UpdateShippingRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.order.domain.exception.InsufficientStockException;
import com.example.new_toy_store.order.domain.exception.InvalidOrderDataException;
import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import com.example.new_toy_store.order.domain.exception.OrderAccessDeniedException;
import com.example.new_toy_store.order.domain.exception.OrderNotFoundException;
import com.example.new_toy_store.order.mapper.OrderMapper;
import com.example.new_toy_store.product.application.service.ProductService;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final PromotionService promotionService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository repository,
            ProductService productService,
            UserRepository userRepository,
            PromotionService promotionService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.productService = productService;
        this.userRepository = userRepository;
        this.promotionService = promotionService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Integer id) {
        Order order = repository.findByIdWithItemsAndHistories(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new InvalidOrderDataException("userId", "Không tìm thấy thông tin khách hàng"));

        if (!user.getStatus().canPlaceOrder()) {
            throw new InvalidOrderOperationException(user.getStatus().getDisplayName(), "Tạo đơn hàng");
        }

        validatePromoCodeUsage(request);
        Map<Integer, Product> productMap = loadProductMap(request);
        validateOrderItems(request, productMap);

        Order order = OrderMapper.toEntity(request);
        addItemsAndReduceStock(order, request, productMap);
        applyPromotionIfPresent(order, request);

        repository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(),
                null,
                order.getUserId(),
                toOrderCreatedItemPayloads(order)
        ));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Integer id, String note) {
        Order order = getOrder(id);
        OrderStatus previousStatus = order.getStatus();
        String resolvedNote = resolveNote(note, "Đơn hàng đã được xác nhận");
        order.confirm(resolvedNote);
        publishStatusChanged(order, previousStatus, resolvedNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse ship(Integer id, String note) {
        Order order = getOrder(id);
        OrderStatus previousStatus = order.getStatus();
        String resolvedNote = resolveNote(note, "Đơn hàng đang được giao");
        order.ship(resolvedNote);
        publishStatusChanged(order, previousStatus, resolvedNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse complete(Integer id, String note) {
        Order order = getOrder(id);
        OrderStatus previousStatus = order.getStatus();
        String resolvedNote = resolveNote(note, "Đơn hàng giao thành công");
        order.complete(resolvedNote);
        publishStatusChanged(order, previousStatus, resolvedNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Integer id, String note) {
        Order order = getOrder(id);
        OrderStatus previousStatus = order.getStatus();
        String resolvedNote = resolveNote(note, "Đơn hàng đã bị hủy");
        order.cancel(resolvedNote);
        restoreStockForOrder(order);

        repository.save(order);
        publishStatusChanged(order, previousStatus, resolvedNote);
        eventPublisher.publishEvent(new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                resolvedNote,
                toOrderCancelledItemPayloads(order)
        ));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Integer id) {
        Order order = getOrder(id);
        if (!order.getStatus().canBeDeleted()) {
            throw new InvalidOrderOperationException(order.getStatus().getDisplayName(), "Xóa đơn hàng");
        }

        repository.softDeleteItemsByOrderId(id);
        repository.softDeleteHistoriesByOrderId(id);
        int updatedRows = repository.softDeleteOrderWithVersion(
                id,
                order.getVersion(),
                List.of(OrderStatus.PENDING, OrderStatus.CANCELLED)
        );

        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Order.class, id);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasCompletedOrder(Integer userId, Integer productId) {
        return repository.existsCompletedOrderByUserAndProduct(userId, productId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> filterOrders(OrderFilterRequest filterRequest, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.filter(filterRequest);
        return repository.findAll(spec, pageable).map(OrderMapper::toResponse);
    }

    @Transactional
    public OrderResponse updateShippingAddress(Integer id, UpdateShippingRequest request, Integer currentUserId, boolean isAdmin) {
        Order order = getOrder(id);

        if (!order.getUserId().equals(currentUserId) && !isAdmin) {
            throw new OrderAccessDeniedException(id, currentUserId, "chỉnh sửa địa chỉ giao hàng của");
        }

        order.updateShippingAddress(request.getNewAddress(), request.getNote());
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderItem getCompletedOrderItemForReview(Integer orderItemId, Integer userId) {
        return repository.findCompletedOrderItem(orderItemId, userId)
                .orElseThrow(() -> new IllegalStateException("Thao tác bị từ chối: sản phẩm này chưa được giao thành công hoặc không thuộc về đơn hàng của bạn."));
    }

    @Transactional(readOnly = true)
    public String getOrderStatus(Integer orderId) {
        Order order = getOrder(orderId);
        return order.getStatus().name();
    }

    @Transactional
    public void updateOrderRefundStatus(Integer orderId, Map<Integer, Integer> returnedItemsQty) {
        Order order = getOrder(orderId);
        OrderStatus previousStatus = order.getStatus();

        int totalOriginalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        int totalReturnedItems = returnedItemsQty.values().stream().mapToInt(Integer::intValue).sum();

        String note = "Hệ thống tự động cập nhật từ yêu cầu trả hàng";

        if (totalReturnedItems >= totalOriginalItems) {
            order.refundFully(note);
        } else {
            order.refundPartially(note);
        }

        repository.save(order);
        publishStatusChanged(order, previousStatus, note);
    }

    @Transactional(readOnly = true)
    public void verifyOrderOwnership(Integer orderId, Integer userId) {
        Order order = getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderAccessDeniedException(orderId, userId, "truy cập hoặc thao tác trên");
        }
    }

    @Transactional(readOnly = true)
    public boolean isHighRiskCustomer(Integer userId) {
        long totalOrders = repository.countTotalValidOrders(userId);
        if (totalOrders < 3) return false;
        long refundedOrders = repository.countRefundedOrders(userId);
        double returnRate = (double) refundedOrders / totalOrders;
        return returnRate >= 0.3;
    }

    private Order getOrder(Integer id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void validatePromoCodeUsage(OrderRequest request) {
        if (request.getPromoCode() == null || request.getPromoCode().trim().isEmpty()) {
            return;
        }

        String sanitizedCode = request.getPromoCode().toUpperCase().trim();
        boolean isPromoUsed = repository.existsByUserIdAndPromoCodeAndStatusNot(
                request.getUserId(), sanitizedCode, OrderStatus.CANCELLED);

        if (isPromoUsed) {
            throw new InvalidOrderDataException("promoCode", "Bạn đã sử dụng mã khuyến mãi '" + sanitizedCode + "' cho một đơn hàng khác.");
        }
    }

    private Map<Integer, Product> loadProductMap(OrderRequest request) {
        Set<Integer> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toSet());

        return productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    private void validateOrderItems(OrderRequest request, Map<Integer, Product> productMap) {
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            if (product == null || !product.isAvailableForPurchase()) {
                throw new InvalidOrderDataException("productId", "Sản phẩm không tồn tại hoặc đã ngừng kinh doanh");
            }

            ProductVariant variant = findVariant(product, itemRequest.getVariantId());
            if (variant.getInventory().getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        product.getId(), product.getName(),
                        itemRequest.getQuantity(), variant.getInventory().getStockQuantity()
                );
            }
        }
    }

    private void addItemsAndReduceStock(Order order, OrderRequest request, Map<Integer, Product> productMap) {
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            ProductVariant variant = findVariant(product, itemRequest.getVariantId());

            String snapshot = variant.generateAttributesSnapshot();
            variant.getInventory().reduceStock(itemRequest.getQuantity());

            order.addItem(
                    product.getId(), variant.getId(), product.getName(),
                    snapshot, itemRequest.getQuantity(), variant.getPrice()
            );
        }
    }

    private ProductVariant findVariant(Product product, Integer variantId) {
        return product.getVariants().stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new InvalidOrderDataException("variantId", "Không tìm thấy mẫu mã sản phẩm này"));
    }

    private void applyPromotionIfPresent(Order order, OrderRequest request) {
        if (request.getPromoCode() == null || request.getPromoCode().trim().isEmpty()) {
            return;
        }

        try {
            double rawTotal = order.getTotalAmount();
            double discount = promotionService.calculateOrderDiscount(request.getPromoCode(), rawTotal);
            if (discount > 0) {
                order.applyPromoCode(request.getPromoCode().toUpperCase().trim(), discount);
            }
        } catch (RuntimeException ex) {
            throw new InvalidOrderDataException("promoCode", "Lỗi mã khuyến mãi: " + ex.getMessage());
        }
    }

    private void restoreStockForOrder(Order order) {
        Set<Integer> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        order.getItems().forEach(item -> {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                return;
            }

            product.getVariants().stream()
                    .filter(variant -> variant.getId().equals(item.getVariantId()))
                    .findFirst()
                    .ifPresent(variant -> variant.getInventory().addStock(item.getQuantity()));
        });
    }

    private void publishStatusChanged(Order order, OrderStatus previousStatus, String note) {
        if (previousStatus != order.getStatus()) {
            eventPublisher.publishEvent(OrderStatusChangedEvent.now(
                    order.getId(),
                    order.getUserId(),
                    previousStatus,
                    order.getStatus(),
                    note
            ));
        }
    }

    private String resolveNote(String note, String defaultNote) {
        return note != null && !note.trim().isEmpty() ? note : defaultNote;
    }

    private List<OrderCreatedItemPayload> toOrderCreatedItemPayloads(Order order) {
        return order.getItems().stream()
                .map(item -> new OrderCreatedItemPayload(item.getVariantId(), item.getQuantity()))
                .toList();
    }

    private List<OrderCancelledItemPayload> toOrderCancelledItemPayloads(Order order) {
        return order.getItems().stream()
                .map(item -> new OrderCancelledItemPayload(item.getVariantId(), item.getQuantity()))
                .toList();
    }
}
