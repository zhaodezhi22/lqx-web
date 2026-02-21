SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `community_comment`;
CREATE TABLE `community_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` tinyint NOT NULL,
  `content` varchar(500) NOT NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`)
);

DROP TABLE IF EXISTS `heritage_resource`;
CREATE TABLE `heritage_resource`  (
  `resource_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `type` tinyint NOT NULL,
  `category` varchar(50) NULL DEFAULT NULL,
  `cover_img` varchar(255) NULL DEFAULT NULL,
  `file_url` varchar(500) NOT NULL,
  `uploader_id` bigint NOT NULL,
  `description` text NULL,
  `view_count` int NULL DEFAULT 0,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`resource_id`)
);

DROP TABLE IF EXISTS `inheritor_profile`;
CREATE TABLE `inheritor_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `level` varchar(50) NULL DEFAULT NULL,
  `genre` varchar(50) NULL DEFAULT NULL,
  `master_name` varchar(50) NULL DEFAULT NULL,
  `artistic_career` text NULL,
  `awards` text NULL,
  `verify_status` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `performance_event`;
CREATE TABLE `performance_event`  (
  `event_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `venue` varchar(100) NOT NULL,
  `show_time` datetime NOT NULL,
  `ticket_price` decimal(10, 2) NOT NULL,
  `total_seats` int NOT NULL,
  `seat_layout_json` json NULL,
  `status` tinyint NULL DEFAULT 1,
  PRIMARY KEY (`event_id`)
);

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `sub_title` varchar(200) NULL DEFAULT NULL,
  `main_image` varchar(255) NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int NOT NULL,
  `status` tinyint NULL DEFAULT 1,
  `detail` text NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`)
);

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) NULL DEFAULT NULL,
  `avatar` varchar(255) NULL DEFAULT NULL,
  `phone` varchar(20) NULL DEFAULT NULL,
  `email` varchar(50) NULL DEFAULT NULL,
  `role` tinyint NOT NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 1,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
);

DROP TABLE IF EXISTS `ticket_order`;
CREATE TABLE `ticket_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  `event_id` bigint NOT NULL,
  `seat_info` varchar(100) NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `status` tinyint NULL DEFAULT 0,
  `qr_code` varchar(255) NULL DEFAULT NULL,
  `pay_time` datetime NULL DEFAULT NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`)
);

SET FOREIGN_KEY_CHECKS = 1;
