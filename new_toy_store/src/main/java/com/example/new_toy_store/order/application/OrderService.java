package com.example.new_toy_store.order.application;

import com.example.new_toy_store.cart.application.CartService;
import com.example.new_toy_store.order.application.dto.request.OrderItemRequest;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.mapper.OrderMapper;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ProductService productService;
    private final CartService cartService;
    private final UserRepository userRepository;

    public OrderService(OrderRepository repository, ProductService productService,
                        CartService cartService, UserRepository userRepository) {
        this.repository = repository;
        this.productService = productService;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Integer id) {
        Order order = repository.findByIdWithItems(id);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng");
        }
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng"));

        if (!user.getStatus().canPlaceOrder()) {
            throw new IllegalStateException("Tài khoản của bạn hiện không đủ điều kiện để đặt hàng tại hệ thống.");
        }

        Set<Integer> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Order order = OrderMapper.toEntity(request);

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());

            if (product == null || !product.isAvailableForPurchase()) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh");
            }

            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(itemRequest.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu mã sản phẩm này"));

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
        }

        repository.save(order);
        cartService.clearCart(request.getUserId());

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Integer id, String note) {
        Order order = getOrder(id);
        order.confirm(note != null && !note.trim().isEmpty() ? note : "Đơn hàng đã được xác nhận");
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse ship(Integer id, String note) {
        Order order = getOrder(id);
        order.ship(note != null && !note.trim().isEmpty() ? note : "Đơn hàng đang được giao");
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse complete(Integer id, String note) {
        Order order = getOrder(id);
        order.complete(note != null && !note.trim().isEmpty() ? note : "Đơn hàng giao thành công");
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Integer id, String note) {
        Order order = getOrder(id);
        order.cancel(note != null && !note.trim().isEmpty() ? note : "Đơn hàng đã bị hủy");

        Set<Integer> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        order.getItems().forEach(item -> {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                ProductVariant variant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(item.getVariantId()))
                        .findFirst()
                        .orElse(null);

                if (variant != null) {
                    variant.getInventory().addStock(item.getQuantity());
                }
            }
        });

        repository.save(order);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Integer id) {
        Order order = getOrder(id);
        order.delete();
    }

    private Order getOrder(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
    }
}