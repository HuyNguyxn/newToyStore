package com.example.new_toy_store.accounting.application.reconciliation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AccountingReconciliationSourceReader {
    private final JdbcTemplate jdbcTemplate;

    public AccountingReconciliationSourceReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CustomerPaymentSource> findMissingCustomerPayments() {
        return jdbcTemplate.query("""
                SELECT p.id, p.order_id, p.amount, p.method,
                       DATE(COALESCE(p.paid_at, p.updated_at, p.created_at, CURRENT_TIMESTAMP)) AS entry_date
                  FROM payment_transactions p
                 WHERE p.deleted_at IS NULL
                   AND p.status IN ('SUCCEEDED', 'PARTIALLY_REFUNDED', 'REFUNDED', 'REFUND_PENDING', 'REFUND_FAILED')
                   AND NOT EXISTS (
                       SELECT 1 FROM journal_entries j
                        WHERE j.source_type = 'CUSTOMER_PAYMENT'
                          AND j.source_reference = CONCAT('PAYMENT-', p.id)
                   )
                 ORDER BY p.id
                """, (rs, rowNum) -> new CustomerPaymentSource(
                rs.getInt("id"), rs.getInt("order_id"), round(rs.getDouble("amount")),
                rs.getString("method"), toLocalDate(rs.getDate("entry_date"))
        ));
    }

    public List<OrderCompletionSource> findMissingCompletedOrders() {
        return jdbcTemplate.query("""
                SELECT o.id, o.total_amount,
                       COALESCE(SUM(oi.quantity * COALESCE(NULLIF(oi.cost_price_snapshot, 0), pv.cost_price, 0)), 0) AS cost_amount,
                       DATE(COALESCE((SELECT MIN(completed.created_at)
                                        FROM order_histories completed
                                       WHERE completed.order_id = o.id
                                         AND completed.status = 'COMPLETED'
                                         AND completed.deleted_at IS NULL),
                                     o.updated_at, o.created_at, CURRENT_TIMESTAMP)) AS entry_date
                  FROM orders o
             LEFT JOIN order_items oi ON oi.order_id = o.id AND oi.deleted_at IS NULL
             LEFT JOIN product_variants pv ON pv.id = oi.variant_id
                 WHERE o.deleted_at IS NULL
                   AND o.status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED')
                   AND EXISTS (SELECT 1 FROM order_histories completed
                                WHERE completed.order_id = o.id
                                  AND completed.status = 'COMPLETED'
                                  AND completed.deleted_at IS NULL)
                   AND NOT EXISTS (
                       SELECT 1 FROM journal_entries j
                        WHERE j.source_type = 'ORDER_COMPLETION'
                          AND j.source_reference = CONCAT('ORDER-COMPLETION-', o.id)
                   )
                 GROUP BY o.id, o.total_amount, o.updated_at, o.created_at
                 ORDER BY o.id
                """, (rs, rowNum) -> new OrderCompletionSource(
                rs.getInt("id"), round(rs.getDouble("total_amount")),
                round(rs.getDouble("cost_amount")), toLocalDate(rs.getDate("entry_date"))
        ));
    }

    public List<CustomerRefundSource> findMissingCustomerRefunds() {
        return jdbcTemplate.query("""
                SELECT r.id, r.order_id, r.amount, r.method, o.status AS order_status,
                       DATE(COALESCE(r.completed_at, r.updated_at, r.created_at, CURRENT_TIMESTAMP)) AS entry_date
                  FROM payment_refunds r
                  JOIN orders o ON o.id = r.order_id
                 WHERE r.deleted_at IS NULL
                   AND r.status = 'SUCCEEDED'
                   AND NOT EXISTS (
                       SELECT 1 FROM journal_entries j
                        WHERE j.source_type = 'CUSTOMER_REFUND'
                          AND j.source_reference = CONCAT('REFUND-', r.id)
                   )
                 ORDER BY r.id
                """, (rs, rowNum) -> new CustomerRefundSource(
                rs.getInt("id"), rs.getInt("order_id"), round(rs.getDouble("amount")),
                rs.getString("method"), rs.getString("order_status"),
                toLocalDate(rs.getDate("entry_date"))
        ));
    }

    public List<ImportReceiptSource> findMissingImportReceipts() {
        return jdbcTemplate.query("""
                SELECT n.id, n.supplier_id,
                       COALESCE(SUM(i.quantity * i.import_price), n.total_amount, 0) AS receipt_amount,
                       DATE(COALESCE(n.updated_at, n.created_at, CURRENT_TIMESTAMP)) AS entry_date
                  FROM import_notes n
             LEFT JOIN import_note_items i ON i.import_note_id = n.id
                 WHERE n.deleted_at IS NULL
                   AND n.status = 'COMPLETED'
                   AND NOT EXISTS (
                       SELECT 1 FROM journal_entries j
                        WHERE j.source_type = 'IMPORT_RECEIPT'
                          AND j.source_reference = CONCAT('IMPORT-', n.id)
                   )
                 GROUP BY n.id, n.supplier_id, n.total_amount, n.updated_at, n.created_at
                 ORDER BY n.id
                """, (rs, rowNum) -> new ImportReceiptSource(
                rs.getInt("id"), rs.getInt("supplier_id"), round(rs.getDouble("receipt_amount")),
                toLocalDate(rs.getDate("entry_date"))
        ));
    }

    public List<SupplierPaymentSource> findMissingSupplierPayments() {
        return jdbcTemplate.query("""
                SELECT t.id, i.supplier_id, i.import_note_id, t.amount, t.method, t.paid_date
                  FROM supplier_payment_transactions t
                  JOIN supplier_payment_invoices i ON i.id = t.invoice_id
                 WHERE t.deleted_at IS NULL
                   AND i.deleted_at IS NULL
                   AND NOT EXISTS (
                       SELECT 1 FROM journal_entries j
                        WHERE j.source_type = 'SUPPLIER_PAYMENT'
                          AND j.source_reference = CONCAT('SUPPLIER-PAYMENT-', t.id)
                   )
                 ORDER BY t.id
                """, (rs, rowNum) -> new SupplierPaymentSource(
                rs.getInt("id"), rs.getInt("supplier_id"), rs.getInt("import_note_id"),
                round(rs.getDouble("amount")), rs.getString("method"),
                toLocalDate(rs.getDate("paid_date"))
        ));
    }

    private static LocalDate toLocalDate(Date value) {
        return value == null ? LocalDate.now() : value.toLocalDate();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record CustomerPaymentSource(
            int id, int orderId, double amount, String method, LocalDate entryDate) {}

    public record OrderCompletionSource(
            int id, double revenueAmount, double costAmount, LocalDate entryDate) {}

    public record CustomerRefundSource(
            int id, int orderId, double amount, String method, String orderStatus, LocalDate entryDate) {}

    public record ImportReceiptSource(
            int id, int supplierId, double amount, LocalDate entryDate) {}

    public record SupplierPaymentSource(
            int id, int supplierId, int importNoteId, double amount, String method, LocalDate entryDate) {}
}
