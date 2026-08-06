-- V4__logistics_multi_direction.sql
-- Nâng cấp cấu trúc bảng shipments hỗ trợ giao nhận đa chiều

-- 1. Loại bỏ ràng buộc unique uk_shipment_order để một đơn hàng có thể có nhiều loại vận đơn khác nhau (chiều đi/về)
ALTER TABLE `shipments` DROP INDEX `uk_shipment_order`;

-- 2. Chuyển cột order_id thành nullable
ALTER TABLE `shipments` MODIFY COLUMN `order_id` INT NULL;

-- 3. Chuyển đổi các cột ENUM sang VARCHAR để tối ưu hóa việc quản lý thông qua JPA AttributeConverter
ALTER TABLE `shipments` MODIFY COLUMN `status` VARCHAR(40) NOT NULL;
ALTER TABLE `shipments` MODIFY COLUMN `provider_code` VARCHAR(40) NOT NULL;

-- 4. Bổ sung các cột mới phục vụ luồng trả hàng Khách hàng và trả Nhà cung cấp
ALTER TABLE `shipments` ADD COLUMN `shipment_type` VARCHAR(30) NOT NULL DEFAULT 'FORWARD';
ALTER TABLE `shipments` ADD COLUMN `customer_return_id` INT NULL;
ALTER TABLE `shipments` ADD COLUMN `supplier_return_id` INT NULL;

-- 5. Bổ sung các Index hỗ trợ tìm kiếm nhanh các phiếu trả hàng
ALTER TABLE `shipments` ADD INDEX `idx_shipment_cust_return` (`customer_return_id`);
ALTER TABLE `shipments` ADD INDEX `idx_shipment_supp_return` (`supplier_return_id`);
