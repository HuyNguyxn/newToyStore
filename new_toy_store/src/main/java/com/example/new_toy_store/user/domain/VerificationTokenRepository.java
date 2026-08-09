package com.example.new_toy_store.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByTokenValue(String tokenValue);
    void deleteByUser_IdAndTokenType(Integer userId, TokenType tokenType);
    void deleteByUser_Id(Integer userId);
}
