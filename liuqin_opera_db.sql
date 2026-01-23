/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : liuqin_opera_db

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 23/01/2026 12:01:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of apprenticeship_apply
-- ----------------------------

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
  PRIMARY KEY (`comment_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '互动评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_comment
-- ----------------------------
INSERT INTO `community_comment` VALUES (1, 1, 1, 1, '这出戏唱得真好，老艺术家的功底就是深厚！', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (2, 2, 1, 1, '画面很清晰，声音也很棒，支持非遗！', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (3, 3, 1, 1, '小时候爷爷经常带我听这出戏，满满的回忆。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (4, 1, 1, 2, '手办做工很精致，孩子很喜欢。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (5, 4, 3, 2, '扇子很漂亮，但是价格稍微有点贵。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (6, 5, 2, 1, '这个唱段太经典了，百听不厌。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (7, 2, 5, 1, '看了访谈录，对柳琴戏有了更深的了解。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (8, 3, 2, 2, '书签很有质感，送给朋友的礼物。', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (9, 6, 1, 1, '感谢大家的支持，我们会继续努力！', '2026-01-23 11:57:53');
INSERT INTO `community_comment` VALUES (10, 4, 9, 2, '本子纸张很好，书写顺滑。', '2026-01-23 11:57:53');

-- ----------------------------
-- Table structure for heritage_resource
-- ----------------------------
DROP TABLE IF EXISTS `heritage_resource`;
CREATE TABLE `heritage_resource`  (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标题(剧目名)',
  `type` tinyint NOT NULL COMMENT '类型: 1-视频, 2-音频, 3-图文, 4-剧本PDF',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类(如: 拉魂腔, 经典剧目)',
  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源文件OSS地址',
  `uploader_id` bigint NOT NULL COMMENT '上传人ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '资源简介',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`resource_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非遗资源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of heritage_resource
-- ----------------------------
INSERT INTO `heritage_resource` VALUES (1, '《喝面叶》全本', 1, '经典剧目', 'https://via.placeholder.com/300x200?text=Video1', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 6, '柳琴戏经典剧目《喝面叶》完整版视频', 1205, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (2, '柳琴戏唱腔选段', 2, '唱腔教学', 'https://via.placeholder.com/300x200?text=Audio1', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', 6, '著名唱段合集', 850, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (3, '柳琴戏的历史渊源', 3, '历史文化', 'https://via.placeholder.com/300x200?text=Article1', '#', 7, '详细介绍了柳琴戏的起源与发展。', 3400, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (4, '《小姑贤》剧本手稿', 4, '剧本资料', 'https://via.placeholder.com/300x200?text=PDF1', '#', 7, '珍贵的老艺人手写剧本扫描件', 560, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (5, '名家访谈录', 1, '纪录片', 'https://via.placeholder.com/300x200?text=Video2', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 6, '采访多位老艺术家', 980, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (6, '柳琴戏身段教学', 1, '教学视频', 'https://via.placeholder.com/300x200?text=Video3', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 6, '基础身段动作分解教学', 2100, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (7, '传统服饰展示', 3, '服饰道具', 'https://via.placeholder.com/300x200?text=Image1', '#', 7, '精美的戏曲服装图集', 1500, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (8, '乐器伴奏集', 2, '音乐伴奏', 'https://via.placeholder.com/300x200?text=Audio2', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', 7, '高清伴奏带', 300, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (9, '2024春晚演出实录', 1, '演出实录', 'https://via.placeholder.com/300x200?text=Video4', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 6, '春节联欢晚会柳琴戏节目', 5000, '2026-01-23 11:57:53');
INSERT INTO `heritage_resource` VALUES (10, '非遗保护座谈会纪要', 3, '新闻资讯', 'https://via.placeholder.com/300x200?text=Article2', '#', 6, '关于非遗保护工作的会议记录', 200, '2026-01-23 11:57:53');

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
  `artistic_career` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '演艺经历/生平介绍',
  `awards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '获奖情况',
  `verify_status` tinyint NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '传承人档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inheritor_profile
-- ----------------------------
INSERT INTO `inheritor_profile` VALUES (1, 6, '省级', '柳琴戏', '张宗师', '自幼习艺，从艺30年，擅长文生。', '省戏曲大赛金奖', 1);
INSERT INTO `inheritor_profile` VALUES (2, 7, '市级', '柳琴戏', '无', '家族传承，致力于柳琴戏推广。', '市文化贡献奖', 1);
INSERT INTO `inheritor_profile` VALUES (3, 8, '区级', '柳琴戏', '陈大师', '师从陈大师，新生代领军人物。', '青年演员优秀奖', 0);

-- ----------------------------
-- Table structure for mall_order
-- ----------------------------
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `address_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址快照',
  `status` tinyint NULL DEFAULT NULL COMMENT '0-待付 1-已付 2-发货 3-完成',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mall_order
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mall_order_item
-- ----------------------------

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
  PRIMARY KEY (`event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '演出活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of performance_event
-- ----------------------------
INSERT INTO `performance_event` VALUES (1, '柳琴戏专场演出《赵五娘》', '市大剧院', '2026-01-28 11:57:53', 80.00, 500, '{\"cols\": 25, \"rows\": 20}', 1);
INSERT INTO `performance_event` VALUES (2, '周末戏曲汇', '市民文化中心', '2026-01-25 11:57:53', 50.00, 200, '{\"cols\": 20, \"rows\": 10}', 1);
INSERT INTO `performance_event` VALUES (3, '非遗进校园巡演', '第一中学礼堂', '2026-02-02 11:57:53', 0.00, 800, '{\"cols\": 32, \"rows\": 25}', 0);
INSERT INTO `performance_event` VALUES (4, '经典折子戏展演', '老茶馆戏台', '2026-01-22 11:57:53', 30.00, 100, '{\"cols\": 10, \"rows\": 10}', 2);
INSERT INTO `performance_event` VALUES (5, '新年戏曲晚会', '省艺术中心', '2026-02-12 11:57:53', 120.00, 1000, '{\"cols\": 35, \"rows\": 30}', 1);
INSERT INTO `performance_event` VALUES (6, '青年演员选拔赛', '市文化馆', '2026-02-07 11:57:53', 20.00, 300, '{\"cols\": 20, \"rows\": 15}', 1);
INSERT INTO `performance_event` VALUES (7, '大师公开课', '非遗博物馆报告厅', '2026-01-30 11:57:53', 0.00, 150, '{\"cols\": 15, \"rows\": 10}', 1);
INSERT INTO `performance_event` VALUES (8, '惠民演出下乡', '李家村大舞台', '2026-01-26 11:57:53', 0.00, 500, '{\"cols\": 25, \"rows\": 20}', 1);
INSERT INTO `performance_event` VALUES (9, '《墙头记》复排首演', '人民剧院', '2026-02-22 11:57:53', 100.00, 600, '{\"cols\": 30, \"rows\": 20}', 0);
INSERT INTO `performance_event` VALUES (10, '戏曲体验工坊', '文创园区', '2026-01-31 11:57:53', 50.00, 50, '{\"cols\": 10, \"rows\": 5}', 1);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_log
-- ----------------------------

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `sub_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题',
  `main_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL COMMENT '库存数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-在售, 0-下架',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情(HTML)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文创商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, '柳琴戏Q版人物手办', '一套5个，造型可爱', 'https://via.placeholder.com/300x300?text=Product1', 199.00, 50, 1, '树脂材质，手工上色', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (2, '戏曲脸谱书签', '金属镂空工艺', 'https://via.placeholder.com/300x300?text=Product2', 29.90, 200, 1, '黄铜材质，精美礼盒装', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (3, '手工刺绣团扇', '非遗传承人手作', 'https://via.placeholder.com/300x300?text=Product3', 158.00, 20, 1, '双面绣，苏绣工艺', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (4, '经典剧目DVD珍藏版', '高清修复，含幕后花絮', 'https://via.placeholder.com/300x300?text=Product4', 88.00, 100, 1, '内含3张光盘及精美画册', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (5, '戏曲元素帆布袋', '时尚百搭，容量大', 'https://via.placeholder.com/300x300?text=Product5', 39.00, 300, 1, '12安帆布，热转印图案', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (6, '柳琴戏主题T恤', '纯棉舒适，国潮设计', 'https://via.placeholder.com/300x300?text=Product6', 69.00, 150, 1, '男女同款，多尺码可选', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (7, '精制柳琴乐器模型', '1:10比例还原', 'https://via.placeholder.com/300x300?text=Product7', 128.00, 30, 1, '木质模型，不可弹奏', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (8, '戏曲人物钥匙扣', '亚克力材质', 'https://via.placeholder.com/300x300?text=Product8', 15.00, 500, 1, '双面图案，高清印刷', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (9, '复古戏票笔记本', '牛皮纸内页', 'https://via.placeholder.com/300x300?text=Product9', 25.00, 100, 1, '线装本，适合手账', '2026-01-23 11:57:53');
INSERT INTO `product` VALUES (10, '非遗文化明信片', '一套10张', 'https://via.placeholder.com/300x300?text=Product10', 19.90, 200, 1, '特种纸印刷，色彩还原度高', '2026-01-23 11:57:53');

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
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-传承人, 2-管理员',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'user01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user01', '13800138001', 'user01@example.com', 0, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (2, 'user02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user02', '13800138002', 'user02@example.com', 0, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (3, 'user03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user03', '13800138003', 'user03@example.com', 0, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (4, 'user04', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user04', '13800138004', 'user04@example.com', 0, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (5, 'user05', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '孙七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user05', '13800138005', 'user05@example.com', 0, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (6, 'inheritor01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '陈大师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=inh01', '13900139001', 'inh01@example.com', 1, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (7, 'inheritor02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '林老艺人', 'https://api.dicebear.com/7.x/avataaars/svg?seed=inh02', '13900139002', 'inh02@example.com', 1, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (8, 'inheritor03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '黄传承', 'https://api.dicebear.com/7.x/avataaars/svg?seed=inh03', '13900139003', 'inh03@example.com', 1, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (9, 'auditor01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '周审核', 'https://api.dicebear.com/7.x/avataaars/svg?seed=audit01', '13700137001', 'audit01@example.com', 2, 1, '2026-01-23 11:57:53');
INSERT INTO `sys_user` VALUES (10, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcLRch.G.q.e', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '13600136001', 'admin@example.com', 3, 1, '2026-01-23 11:57:53');

-- ----------------------------
-- Table structure for ticket_order
-- ----------------------------
DROP TABLE IF EXISTS `ticket_order`;
CREATE TABLE `ticket_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号(唯一)',
  `user_id` bigint NOT NULL COMMENT '购票用户ID',
  `event_id` bigint NOT NULL COMMENT '演出ID',
  `seat_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位信息(如: 1排5座)',
  `price` decimal(10, 2) NOT NULL COMMENT '成交价格',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待支付, 1-已支付, 2-已核销, 3-已取消',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '核销二维码URL',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '演出票务订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ticket_order
-- ----------------------------
INSERT INTO `ticket_order` VALUES (1, 'ORD20240101001', 1, 1, '3排5座', 80.00, 1, 'https://via.placeholder.com/150?text=QR1', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (2, 'ORD20240101002', 1, 2, '5排8座', 50.00, 1, 'https://via.placeholder.com/150?text=QR2', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (3, 'ORD20240101003', 2, 1, '3排6座', 80.00, 0, NULL, NULL, '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (4, 'ORD20240101004', 2, 5, '10排12座', 120.00, 1, 'https://via.placeholder.com/150?text=QR4', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (5, 'ORD20240101005', 3, 2, '6排6座', 50.00, 2, 'https://via.placeholder.com/150?text=QR5', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (6, 'ORD20240101006', 3, 6, '8排1座', 20.00, 1, 'https://via.placeholder.com/150?text=QR6', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (7, 'ORD20240101007', 4, 1, '4排5座', 80.00, 3, NULL, NULL, '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (8, 'ORD20240101008', 4, 5, '10排13座', 120.00, 1, 'https://via.placeholder.com/150?text=QR8', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (9, 'ORD20240101009', 5, 2, '7排7座', 50.00, 1, 'https://via.placeholder.com/150?text=QR9', '2026-01-23 11:57:53', '2026-01-23 11:57:53');
INSERT INTO `ticket_order` VALUES (10, 'ORD20240101010', 5, 10, '2排3座', 50.00, 1, 'https://via.placeholder.com/150?text=QR10', '2026-01-23 11:57:53', '2026-01-23 11:57:53');

SET FOREIGN_KEY_CHECKS = 1;
