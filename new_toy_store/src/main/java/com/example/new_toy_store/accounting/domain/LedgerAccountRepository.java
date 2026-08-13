package com.example.new_toy_store.accounting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Integer> {
    Optional<LedgerAccount> findByCodeAndActiveTrue(String code);
    List<LedgerAccount> findAllByActiveTrueOrderByCodeAsc();
}
