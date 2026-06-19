package com.example.new_toy_store.supplier.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Supplier> findByPhoneNumber(String phoneNumber);
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
}