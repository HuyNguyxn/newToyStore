package com.example.new_toy_store.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(Integer userId);
    List<Cart> findByUpdatedAtBefore(LocalDateTime dateTime);
}