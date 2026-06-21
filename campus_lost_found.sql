/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : campus_lost_found

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 21/06/2026 19:48:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for claim_record
-- ----------------------------
DROP TABLE IF EXISTS `claim_record`;
CREATE TABLE `claim_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lost_item_id` bigint NOT NULL COMMENT '拾物ID',
  `claimer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '领取人姓名',
  `claimer_identity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '领取人学号/工号',
  `claimer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '领取人电话',
  `handler_id` bigint NOT NULL COMMENT '办理人员ID(staff)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '领取备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lost_item_id`(`lost_item_id` ASC) USING BTREE,
  INDEX `idx_handler_id`(`handler_id` ASC) USING BTREE,
  INDEX `idx_claimer_identity`(`claimer_identity` ASC) USING BTREE,
  CONSTRAINT `fk_claim_lost_item` FOREIGN KEY (`lost_item_id`) REFERENCES `lost_item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '领取记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of claim_record
-- ----------------------------
INSERT INTO `claim_record` VALUES (1, 2, '张三', '2023108', '', 1, '', '2026-06-04 16:35:35');

-- ----------------------------
-- Table structure for lost_item
-- ----------------------------
DROP TABLE IF EXISTS `lost_item`;
CREATE TABLE `lost_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物品图片URL',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI识别类别(中文)',
  `category_confidence` double NULL DEFAULT NULL COMMENT 'YOLO置信度(0~1)',
  `storage_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存放位置',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注(捡拾信息)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNCLAIMED' COMMENT '状态: UNCLAIMED(未领取)/CLAIMED(已领取)',
  `text_embedding` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文本向量缓存(JSON数组)',
  `image_phash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片感知哈希(重复检测)',
  `create_user_id` bigint NOT NULL COMMENT '登记人ID(staff)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_create_user_id`(`create_user_id` ASC) USING BTREE,
  INDEX `idx_status_category`(`status` ASC, `category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拾物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lost_item
-- ----------------------------
INSERT INTO `lost_item` VALUES (2, 'https://campus-lost-found-unn.oss-cn-guangzhou.aliyuncs.com/2026/06/04/41c9d1a3-71b5-43f3-876b-6f1766cbebb5.png', '绿色水杯', 0.8706, '游客中心失物招领处', '', 'CLAIMED', NULL, NULL, 1, '2026-06-04 16:13:19', '2026-06-04 16:35:35');
INSERT INTO `lost_item` VALUES (3, '/uploads/2026/06/09/f91956e6-4785-4c37-965b-e2e684a067a5.jpg', '黑色键盘', 0.8666, 'a', '图书馆拾取', 'UNCLAIMED', NULL, NULL, 1, '2026-06-09 10:30:13', '2026-06-09 10:30:13');
INSERT INTO `lost_item` VALUES (4, '/uploads/2026/06/21/e563991e-c040-471f-975a-e76cb58074d0.jpg', '橙色塑料鼠标', 0.95, '工科楼', '', 'UNCLAIMED', NULL, NULL, 1, '2026-06-21 17:07:57', '2026-06-21 17:07:57');

-- ----------------------------
-- Table structure for match_confirm
-- ----------------------------
DROP TABLE IF EXISTS `match_confirm`;
CREATE TABLE `match_confirm`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lost_item_id` bigint NOT NULL COMMENT '拾物ID',
  `user_id` bigint NOT NULL COMMENT '确认人ID(失主)',
  `match_score` double NULL DEFAULT NULL COMMENT '匹配度(0~1)',
  `confirm_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lost_item_id`(`lost_item_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_confirm_lost_item` FOREIGN KEY (`lost_item_id`) REFERENCES `lost_item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_confirm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '确认匹配表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of match_confirm
-- ----------------------------
INSERT INTO `match_confirm` VALUES (1, 2, 3, 0, '2026-06-04 16:29:29', '2026-06-04 16:29:29');
INSERT INTO `match_confirm` VALUES (2, 3, 3, 0.5, '2026-06-09 10:33:49', '2026-06-09 10:33:49');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置说明',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'contact_address', '游客中心失物招领处（游客中心一楼）', '失物招领处地址', '2026-06-04 14:17:56');
INSERT INTO `system_config` VALUES (2, 'contact_phone', '010-12345678', '失物招领处电话', '2026-06-04 14:17:56');
INSERT INTO `system_config` VALUES (3, 'working_hours', '周一至周五 9:00-17:00', '工作时间', '2026-06-04 14:17:56');
INSERT INTO `system_config` VALUES (4, 'claim_notice', '请携带本人学生证/工作证前来领取', '领取须知', '2026-06-04 14:17:56');
INSERT INTO `system_config` VALUES (5, 'deepseek_api_key', '', 'DeepSeek API密钥', '2026-06-04 14:17:56');
INSERT INTO `system_config` VALUES (6, 'yolo_service_url', 'http://ai-service:5000', 'YOLO服务地址', '2026-06-04 14:17:56');

-- ----------------------------
-- Table structure for system_log
-- ----------------------------
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `log_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志类型: API/AI/SYSTEM',
  `log_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO' COMMENT '级别: INFO/WARN/ERROR',
  `request_uri` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URI',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数(JSON)',
  `response_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应体(JSON)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求IP',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `duration_ms` bigint NULL DEFAULT NULL COMMENT '执行耗时(ms)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_log_type`(`log_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_log
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名(学号/工号)',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt加密密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色: ROLE_USER/ROLE_STAFF/ROLE_ADMIN',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用(1=是,0=否)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2b$12$UnjAEFnDdveJSWHh5EATduy6dVz5rrqpcYZ2rY73GlN9Dn8iBMzIq', '系统管理员', NULL, NULL, 'ROLE_ADMIN', 1, '2026-06-04 14:17:56', '2026-06-04 15:06:27');
INSERT INTO `user` VALUES (2, 'staff01', '$2b$12$EqAZA/IYNZzrh7Jcjhh3y.z/Is4wbU2QeXraXSTvNxd/XaebgTXUO', '招领处工作人员', NULL, NULL, 'ROLE_STAFF', 1, '2026-06-04 14:17:56', '2026-06-04 15:06:27');
INSERT INTO `user` VALUES (3, '2023108', '$2a$10$Rc1ToNC30Euk6ulqT/p1O.LDsYkcL40.aGFs3k9rtY2CKSw8t6myC', '', '', NULL, 'ROLE_USER', 1, '2026-06-04 15:01:09', '2026-06-04 15:01:09');

SET FOREIGN_KEY_CHECKS = 1;
