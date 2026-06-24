-- =====================================================
-- 校园失物招领平台 - 新增表 SQL
-- 在已有数据库 campus_lost_found 中执行
-- =====================================================

-- 1. 寻物启事表
CREATE TABLE IF NOT EXISTS `found_item` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '发布人ID',
  `item_name` VARCHAR(200) NOT NULL COMMENT '物品名称',
  `category` VARCHAR(100) COMMENT '物品类别',
  `color` VARCHAR(50) COMMENT '颜色',
  `brand` VARCHAR(100) COMMENT '品牌',
  `description` TEXT COMMENT '详细描述',
  `lost_time` DATETIME COMMENT '丢失时间',
  `lost_location` VARCHAR(200) COMMENT '丢失地点',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态: OPEN/MATCHED/CLOSED',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻物启事';

-- 2. 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `type` VARCHAR(50) COMMENT '通知类型: MATCH_CONFIRM/ITEM_CLAIMED/SYSTEM',
  `title` VARCHAR(200) COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `related_item_id` BIGINT COMMENT '关联物品ID',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_unread` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知';
