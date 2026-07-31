CREATE TABLE `review_media` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `display_order` int NOT NULL,
  `media_type` enum('IMAGE','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `review_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_review_media_review_id` (`review_id`),
  KEY `idx_review_media_type` (`media_type`),
  CONSTRAINT `fk_review_media_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
