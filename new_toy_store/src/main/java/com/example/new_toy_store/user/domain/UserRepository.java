package com.example.new_toy_store.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = "SELECT id FROM users WHERE email LIKE CONCAT(:email, '\\_deleted\\_%')", nativeQuery = true)
    List<Integer> findSoftDeletedUserIdsByEmailPattern(@Param("email") String email);

    @Query(value = "SELECT status FROM users WHERE email LIKE CONCAT(:email, '\\_deleted\\_%')", nativeQuery = true)
    List<String> findStatusesOfSoftDeletedUsersByEmailPattern(@Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM addresses WHERE user_id IN :userIds", nativeQuery = true)
    void hardDeleteAddressesByUserIds(@Param("userIds") List<Integer> userIds);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM users WHERE id IN :userIds", nativeQuery = true)
    void hardDeleteUsersByIds(@Param("userIds") List<Integer> userIds);
}
