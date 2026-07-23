package com.example.new_toy_store.supplier.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer>, JpaSpecificationExecutor<Supplier> {

    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Supplier> findByPhoneNumber(String phoneNumber);
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query(value = "SELECT * FROM suppliers WHERE phone_number = :phoneNumber", nativeQuery = true)
    Optional<Supplier> findByPhoneNumberIncludingDeleted(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT * FROM suppliers WHERE id = :id", nativeQuery = true)
    Optional<Supplier> findByIdIncludingDeleted(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Supplier s
               SET s.status = :status,
                   s.version = s.version + 1,
                   s.updatedAt = CURRENT_TIMESTAMP
             WHERE s.id = :id
               AND s.version = :version
            """)
    int updateStatusWithVersion(
            @Param("id") Integer id,
            @Param("status") SupplierStatus status,
            @Param("version") Long version
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Supplier s
               SET s.deletedAt = CURRENT_TIMESTAMP,
                   s.version = s.version + 1,
                   s.updatedAt = CURRENT_TIMESTAMP
             WHERE s.id = :id
               AND s.version = :version
            """)
    int softDeleteWithVersion(
            @Param("id") Integer id,
            @Param("version") Long version
    );
}
