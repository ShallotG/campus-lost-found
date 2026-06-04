-- ============================================================
-- 智能校园失物招领平台 - 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_lost_found
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- -------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------
DROP TABLE IF EXISTS `match_confirm`;
DROP TABLE IF EXISTS `claim_record`;
DROP TABLE IF EXISTS `lost_item`;
DROP TABLE IF EXISTS `system_log`;
DROP TABLE IF EXISTS `system_config`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名(学号/工号)',
    `password`      VARCHAR(200)    NOT NULL                 COMMENT 'BCrypt加密密码',
    `real_name`     VARCHAR(50)     DEFAULT NULL             COMMENT '真实姓名',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT '联系电话',
    `email`         VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER'
                        COMMENT '角色: ROLE_USER/ROLE_STAFF/ROLE_ADMIN',
    `enabled`       TINYINT(1)      NOT NULL DEFAULT 1      COMMENT '是否启用(1=是,0=否)',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP         COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表';

-- -------------------------------------------------------
-- 2. 拾物表
-- -------------------------------------------------------
CREATE TABLE `lost_item` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `image_url`            VARCHAR(500) DEFAULT NULL             COMMENT '物品图片URL',
    `category`             VARCHAR(50)  DEFAULT NULL             COMMENT 'AI识别类别(中文)',
    `category_confidence`  DOUBLE       DEFAULT NULL             COMMENT 'YOLO置信度(0~1)',
    `storage_location`     VARCHAR(200) NOT NULL                 COMMENT '存放位置',
    `remark`               TEXT         DEFAULT NULL             COMMENT '备注(捡拾信息)',
    `status`               VARCHAR(20)  NOT NULL DEFAULT 'UNCLAIMED'
                            COMMENT '状态: UNCLAIMED(未领取)/CLAIMED(已领取)',
    `text_embedding`       MEDIUMTEXT   DEFAULT NULL             COMMENT '文本向量缓存(JSON数组)',
    `image_phash`          VARCHAR(64)  DEFAULT NULL             COMMENT '图片感知哈希(重复检测)',
    `create_user_id`       BIGINT       NOT NULL                 COMMENT '登记人ID(staff)',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP         COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user_id` (`create_user_id`),
    KEY `idx_status_category` (`status`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='拾物表';

-- -------------------------------------------------------
-- 3. 领取记录表
-- -------------------------------------------------------
CREATE TABLE `claim_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `lost_item_id`      BIGINT          NOT NULL                 COMMENT '拾物ID',
    `claimer_name`      VARCHAR(50)     NOT NULL                 COMMENT '领取人姓名',
    `claimer_identity`  VARCHAR(50)     NOT NULL                 COMMENT '领取人学号/工号',
    `claimer_phone`     VARCHAR(20)     DEFAULT NULL             COMMENT '领取人电话',
    `handler_id`        BIGINT          NOT NULL                 COMMENT '办理人员ID(staff)',
    `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT '领取备注',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    PRIMARY KEY (`id`),
    KEY `idx_lost_item_id` (`lost_item_id`),
    KEY `idx_handler_id` (`handler_id`),
    KEY `idx_claimer_identity` (`claimer_identity`),
    CONSTRAINT `fk_claim_lost_item` FOREIGN KEY (`lost_item_id`)
        REFERENCES `lost_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='领取记录表';

-- -------------------------------------------------------
-- 4. 确认匹配表
-- -------------------------------------------------------
CREATE TABLE `match_confirm` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `lost_item_id`  BIGINT          NOT NULL                 COMMENT '拾物ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '确认人ID(失主)',
    `match_score`   DOUBLE          DEFAULT NULL             COMMENT '匹配度(0~1)',
    `confirm_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_lost_item_id` (`lost_item_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_confirm_lost_item` FOREIGN KEY (`lost_item_id`)
        REFERENCES `lost_item` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_confirm_user` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='确认匹配表';

-- -------------------------------------------------------
-- 5. 系统配置表
-- -------------------------------------------------------
CREATE TABLE `system_config` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `config_key`    VARCHAR(100)    NOT NULL                 COMMENT '配置键',
    `config_value`  TEXT            NOT NULL                 COMMENT '配置值',
    `description`   VARCHAR(200)    DEFAULT NULL             COMMENT '配置说明',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统配置表';

-- -------------------------------------------------------
-- 6. 系统日志表
-- -------------------------------------------------------
CREATE TABLE `system_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `log_type`      VARCHAR(20)     NOT NULL                 COMMENT '日志类型: API/AI/SYSTEM',
    `log_level`     VARCHAR(10)     NOT NULL DEFAULT 'INFO'  COMMENT '级别: INFO/WARN/ERROR',
    `request_uri`   VARCHAR(200)    DEFAULT NULL             COMMENT '请求URI',
    `request_method` VARCHAR(10)    DEFAULT NULL             COMMENT '请求方法',
    `request_params` TEXT           DEFAULT NULL             COMMENT '请求参数(JSON)',
    `response_body`  TEXT           DEFAULT NULL             COMMENT '响应体(JSON)',
    `ip_address`     VARCHAR(50)    DEFAULT NULL             COMMENT '请求IP',
    `user_id`        BIGINT         DEFAULT NULL             COMMENT '操作用户ID',
    `duration_ms`    BIGINT         DEFAULT NULL             COMMENT '执行耗时(ms)',
    `error_message`  TEXT           DEFAULT NULL             COMMENT '错误信息',
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统日志表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认管理员: admin / admin123 (BCrypt密文)
-- 默认工作人员: staff01 / staff123
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('admin',   '$2b$12$UnjAEFnDdveJSWHh5EATduy6dVz5rrqpcYZ2rY73GlN9Dn8iBMzIq', '系统管理员',     'ROLE_ADMIN'),
('staff01', '$2b$12$EqAZA/IYNZzrh7Jcjhh3y.z/Is4wbU2QeXraXSTvNxd/XaebgTXUO', '招领处工作人员', 'ROLE_STAFF');

-- 系统配置初始值
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('contact_address',  'XX大学图书馆一楼101室',          '失物招领处地址'),
('contact_phone',    '010-12345678',                   '失物招领处电话'),
('working_hours',    '周一至周五 9:00-17:00',          '工作时间'),
('claim_notice',     '请携带本人学生证/工作证前来领取', '领取须知'),
('deepseek_api_key', '',                               'DeepSeek API密钥'),
('yolo_service_url', 'http://ai-service:5000',          'YOLO服务地址');
