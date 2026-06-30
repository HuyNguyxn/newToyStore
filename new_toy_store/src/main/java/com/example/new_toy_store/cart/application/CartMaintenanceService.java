package com.example.new_toy_store.cart.application;

import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.domain.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartMaintenanceService {

    private final CartItemRepository itemRepository;

    public CartMaintenanceService(CartItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void executeLifecycle() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime day23Start = now.minusDays(23).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime day23End = day23Start.plusDays(1);
        List<CartItem> items7DaysLeft = itemRepository.findByUpdatedAtBetween(day23Start, day23End);
        notifyUsers(items7DaysLeft, 7);

        LocalDateTime day29Start = now.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime day29End = day29Start.plusDays(1);
        List<CartItem> items1DayLeft = itemRepository.findByUpdatedAtBetween(day29Start, day29End);
        notifyUsers(items1DayLeft, 1);

        LocalDateTime expirationThreshold = now.minusDays(30);
        itemRepository.deleteExpiredItems(expirationThreshold);
    }

    private void notifyUsers(List<CartItem> items, int daysLeft) {
        for (CartItem item : items) {
            Integer userId = item.getCart().getUserId();
            Integer productId = item.getProductId();

            System.out.println("Gửi thông báo tới User " + userId + ": Sản phẩm ID " + productId + " sẽ bị xóa khỏi giỏ hàng sau " + daysLeft + " ngày.");
        }
    }
}