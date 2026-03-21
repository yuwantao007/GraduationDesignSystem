-- 为答辩安排增加专业字段
-- 执行前建议备份 defense_arrangement 表

-- 兼容 MySQL 5.7/8.0 的写法：通过 information_schema 判断后再执行 DDL

SET @db_name = DATABASE();

-- 1) 列不存在时再新增 major_id
SELECT COUNT(1) INTO @major_id_col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'defense_arrangement'
    AND COLUMN_NAME = 'major_id';

SET @ddl_add_column = IF(
    @major_id_col_exists = 0,
    'ALTER TABLE defense_arrangement ADD COLUMN major_id VARCHAR(32) NULL COMMENT ''专业ID'' AFTER topic_category',
    'SELECT ''major_id column already exists'''
);

PREPARE stmt_add_column FROM @ddl_add_column;
EXECUTE stmt_add_column;
DEALLOCATE PREPARE stmt_add_column;

-- 2) 索引不存在时再新增 idx_major_id
SELECT COUNT(1) INTO @major_id_idx_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'defense_arrangement'
    AND INDEX_NAME = 'idx_major_id';

SET @ddl_add_index = IF(
    @major_id_idx_exists = 0,
    'ALTER TABLE defense_arrangement ADD INDEX idx_major_id (major_id)',
    'SELECT ''idx_major_id already exists'''
);

PREPARE stmt_add_index FROM @ddl_add_index;
EXECUTE stmt_add_index;
DEALLOCATE PREPARE stmt_add_index;

-- 可选：补齐历史数据后再改为非空
-- UPDATE defense_arrangement SET major_id = '请替换为实际专业ID' WHERE major_id IS NULL;
-- ALTER TABLE defense_arrangement MODIFY COLUMN major_id VARCHAR(32) NOT NULL COMMENT '专业ID';
