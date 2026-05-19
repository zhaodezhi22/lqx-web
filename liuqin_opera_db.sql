/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : liuqin_opera_db

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 19/05/2026 13:07:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_log`;
CREATE TABLE `admin_operation_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint NULL DEFAULT NULL,
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator_role` int NULL DEFAULT NULL,
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_query` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `action_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `response_status` int NULL DEFAULT NULL,
  `success_flag` tinyint NULL DEFAULT 1,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_admin_log_created_time`(`created_time` ASC) USING BTREE,
  INDEX `idx_admin_log_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1043 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apprentice_task
-- ----------------------------
DROP TABLE IF EXISTS `apprentice_task`;
CREATE TABLE `apprentice_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `master_id` bigint NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `demo_video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apprenticeship_apply
-- ----------------------------
DROP TABLE IF EXISTS `apprenticeship_apply`;
CREATE TABLE `apprenticeship_apply`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `master_id` bigint NOT NULL,
  `apply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '拜师贴内容',
  `status` tinyint NULL DEFAULT 0 COMMENT '0-审核中 1-通过 2-拒绝',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核操作时间',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝理由等',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拜师申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apprenticeship_relation
-- ----------------------------
DROP TABLE IF EXISTS `apprenticeship_relation`;
CREATE TABLE `apprenticeship_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `master_id` bigint NOT NULL COMMENT '传承人(老师)的User ID',
  `student_id` bigint NOT NULL COMMENT '学徒的User ID',
  `relation_status` tinyint NULL DEFAULT 1 COMMENT '1-在教, 2-结业, 3-解除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_master`(`master_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '师徒正式关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for community_comment
-- ----------------------------
DROP TABLE IF EXISTS `community_comment`;
CREATE TABLE `community_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID(资源ID或商品ID)',
  `target_type` tinyint NOT NULL COMMENT '类型: 1-资源, 2-商品, 3-帖子',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID(用于回复)',
  `is_official` tinyint NULL DEFAULT 0 COMMENT '是否为传承人/官方回复',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-正常, 1-被举报, 2-隐藏',
  PRIMARY KEY (`comment_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '互动评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for community_like
-- ----------------------------
DROP TABLE IF EXISTS `community_like`;
CREATE TABLE `community_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` tinyint NOT NULL COMMENT '1-帖子, 2-评论',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for community_post
-- ----------------------------
DROP TABLE IF EXISTS `community_post`;
CREATE TABLE `community_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖人',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `images` json NULL COMMENT '图片URL数组 [\"url1\", \"url2\"]',
  `view_count` int NULL DEFAULT 0,
  `like_count` int NULL DEFAULT 0,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NULL DEFAULT 1 COMMENT '0-Pending, 1-Approved, 2-Rejected',
  `source_assignment_id` bigint NULL DEFAULT NULL COMMENT '来源作业ID',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区帖子表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for daily_sign_in
-- ----------------------------
DROP TABLE IF EXISTS `daily_sign_in`;
CREATE TABLE `daily_sign_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `sign_date` date NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `sign_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for heritage_resource
-- ----------------------------
DROP TABLE IF EXISTS `heritage_resource`;
CREATE TABLE `heritage_resource`  (
  `resource_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-视频, 2-音频, 3-图文, 4-剧本PDF',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `uploader_id` bigint NOT NULL,
  `is_certified` tinyint NULL DEFAULT 0 COMMENT '是否官方认证/精选: 0-否, 1-是',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `view_count` int NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 0 COMMENT '0-待审核, 1-已发布, 2-已驳回',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`resource_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非遗资源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for home_content
-- ----------------------------
DROP TABLE IF EXISTS `home_content`;
CREATE TABLE `home_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'HOME_CAROUSEL, NEWS_CAROUSEL, NOTICE, HIGHLIGHT',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `link_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1 COMMENT '1=Active, 0=Disabled',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for im_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `im_chat_message`;
CREATE TABLE `im_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_id` bigint NOT NULL COMMENT '发送者ID',
  `to_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `type` int NULL DEFAULT 0 COMMENT '0文本, 1图片, 2撤回提示',
  `is_recalled` int NULL DEFAULT 0 COMMENT '0否, 1是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for im_friend
-- ----------------------------
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for im_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `im_friend_request`;
CREATE TABLE `im_friend_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_id` bigint NOT NULL COMMENT '申请人ID',
  `to_id` bigint NOT NULL COMMENT '被申请人ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请理由',
  `status` int NULL DEFAULT 0 COMMENT '0待处理, 1已通过, 2已拒绝',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for inheritor_level_apply
-- ----------------------------
DROP TABLE IF EXISTS `inheritor_level_apply`;
CREATE TABLE `inheritor_level_apply`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `current_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `apply_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `proof_materials` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` int NULL DEFAULT 0 COMMENT '0-审核中, 1-通过, 2-拒绝',
  `audit_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `audit_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for inheritor_profile
-- ----------------------------
DROP TABLE IF EXISTS `inheritor_profile`;
CREATE TABLE `inheritor_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '传承等级(如: 国家级, 省级, 市级)',
  `genre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流派',
  `master_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '师承(师傅名字)',
  `master_id` bigint NULL DEFAULT NULL COMMENT '师父的传承人档案ID(不是User ID)',
  `artistic_career` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '演艺经历/生平介绍',
  `awards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '获奖情况',
  `verify_status` tinyint NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `certificates` json NULL COMMENT '证书/身份证图片URL集合',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '传承人档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mall_order
-- ----------------------------
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL COMMENT '应付总额',
  `points_discount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实际支付金额',
  `used_points` int NULL DEFAULT 0 COMMENT '消耗积分数',
  `address_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0-待付 1-已付 2-待收货 3-已完成 4-已取消',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `delivery_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递公司',
  `delivery_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递单号',
  `refund_apply_time` datetime NULL DEFAULT NULL COMMENT '退款申请时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商城订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mall_order_item
-- ----------------------------
DROP TABLE IF EXISTS `mall_order_item`;
CREATE TABLE `mall_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for performance_event
-- ----------------------------
DROP TABLE IF EXISTS `performance_event`;
CREATE TABLE `performance_event`  (
  `event_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '演出标题',
  `venue` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '演出地点',
  `show_time` datetime NOT NULL COMMENT '演出时间',
  `ticket_price` decimal(10, 2) NOT NULL COMMENT '基础票价',
  `total_seats` int NOT NULL COMMENT '总座位数',
  `seat_layout_json` json NULL COMMENT '座位布局数据(JSON格式存储行/列/状态)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-未开始, 1-售票中, 2-已结束',
  `publisher_id` bigint NULL DEFAULT NULL COMMENT '发布人ID',
  `resource_id` bigint NULL DEFAULT NULL COMMENT '关联戏曲资源ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动介绍',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  PRIMARY KEY (`event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '演出活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for points_log
-- ----------------------------
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `change_point` int NOT NULL COMMENT '+10 或 -5',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到/评论',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sub_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `main_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int NOT NULL,
  `sales_volume` int NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NULL DEFAULT 1 COMMENT '1-在售, 0-下架',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `seller_id` bigint NULL DEFAULT NULL COMMENT '所属传承人ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文创商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for product_log
-- ----------------------------
DROP TABLE IF EXISTS `product_log`;
CREATE TABLE `product_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NULL DEFAULT NULL,
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for resource_category
-- ----------------------------
DROP TABLE IF EXISTS `resource_category`;
CREATE TABLE `resource_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort_order` int NULL DEFAULT 0,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`) USING BTREE,
  UNIQUE INDEX `uk_resource_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '资源分类配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-传承人, 2-管理员, 3-超级管理员，4-学徒',
  `current_points` int NULL DEFAULT 0 COMMENT '当前积分余额',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `continuous_sign_days` int NULL DEFAULT 0 COMMENT '连续签到天数',
  `last_sign_date` date NULL DEFAULT NULL COMMENT '最后签到日期',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 134 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for task_assignment
-- ----------------------------
DROP TABLE IF EXISTS `task_assignment`;
CREATE TABLE `task_assignment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0-已分配, 1-已提交, 2-已评价',
  `submission_video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `submission_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `submit_time` datetime NULL DEFAULT NULL,
  `score` int NULL DEFAULT NULL COMMENT '分数(0-100)',
  `review_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '老师评语',
  `review_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务执行与评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ticket_order
-- ----------------------------
DROP TABLE IF EXISTS `ticket_order`;
CREATE TABLE `ticket_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `event_id` bigint NOT NULL,
  `seat_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0-待支付, 1-已支付, 2-已核销, 3-已取消',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `verifier_id` bigint NULL DEFAULT NULL COMMENT '执行核销的操作员/传承人ID',
  `verify_time` datetime NULL DEFAULT NULL COMMENT '核销时间',
  `pay_time` datetime NULL DEFAULT NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '演出票务订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_default` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for wallet_withdraw_request
-- ----------------------------
DROP TABLE IF EXISTS `wallet_withdraw_request`;
CREATE TABLE `wallet_withdraw_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `user_role` int NULL DEFAULT NULL,
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ALL',
  `amount` decimal(10, 2) NOT NULL,
  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `account_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '0-待处理,1-已完成,2-已拒绝',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
