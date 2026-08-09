package com.example.new_toy_store.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByRole(UserRole role);

    @Query("""
            SELECT COUNT(u)
              FROM User u
             WHERE u.status = :status
               AND u.createdAt >= :from
               AND u.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countByStatusBetween(@Param("status") UserStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(u)
              FROM User u
             WHERE u.createdAt >= :from
               AND u.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @EntityGraph(attributePaths = "addresses")
    Optional<User> findByEmail(String email);

    List<User> findAllByStatus(UserStatus status);

    @EntityGraph(attributePaths = "addresses")
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithAddresses(@Param("id") Integer id);

    @Override
    @EntityGraph(attributePaths = "addresses")
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Query(value = """
            SELECT id
              FROM users
             WHERE deleted_at IS NOT NULL
               AND REGEXP_REPLACE(email, '_deleted_[0-9]+$', '') = :email
             LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> findSoftDeletedUserIdByOriginalEmail(@Param("email") String email);

    @Query(value = """
            SELECT id AS id,
                   REGEXP_REPLACE(email, '_deleted_[0-9]+$', '') AS email,
                   full_name AS fullName,
                   phone_number AS phoneNumber,
                   role AS role,
                   status AS status,
                   created_at AS createdAt,
                   updated_at AS updatedAt,
                   deleted_at AS deletedAt
              FROM users
             WHERE deleted_at IS NOT NULL
             ORDER BY deleted_at DESC
            """, nativeQuery = true)
    List<DeletedUserProjection> findAllSoftDeletedUsers();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE users
               SET email = :email,
                   deleted_at = NULL,
                   updated_at = CURRENT_TIMESTAMP
             WHERE id = :userId
               AND deleted_at IS NOT NULL
            """, nativeQuery = true)
    int restoreSoftDeletedUser(@Param("userId") Integer userId, @Param("email") String email);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE addresses
               SET deleted_at = NULL,
                   updated_at = CURRENT_TIMESTAMP
             WHERE user_id = :userId
               AND deleted_at >= :deletedAt
            """, nativeQuery = true)
    void restoreSoftDeletedAddresses(
            @Param("userId") Integer userId,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}
