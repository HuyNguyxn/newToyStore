-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: new_toy_store
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_default` bit(1) NOT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_phone` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_address_user_id` (`user_id`),
  CONSTRAINT `FK1fa36y2oqhao3wgg2rw1pi459` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blacklisted_words`
--

DROP TABLE IF EXISTS `blacklisted_words`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blacklisted_words` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `category` enum('COMPETITOR','OTHER','PROFANITY','SPAM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `word` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_blacklisted_word` (`word`),
  KEY `idx_blacklisted_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `added_price` double NOT NULL,
  `is_selected` bit(1) NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `variant_id` int NOT NULL,
  `cart_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_variant` (`cart_id`,`variant_id`),
  KEY `idx_cart_item_variant` (`variant_id`),
  KEY `idx_cart_item_updated_at` (`updated_at`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`),
  CONSTRAINT `cart_items_chk_1` CHECK (((`product_id` > 0) and (`variant_id` > 0) and (`quantity` > 0) and (`added_price` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `status` enum('ACTIVE','CHECKING_OUT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user` (`user_id`),
  CONSTRAINT `carts_chk_1` CHECK (((`user_id` > 0) and (`status` in (_utf8mb4'ACTIVE',_utf8mb4'CHECKING_OUT'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int NOT NULL,
  `icon_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `level` int NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('DELETED','HIDDEN','VISIBLE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_slug` (`slug`),
  KEY `idx_category_slug` (`slug`),
  KEY `idx_category_status` (`status`),
  KEY `idx_category_path` (`path`),
  KEY `idx_category_parent_order` (`parent_id`,`display_order`),
  CONSTRAINT `FKsaok720gsu4u2wrgbk10b5n8d` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_return_histories`
--

DROP TABLE IF EXISTS `customer_return_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_return_histories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `action_by` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action_date` datetime(6) NOT NULL,
  `new_status` enum('APPROVED','CANCELLED','DISPUTED','INSPECTED_FAILED','INSPECTED_OK','NEEDS_MORE_INFO','RECEIVED','REFUNDED','REJECTED','REPLACED','REQUESTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_status` enum('APPROVED','CANCELLED','DISPUTED','INSPECTED_FAILED','INSPECTED_OK','NEEDS_MORE_INFO','RECEIVED','REFUNDED','REJECTED','REPLACED','REQUESTED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe0kxqkqvlwi4lpwxru5908ifj` (`customer_return_id`),
  CONSTRAINT `FKe0kxqkqvlwi4lpwxru5908ifj` FOREIGN KEY (`customer_return_id`) REFERENCES `customer_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_return_images`
--

DROP TABLE IF EXISTS `customer_return_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_return_images` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK10st1htlg46iuiqaitj0bjih1` (`customer_return_id`),
  CONSTRAINT `FK10st1htlg46iuiqaitj0bjih1` FOREIGN KEY (`customer_return_id`) REFERENCES `customer_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_return_items`
--

DROP TABLE IF EXISTS `customer_return_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_return_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expected_refund_amount` double NOT NULL,
  `order_item_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `reason_code` enum('CHANGED_MIND','DAMAGED_IN_TRANSIT','DEFECTIVE','WRONG_ITEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `variant_id` int NOT NULL,
  `customer_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkxoapw252r2cjl1eduyk021c5` (`customer_return_id`),
  CONSTRAINT `FKkxoapw252r2cjl1eduyk021c5` FOREIGN KEY (`customer_return_id`) REFERENCES `customer_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_returns`
--

DROP TABLE IF EXISTS `customer_returns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_returns` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `admin_note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deadline_for_extra_info` datetime(6) DEFAULT NULL,
  `is_high_risk` bit(1) NOT NULL,
  `order_id` int NOT NULL,
  `return_shipping_fee` double NOT NULL,
  `status` enum('APPROVED','CANCELLED','DISPUTED','INSPECTED_FAILED','INSPECTED_OK','NEEDS_MORE_INFO','RECEIVED','REFUNDED','REJECTED','REPLACED','REQUESTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cust_return_order_id` (`order_id`),
  KEY `idx_cust_return_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_note_items`
--

DROP TABLE IF EXISTS `import_note_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_note_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `import_price` double NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `variant_id` int NOT NULL,
  `import_note_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_import_note_variant` (`import_note_id`,`variant_id`),
  KEY `idx_import_item_note_id` (`import_note_id`),
  KEY `idx_import_item_variant_id` (`variant_id`),
  CONSTRAINT `FK4p2fa9iw18bhhonklxa6fcu08` FOREIGN KEY (`import_note_id`) REFERENCES `import_notes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_notes`
--

DROP TABLE IF EXISTS `import_notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_notes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_id` int NOT NULL,
  `total_amount` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_import_note_supplier_id` (`supplier_id`),
  KEY `idx_import_note_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventories`
--

DROP TABLE IF EXISTS `inventories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `reserved_quantity` int NOT NULL DEFAULT '0',
  `stock_quantity` int NOT NULL,
  `version` bigint DEFAULT NULL,
  `variant_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK93u7raebj7w49wn4449dv1v79` (`variant_id`),
  KEY `idx_inventory_variant_id` (`variant_id`),
  CONSTRAINT `FK1hwr2nkixleellrw6uthechom` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `inventories_chk_1` CHECK (((`stock_quantity` >= 0) and (`reserved_quantity` >= 0) and (`reserved_quantity` <= `stock_quantity`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventory_batches`
--

DROP TABLE IF EXISTS `inventory_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_batches` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `batch_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` date NOT NULL,
  `quantity` int NOT NULL,
  `version` bigint DEFAULT NULL,
  `inventory_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_batch_inventory_id` (`inventory_id`),
  CONSTRAINT `FK4hvb5vqjhklm0g1ujtdx19snu` FOREIGN KEY (`inventory_id`) REFERENCES `inventories` (`id`),
  CONSTRAINT `inventory_batches_chk_1` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_preferences`
--

DROP TABLE IF EXISTS `notification_preferences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_preferences` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `cart_enabled` bit(1) NOT NULL,
  `email_enabled` bit(1) NOT NULL,
  `in_app_enabled` bit(1) NOT NULL,
  `order_enabled` bit(1) NOT NULL,
  `payment_enabled` bit(1) NOT NULL,
  `return_enabled` bit(1) NOT NULL,
  `review_enabled` bit(1) NOT NULL,
  `shipment_enabled` bit(1) NOT NULL,
  `system_enabled` bit(1) NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_preference_user` (`user_id`),
  KEY `idx_notification_preference_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `action_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deduplication_key` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `recipient_user_id` int NOT NULL,
  `reference_id` int DEFAULT NULL,
  `reference_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_deduplication_key` (`deduplication_key`),
  KEY `idx_notification_recipient_status_created` (`recipient_user_id`,`status`,`created_at`),
  KEY `idx_notification_recipient_type_created` (`recipient_user_id`,`type`,`created_at`),
  KEY `idx_notification_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_histories`
--

DROP TABLE IF EXISTS `order_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_histories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','FULLY_REFUNDED','PARTIALLY_REFUNDED','PENDING','SHIPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_history_order` (`order_id`),
  CONSTRAINT `FK4x7xskavxw4wtfuwmbq5fujus` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `price` double NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `variant_attributes_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `variant_id` int NOT NULL,
  `order_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_variant` (`order_id`,`variant_id`),
  KEY `idx_order_item_order` (`order_id`),
  KEY `idx_order_item_product` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `discount_amount` double NOT NULL,
  `promo_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','FULLY_REFUNDED','PARTIALLY_REFUNDED','PENDING','SHIPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` double NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_created_at` (`created_at`),
  KEY `idx_order_user_id` (`user_id`),
  KEY `idx_order_status` (`status`),
  KEY `idx_order_user_status_created` (`user_id`,`status`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_refunds`
--

DROP TABLE IF EXISTS `payment_refunds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_refunds` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `amount` double NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `failed_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `method` enum('COD_MANUAL','VNPAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` int NOT NULL,
  `payment_id` int NOT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `provider_refund_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `refund_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','FAILED','PENDING','PROCESSING','REJECTED','SUCCEEDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_refund_code` (`refund_code`),
  KEY `idx_refund_payment` (`payment_id`),
  KEY `idx_refund_order` (`order_id`),
  KEY `idx_refund_user` (`user_id`),
  KEY `idx_refund_status` (`status`),
  KEY `idx_refund_method` (`method`),
  KEY `idx_refund_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_transactions`
--

DROP TABLE IF EXISTS `payment_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `amount` double NOT NULL,
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expired_at` datetime(6) DEFAULT NULL,
  `failure_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `idempotency_key` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `method` enum('COD','VNPAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` int NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `provider_transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELLED','EXPIRED','FAILED','PENDING','REFUNDED','REFUND_FAILED','REFUND_PENDING','SUCCEEDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_user_idempotency_key` (`user_id`,`idempotency_key`),
  KEY `idx_payment_order_id` (`order_id`),
  KEY `idx_payment_user_id` (`user_id`),
  KEY `idx_payment_status` (`status`),
  KEY `idx_payment_method` (`method`),
  KEY `idx_payment_idempotency_key` (`idempotency_key`),
  KEY `idx_payment_created_at` (`created_at`),
  KEY `idx_payment_user_status_created` (`user_id`,`status`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_attribute_values`
--

DROP TABLE IF EXISTS `product_attribute_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attribute_values` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `attribute_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attribute_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `variant_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_variant_attribute` (`variant_id`,`attribute_name`),
  CONSTRAINT `FKtb855yedffeathpo3p5i1onsp` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_categories`
--

DROP TABLE IF EXISTS `product_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_categories` (
  `product_id` int NOT NULL,
  `category_id` int NOT NULL,
  PRIMARY KEY (`product_id`,`category_id`),
  KEY `FKd112rx0alycddsms029iifrih` (`category_id`),
  CONSTRAINT `FKd112rx0alycddsms029iifrih` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `FKlda9rad6s180ha3dl1ncsp8n7` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_thumbnail` bit(1) NOT NULL,
  `product_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_image_product_id` (`product_id`),
  CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `cost_price` double NOT NULL,
  `price` double NOT NULL,
  `type` enum('DEFAULT','MASTER','REGULAR') COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_variant_product_id` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `product_variants_chk_1` CHECK (((`price` >= 0) and (`cost_price` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `average_rating` double NOT NULL,
  `base_price` double NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `review_count` int NOT NULL,
  `status` enum('ACTIVE','INACTIVE','OUT_OF_STOCK') COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_created_at` (`created_at`),
  KEY `idx_product_supplier_id` (`supplier_id`),
  CONSTRAINT `products_chk_1` CHECK (((`base_price` >= 0) and (`average_rating` >= 0) and (`review_count` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` double NOT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `max_discount_amount` double DEFAULT NULL,
  `min_order_value` double DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `scope` enum('ORDER','PRODUCT','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `target_product_id` int DEFAULT NULL,
  `type` enum('FIXED_AMOUNT','PERCENTAGE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `usage_limit` int DEFAULT NULL,
  `used_count` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_promo_code` (`code`),
  KEY `idx_promo_time_status` (`is_active`,`start_date`,`end_date`),
  KEY `idx_promo_scope_target` (`scope`,`target_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `admin_reply` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `comment` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_item_id` int NOT NULL,
  `product_id` int NOT NULL,
  `rating` int NOT NULL,
  `status` enum('HIDDEN','PUBLISHED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  `variant_attributes_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_order_item` (`order_item_id`),
  KEY `idx_review_product_id` (`product_id`),
  KEY `idx_review_status` (`status`),
  KEY `idx_review_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shipment_items`
--

DROP TABLE IF EXISTS `shipment_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipment_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `product_id` int NOT NULL,
  `product_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `variant_id` int NOT NULL,
  `variant_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shipment_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shipment_item_shipment` (`shipment_id`),
  KEY `idx_shipment_item_product` (`product_id`),
  KEY `idx_shipment_item_variant` (`variant_id`),
  CONSTRAINT `FK4rh14gyym63tnsi2i95f61d7` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shipment_tracking_logs`
--

DROP TABLE IF EXISTS `shipment_tracking_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipment_tracking_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `shipment_id` int NOT NULL,
  `status` enum('CANCELLED','DELIVERED','DELIVERY_FAILED','IN_TRANSIT','PENDING_PICKUP','RETURNED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tracking_log_shipment_occurred` (`shipment_id`,`occurred_at`),
  KEY `idx_tracking_log_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shipments`
--

DROP TABLE IF EXISTS `shipments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `cod_amount` double NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `delivery_attempt_count` int NOT NULL,
  `failure_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` int NOT NULL,
  `provider_code` enum('GHN','SELF_SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_shipment_code` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recipient_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `returned_at` datetime(6) DEFAULT NULL,
  `shipping_address_snapshot` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shipping_fee` double NOT NULL,
  `status` enum('CANCELLED','DELIVERED','DELIVERY_FAILED','IN_TRANSIT','PENDING_PICKUP','RETURNED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `tracking_code` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipment_tracking_code` (`tracking_code`),
  UNIQUE KEY `uk_shipment_order` (`order_id`),
  KEY `idx_shipment_order` (`order_id`),
  KEY `idx_shipment_user` (`user_id`),
  KEY `idx_shipment_status` (`status`),
  KEY `idx_shipment_provider` (`provider_code`),
  KEY `idx_shipment_created_at` (`created_at`),
  KEY `idx_shipment_status_created` (`status`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_return_histories`
--

DROP TABLE IF EXISTS `supplier_return_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_return_histories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `action_by` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `new_status` enum('APPROVED','CANCELLED','COMPLETED','DRAFT','PENDING_APPROVAL','REJECTED','SHIPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_status` enum('APPROVED','CANCELLED','COMPLETED','DRAFT','PENDING_APPROVAL','REJECTED','SHIPPED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `supplier_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkeq72jva15ymfb61vt1ckgs2t` (`supplier_return_id`),
  CONSTRAINT `FKkeq72jva15ymfb61vt1ckgs2t` FOREIGN KEY (`supplier_return_id`) REFERENCES `supplier_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_return_images`
--

DROP TABLE IF EXISTS `supplier_return_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_return_images` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkkh9q1hqmm33etywmbnj20ljm` (`supplier_return_id`),
  CONSTRAINT `FKkkh9q1hqmm33etywmbnj20ljm` FOREIGN KEY (`supplier_return_id`) REFERENCES `supplier_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_return_items`
--

DROP TABLE IF EXISTS `supplier_return_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_return_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `accepted_quantity` int NOT NULL,
  `batch_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_amount` double NOT NULL,
  `discrepancy_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expiry_date` date NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `reason_code` enum('DEFECTIVE','EXPIRED','LIQUIDATION','WRONG_ITEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `return_price` double NOT NULL,
  `variant_id` int NOT NULL,
  `supplier_return_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sup_ret_variant` (`supplier_return_id`,`variant_id`),
  KEY `idx_sup_ret_item_variant` (`variant_id`),
  CONSTRAINT `FKbpwnt2q1sphohsc2lwmikcu05` FOREIGN KEY (`supplier_return_id`) REFERENCES `supplier_returns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_returns`
--

DROP TABLE IF EXISTS `supplier_returns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_returns` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `freight_cost` double NOT NULL,
  `import_note_id` int DEFAULT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `restocking_fee` double NOT NULL,
  `status` enum('APPROVED','CANCELLED','COMPLETED','DRAFT','PENDING_APPROVAL','REJECTED','SHIPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_id` int NOT NULL,
  `total_refund_amount` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sup_ret_supplier` (`supplier_id`),
  KEY `idx_sup_ret_status` (`status`),
  KEY `idx_sup_ret_import` (`import_note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','BLACKLISTED','SUSPENDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_phone_number` (`phone_number`),
  KEY `idx_supplier_phone` (`phone_number`),
  KEY `idx_supplier_name` (`name`),
  KEY `idx_supplier_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','CUSTOMER','MANAGER','STAFF') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','LOCKED','UNVERIFIED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `idx_user_email` (`email`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_role_status` (`role`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verification_tokens`
--

DROP TABLE IF EXISTS `verification_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `token_type` enum('ACCESS_TOKEN','RESET_PASSWORD','VERIFICATION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verification_token` (`token_value`),
  UNIQUE KEY `idx_token_value` (`token_value`),
  KEY `idx_token_user_id` (`user_id`),
  CONSTRAINT `FK54y8mqsnq1rtyf581sfmrbp4f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-30  6:24:10
