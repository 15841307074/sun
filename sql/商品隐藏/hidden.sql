ALTER TABLE commodity_spus
    ADD COLUMN is_hidden TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '是否隐藏 1是 0否'
        AFTER wx_status;

ALTER TABLE commodity_category
    ADD COLUMN is_hidden TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '是否隐藏 1是 0否'
        AFTER status;

------------回滚脚本-----------
ALTER TABLE commodity_spus
DROP COLUMN is_hidden;

ALTER TABLE commodity_category
DROP COLUMN is_hidden;