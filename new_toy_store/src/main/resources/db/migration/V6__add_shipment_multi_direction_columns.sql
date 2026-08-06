-- V6__add_shipment_multi_direction_columns.sql
-- Bổ sung các cột phục vụ luồng logistics đa chiều cho bảng shipments
-- Sử dụng Stored Procedure để kiểm tra tồn tại trước khi thêm (tương thích MySQL 8.x)

DELIMITER //

DROP PROCEDURE IF EXISTS add_shipment_columns//

CREATE PROCEDURE add_shipment_columns()
BEGIN
    -- Thêm cột shipment_type nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND COLUMN_NAME = 'shipment_type'
    ) THEN
        ALTER TABLE `shipments` ADD COLUMN `shipment_type` VARCHAR(30) NOT NULL DEFAULT 'FORWARD';
    END IF;

    -- Thêm cột customer_return_id nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND COLUMN_NAME = 'customer_return_id'
    ) THEN
        ALTER TABLE `shipments` ADD COLUMN `customer_return_id` INT NULL;
    END IF;

    -- Thêm cột supplier_return_id nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND COLUMN_NAME = 'supplier_return_id'
    ) THEN
        ALTER TABLE `shipments` ADD COLUMN `supplier_return_id` INT NULL;
    END IF;

    -- Chuyển order_id sang nullable
    ALTER TABLE `shipments` MODIFY COLUMN `order_id` INT NULL;

    -- Chuyển status và provider_code sang VARCHAR (idempotent)
    ALTER TABLE `shipments` MODIFY COLUMN `status` VARCHAR(40) NOT NULL;
    ALTER TABLE `shipments` MODIFY COLUMN `provider_code` VARCHAR(40) NOT NULL;

    -- Thêm index nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND INDEX_NAME = 'idx_shipment_cust_return'
    ) THEN
        CREATE INDEX `idx_shipment_cust_return` ON `shipments` (`customer_return_id`);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND INDEX_NAME = 'idx_shipment_supp_return'
    ) THEN
        CREATE INDEX `idx_shipment_supp_return` ON `shipments` (`supplier_return_id`);
    END IF;

    -- Xóa unique index cũ nếu còn tồn tại
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND INDEX_NAME = 'uk_shipment_order'
    ) THEN
        ALTER TABLE `shipments` DROP INDEX `uk_shipment_order`;
    END IF;
END//

DELIMITER ;

CALL add_shipment_columns();

DROP PROCEDURE IF EXISTS add_shipment_columns;
