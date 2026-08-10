package com.example.new_toy_store.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByTokenValue(String tokenValue);
    void deleteByUser_IdAndTokenType(Integer userId, TokenType tokenType);
    void deleteByUser_Id(Integer userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM verification_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserIdNative(@Param("userId") Integer userId);
}
