INSERT INTO ledger_accounts
    (code, name, account_type, normal_balance, liquid_account, system_account, active, description, version, created_at, updated_at)
SELECT
    '3388',
    'Tiền khách trả trước',
    'LIABILITY',
    'CREDIT',
    b'0',
    b'1',
    b'1',
    'Khoản tiền cửa hàng đã thu nhưng đơn hàng chưa hoàn tất và chưa đủ điều kiện ghi nhận doanh thu.',
    0,
    NOW(6),
    NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM ledger_accounts WHERE code = '3388');
