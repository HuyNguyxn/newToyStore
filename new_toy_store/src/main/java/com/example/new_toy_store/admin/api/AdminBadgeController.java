package com.example.new_toy_store.admin.api;

import com.example.new_toy_store.admin.application.dto.response.AdminMenuBadgeResponse;
import com.example.new_toy_store.customer_return.domain.CustomerReturnRepository;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.product.domain.InventoryRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/menu-badges")
public class AdminBadgeController {

    private final OrderRepository orderRepository;
    private final CustomerReturnRepository customerReturnRepository;
    private final SupplierReturnRepository supplierReturnRepository;
    private final InventoryRepository inventoryRepository;

    public AdminBadgeController(
            OrderRepository orderRepository,
            CustomerReturnRepository customerReturnRepository,
            SupplierReturnRepository supplierReturnRepository,
            InventoryRepository inventoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerReturnRepository = customerReturnRepository;
        this.supplierReturnRepository = supplierReturnRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MANAGER')")
    public AdminMenuBadgeResponse getMenuBadges() {
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long pendingCustomerReturns = customerReturnRepository.countByStatus(CustomerReturnStatus.REQUESTED);
        long pendingSupplierReturns = supplierReturnRepository.countByStatus(SupplierReturnStatus.PENDING_APPROVAL);
        long lowStockVariants = inventoryRepository.countLowStock(5);

        return new AdminMenuBadgeResponse(
                pendingOrders,
                pendingCustomerReturns,
                pendingSupplierReturns,
                lowStockVariants
        );
    }
}
