CREATE TABLE ledger_accounts (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    normal_balance VARCHAR(10) NOT NULL,
    liquid_account BIT(1) NOT NULL DEFAULT b'0',
    system_account BIT(1) NOT NULL DEFAULT b'0',
    active BIT(1) NOT NULL DEFAULT b'1',
    description VARCHAR(500) NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ledger_account_code UNIQUE (code),
    INDEX idx_ledger_account_type_active (account_type, active)
);

CREATE TABLE journal_entries (
    id INT NOT NULL AUTO_INCREMENT,
    entry_number VARCHAR(50) NOT NULL,
    entry_date DATE NOT NULL,
    description VARCHAR(500) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_reference VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    posted_by VARCHAR(150) NULL,
    reversed_entry_id INT NULL,
    version BIGINT DEFAULT 0,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_journal_entry_number UNIQUE (entry_number),
    CONSTRAINT uk_journal_source UNIQUE (source_type, source_reference),
    CONSTRAINT fk_journal_reversed_entry FOREIGN KEY (reversed_entry_id) REFERENCES journal_entries (id),
    INDEX idx_journal_entry_date (entry_date),
    INDEX idx_journal_entry_source (source_type, source_reference),
    INDEX idx_journal_entry_status (status)
);

CREATE TABLE journal_entry_lines (
    id INT NOT NULL AUTO_INCREMENT,
    journal_entry_id INT NOT NULL,
    account_id INT NOT NULL,
    description VARCHAR(300) NULL,
    debit_amount DOUBLE NOT NULL DEFAULT 0,
    credit_amount DOUBLE NOT NULL DEFAULT 0,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_journal_line_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id),
    CONSTRAINT fk_journal_line_account FOREIGN KEY (account_id) REFERENCES ledger_accounts (id),
    CONSTRAINT chk_journal_line_one_side CHECK (
        (debit_amount > 0 AND credit_amount = 0)
        OR (credit_amount > 0 AND debit_amount = 0)
    ),
    INDEX idx_journal_line_entry (journal_entry_id),
    INDEX idx_journal_line_account (account_id)
);

INSERT INTO ledger_accounts
    (code, name, account_type, normal_balance, liquid_account, system_account, active, description, version, created_at, updated_at)
VALUES
    ('111', 'Tiền mặt tại cửa hàng', 'ASSET', 'DEBIT', b'1', b'1', b'1', 'Tiền mặt thực tế do cửa hàng quản lý.', 0, NOW(6), NOW(6)),
    ('112', 'Tiền gửi và ví thanh toán', 'ASSET', 'DEBIT', b'1', b'1', b'1', 'Tiền trong tài khoản hoặc cổng thanh toán, chỉ theo dõi nội bộ.', 0, NOW(6), NOW(6)),
    ('156', 'Hàng hóa trong kho', 'ASSET', 'DEBIT', b'0', b'1', b'1', 'Giá vốn hàng hóa đã nhập kho.', 0, NOW(6), NOW(6)),
    ('331', 'Phải trả nhà cung cấp', 'LIABILITY', 'CREDIT', b'0', b'1', b'1', 'Công nợ phải thanh toán cho nhà cung cấp.', 0, NOW(6), NOW(6)),
    ('411', 'Vốn chủ sở hữu', 'EQUITY', 'CREDIT', b'0', b'1', b'1', 'Vốn chủ cửa hàng góp vào hoạt động.', 0, NOW(6), NOW(6)),
    ('511', 'Doanh thu bán hàng', 'REVENUE', 'CREDIT', b'0', b'1', b'1', 'Doanh thu từ các khoản thanh toán khách hàng thành công.', 0, NOW(6), NOW(6)),
    ('521', 'Giảm trừ và hoàn tiền bán hàng', 'REVENUE', 'DEBIT', b'0', b'1', b'1', 'Khoản hoàn tiền và giảm trừ doanh thu.', 0, NOW(6), NOW(6)),
    ('632', 'Giá vốn hàng bán', 'EXPENSE', 'DEBIT', b'0', b'1', b'1', 'Giá vốn của sản phẩm đã bán.', 0, NOW(6), NOW(6)),
    ('642', 'Chi phí vận hành', 'EXPENSE', 'DEBIT', b'0', b'1', b'1', 'Chi phí vận hành được ghi nhận thủ công.', 0, NOW(6), NOW(6));
