package com.example.new_toy_store.user.domain;

import com.example.new_toy_store.user.domain.exception.InvalidUserOperationException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_verification_token", columnNames = {"tokenValue"})
        },
        indexes = {
                @Index(name = "idx_token_value", columnList = "tokenValue", unique = true),
                @Index(name = "idx_token_user_id", columnList = "user_id")
        }
)
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "token_type")
    private TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected VerificationToken() {}

    public VerificationToken(String tokenValue, TokenType tokenType, User user) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("tokenValue", "Giá trị token không được để trống");
        }
        if (tokenType == null) {
            throw InvalidUserOperationException.inputDataInvalid("tokenType", "Loại token không được để trống");
        }
        this.tokenValue = tokenValue;
        this.tokenType = tokenType;
        this.expiryDate = LocalDateTime.now().plusMinutes(tokenType.getExpirationMinutes());
        this.user = user;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public Integer getId() { return id; }
    public String getTokenValue() { return tokenValue; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public TokenType getTokenType() { return tokenType; }
    public User getUser() { return user; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof VerificationToken u && id != null && id.equals(u.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
