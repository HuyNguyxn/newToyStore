package com.example.new_toy_store.user.domain;

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
            throw new IllegalArgumentException("Token value cannot be empty");
        }
        if (tokenType == null) {
            throw new IllegalArgumentException("Token type is required");
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