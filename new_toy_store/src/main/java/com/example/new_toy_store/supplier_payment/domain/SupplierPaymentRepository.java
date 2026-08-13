package com.example.new_toy_store.supplier_payment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPaymentInvoice, Integer>, JpaSpecificationExecutor<SupplierPaymentInvoice> {

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM SupplierPaymentInvoice i WHERE i.status IN :statuses")
    double sumOutstandingByStatuses(@Param("statuses") List<SupplierPaymentStatus> statuses);

    long countByStatusIn(List<SupplierPaymentStatus> statuses);

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM SupplierPaymentInvoice i WHERE i.status IN :statuses AND i.dueDate < :today")
    double sumOverdueOutstanding(@Param("statuses") List<SupplierPaymentStatus> statuses, @Param("today") LocalDate today);

    long countByStatusInAndDueDateBefore(List<SupplierPaymentStatus> statuses, LocalDate today);

    boolean existsByImportNoteId(Integer importNoteId);

    Optional<SupplierPaymentInvoice> findByImportNoteId(Integer importNoteId);

    @EntityGraph(attributePaths = "transactions")
    @Query("SELECT i FROM SupplierPaymentInvoice i WHERE i.id = :id")
    Optional<SupplierPaymentInvoice> findByIdWithTransactions(@Param("id") Integer id);

    Page<SupplierPaymentInvoice> findAll(Specification<SupplierPaymentInvoice> specification, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SupplierPaymentInvoice i
               SET i.status = :status,
                   i.note = :reason,
                   i.updatedAt = CURRENT_TIMESTAMP,
                   i.version = i.version + 1
             WHERE i.id = :id
               AND i.version = :version
               AND i.status IN (com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus.PENDING,
                                com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus.PARTIALLY_PAID,
                                com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus.OVERDUE)
            """)
    int cancelWithVersion(@Param("id") Integer id,
                          @Param("version") Long version,
                          @Param("status") SupplierPaymentStatus status,
                          @Param("reason") String reason);
}
