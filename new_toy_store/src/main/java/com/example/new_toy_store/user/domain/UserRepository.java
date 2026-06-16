package com.example.new_toy_store.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.id = :id")
    Optional<User> findByIdWithAddresses(@Param("id") Integer id);

    @Query(value = "SELECT id FROM users WHERE email LIKE CONCAT(:email, '\\_deleted\\_%')", nativeQuery = true)
    List<Integer> findSoftDeletedUserIdsByEmailPattern(@Param("email") String email);

    @Query(value = "SELECT status FROM users WHERE email LIKE CONCAT(:email, '\\_deleted\\_%')", nativeQuery = true)
    List<String> findStatusesOfSoftDeletedUsersByEmailPattern(@Param("email") String email);

    @Modifying
    @Query(value = "DELETE FROM addresses WHERE user_id IN :userIds", nativeQuery = true)
    void hardDeleteAddressesByUserIds(@Param("userIds") List<Integer> userIds);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id IN :userIds", nativeQuery = true)
    void hardDeleteUsersByIds(@Param("userIds") List<Integer> userIds);
}