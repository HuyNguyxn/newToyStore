package com.example.new_toy_store.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer>, JpaSpecificationExecutor<Notification> {

    boolean existsByDeduplicationKey(String deduplicationKey);

    long countByRecipientUserIdAndStatus(Integer recipientUserId, NotificationStatus status);

    @Query("""
            select count(n)
              from Notification n
             where n.recipientUserId = :userId
               and n.status = :status
               and (n.expiresAt is null or n.expiresAt > :now)
            """)
    long countUnreadNotExpired(
            @Param("userId") Integer userId,
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
               set n.status = :readStatus,
                   n.readAt = :readAt,
                   n.version = n.version + 1
             where n.recipientUserId = :userId
               and n.status = :unreadStatus
            """)
    int markAllAsRead(
            @Param("userId") Integer userId,
            @Param("unreadStatus") NotificationStatus unreadStatus,
            @Param("readStatus") NotificationStatus readStatus,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
               set n.deletedAt = :deletedAt,
                   n.version = n.version + 1
             where n.expiresAt is not null
               and n.expiresAt < :deletedAt
            """)
    int softDeleteExpired(@Param("deletedAt") LocalDateTime deletedAt);
}
