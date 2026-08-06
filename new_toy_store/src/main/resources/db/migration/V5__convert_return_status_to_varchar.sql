-- V5__convert_return_status_to_varchar.sql
-- Chuyển đổi các cột ENUM trạng thái sang VARCHAR để tương thích JPA AttributeConverter

ALTER TABLE `customer_returns` MODIFY COLUMN `status` VARCHAR(40) NOT NULL;
ALTER TABLE `supplier_returns` MODIFY COLUMN `status` VARCHAR(40) NOT NULL;
