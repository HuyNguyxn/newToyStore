package com.example.new_toy_store.order.application;

import com.example.new_toy_store.cart.application.CartService;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.mapper.OrderMapper;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ProductService productService;
    private final CartService cartService;
    public OrderService(OrderRepository repository, ProductService productService, CartService cartService) {
        this.repository = repository;
        this.productService = productService;
        this.cartService = cartService;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable)
                .map(OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Integer id) {
        Order order = repository.findByIdWithItems(id);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = OrderMapper.toEntity(request);

        request.getItems().forEach(itemRequest -> {
            Product product = productService.getProductEntity(itemRequest.getProductId());
            if (!product.isAvailableForPurchase()) {
                throw new RuntimeException("Sản phẩm không sẵn sàng để đặt hàng");
            }
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(itemRequest.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            String snapshot = variant.generateAttributesSnapshot();

            variant.getInventory().reduceStock(itemRequest.getQuantity());

            order.addItem(
                    product.getId(),
                    variant.getId(),
                    product.getName(),
                    snapshot,
                    itemRequest.getQuantity(),
                    variant.getPrice()
            );
        });

        repository.save(order);

        cartService.clearCart(request.getUserId());

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Integer id, String note) {
        Order order = getOrder(id);
        String finalNote = (note != null && !note.trim().isEmpty()) ? note : "Đơn hàng đã được xác nhận";
        order.confirm(finalNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse ship(Integer id, String note) {
        Order order = getOrder(id);
        String finalNote = (note != null && !note.trim().isEmpty()) ? note : "Đơn hàng đang được giao cho đơn vị vận chuyển";
        order.ship(finalNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse complete(Integer id, String note) {
        Order order = getOrder(id);
        String finalNote = (note != null && !note.trim().isEmpty()) ? note : "Đơn hàng giao thành công";
        order.complete(finalNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Integer id, String note) {
        Order order = getOrder(id);
        String finalNote = (note != null && !note.trim().isEmpty()) ? note : "Đơn hàng đã bị hủy";
        order.cancel(finalNote);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Integer id) {
        Order order = getOrder(id);
        order.delete();
    }

    private Order getOrder(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}