-- DANGER: Manual cleanup for transactional/test data.
-- Scope: orders, payments, refunds, shipments, import receipts, returns,
-- carts, reviews, notifications, stock batches, and inventory quantities.
-- Kept: users, addresses, products, variants, categories, suppliers, promotions,
-- moderation settings, notification preferences, and verification tokens.

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `review_media`;
DELETE FROM `reviews`;

DELETE FROM `notifications`;

DELETE FROM `cart_items`;
DELETE FROM `carts`;

DELETE FROM `payment_refunds`;
DELETE FROM `payment_transactions`;

DELETE FROM `shipment_tracking_logs`;
DELETE FROM `shipment_items`;
DELETE FROM `shipments`;

DELETE FROM `customer_return_histories`;
DELETE FROM `customer_return_images`;
DELETE FROM `customer_return_items`;
DELETE FROM `customer_returns`;

DELETE FROM `supplier_return_histories`;
DELETE FROM `supplier_return_images`;
DELETE FROM `supplier_return_items`;
DELETE FROM `supplier_returns`;

DELETE FROM `order_histories`;
DELETE FROM `order_items`;
DELETE FROM `orders`;

DELETE FROM `import_note_items`;
DELETE FROM `import_notes`;

DELETE FROM `inventory_batches`;

UPDATE `inventories`
   SET `stock_quantity` = 0,
       `reserved_quantity` = 0,
       `updated_at` = CURRENT_TIMESTAMP(6);

ALTER TABLE `review_media` AUTO_INCREMENT = 1;
ALTER TABLE `reviews` AUTO_INCREMENT = 1;
ALTER TABLE `notifications` AUTO_INCREMENT = 1;
ALTER TABLE `cart_items` AUTO_INCREMENT = 1;
ALTER TABLE `carts` AUTO_INCREMENT = 1;
ALTER TABLE `payment_refunds` AUTO_INCREMENT = 1;
ALTER TABLE `payment_transactions` AUTO_INCREMENT = 1;
ALTER TABLE `shipment_tracking_logs` AUTO_INCREMENT = 1;
ALTER TABLE `shipment_items` AUTO_INCREMENT = 1;
ALTER TABLE `shipments` AUTO_INCREMENT = 1;
ALTER TABLE `customer_return_histories` AUTO_INCREMENT = 1;
ALTER TABLE `customer_return_images` AUTO_INCREMENT = 1;
ALTER TABLE `customer_return_items` AUTO_INCREMENT = 1;
ALTER TABLE `customer_returns` AUTO_INCREMENT = 1;
ALTER TABLE `supplier_return_histories` AUTO_INCREMENT = 1;
ALTER TABLE `supplier_return_images` AUTO_INCREMENT = 1;
ALTER TABLE `supplier_return_items` AUTO_INCREMENT = 1;
ALTER TABLE `supplier_returns` AUTO_INCREMENT = 1;
ALTER TABLE `order_histories` AUTO_INCREMENT = 1;
ALTER TABLE `order_items` AUTO_INCREMENT = 1;
ALTER TABLE `orders` AUTO_INCREMENT = 1;
ALTER TABLE `import_note_items` AUTO_INCREMENT = 1;
ALTER TABLE `import_notes` AUTO_INCREMENT = 1;
ALTER TABLE `inventory_batches` AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;
