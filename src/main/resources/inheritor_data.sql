-- 批量生成传承人数据 (SysUser + InheritorProfile)
-- 请在数据库工具中执行此脚本

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_01', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '李桂芳', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13837094775', 'inheritor_01@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺15年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_02', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '王传扬', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13874247459', 'inheritor_02@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏 (东路)', '自幼喜爱柳琴戏 (东路)，师从名家，从艺22年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_03', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '张兴兰', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13894680846', 'inheritor_03@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺27年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_04', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '刘玉梅', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13817645672', 'inheritor_04@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺22年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_05', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '陈庆华', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13879152349', 'inheritor_05@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺49年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_06', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '赵启明', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13850492284', 'inheritor_06@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺10年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_07', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '孙晓云', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13839722186', 'inheritor_07@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺49年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_08', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '周建国', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13886385465', 'inheritor_08@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏 (东路)', '自幼喜爱柳琴戏 (东路)，师从名家，从艺30年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_09', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '吴秀英', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13847031690', 'inheritor_09@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺25年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_10', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '郑志强', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13821031390', 'inheritor_10@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏 (东路)', '自幼喜爱柳琴戏 (东路)，师从名家，从艺50年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_11', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '王丽萍', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13842276636', 'inheritor_11@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺36年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_12', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '李强', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13846292839', 'inheritor_12@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺18年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_13', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '张敏', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13879987688', 'inheritor_13@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺34年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_14', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '刘伟', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13814789026', 'inheritor_14@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺43年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_15', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '陈静', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13861651742', 'inheritor_15@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺15年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_16', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '赵军', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13885988859', 'inheritor_16@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺21年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_17', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '孙红', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13857187628', 'inheritor_17@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺47年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_18', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '周涛', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13888697077', 'inheritor_18@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺39年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_19', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '吴刚', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13873733184', 'inheritor_19@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺44年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_20', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '郑丽', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13827517849', 'inheritor_20@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺33年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_21', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '冯巩', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13882242075', 'inheritor_21@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏 (东路)', '自幼喜爱柳琴戏 (东路)，师从名家，从艺35年。多次获得县级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_22', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '郭德纲', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13819662034', 'inheritor_22@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺26年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_23', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '于谦', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13832932787', 'inheritor_23@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺49年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_24', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '岳云鹏', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13860363076', 'inheritor_24@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺39年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_25', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '孙越', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13872475623', 'inheritor_25@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '省级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺18年。多次获得省级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_26', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '郭麒麟', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13870411368', 'inheritor_26@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '市级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺35年。多次获得市级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_27', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '阎鹤祥', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13865121686', 'inheritor_27@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺17年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_28', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '张云雷', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13865612009', 'inheritor_28@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺44年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_29', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '杨九郎', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13880064351', 'inheritor_29@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '国家级', '柳琴戏', '自幼喜爱柳琴戏，师从名家，从艺28年。多次获得国家级比赛金奖。', 1);

INSERT INTO sys_user (username, password, real_name, avatar, phone, email, role, status, created_time) VALUES ('inheritor_30', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPLP0CZIK.2', '秦霄贤', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', '13891925437', 'inheritor_30@example.com', 1, 1, NOW());
SET @uid = LAST_INSERT_ID();
INSERT INTO inheritor_profile (user_id, level, genre, artistic_career, verify_status) VALUES (@uid, '县级', '柳琴戏 (西路)', '自幼喜爱柳琴戏 (西路)，师从名家，从艺33年。多次获得县级比赛金奖。', 1);

