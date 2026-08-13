ALTER TABLE payment_transactions
    MODIFY COLUMN status ENUM(
        'CANCELLED',
        'EXPIRED',
        'FAILED',
        'PENDING',
        'PARTIALLY_REFUNDED',
        'REFUNDED',
        'REFUND_FAILED',
        'REFUND_PENDING',
        'SUCCEEDED'
    ) NOT NULL;
