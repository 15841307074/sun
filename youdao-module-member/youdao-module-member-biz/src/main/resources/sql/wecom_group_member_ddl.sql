-- 企业微信群成员关系表
CREATE TABLE IF NOT EXISTS wecom_group_member (
id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
chat_id VARCHAR(255) NOT NULL COMMENT '群ID',
external_userid VARCHAR(255) NOT NULL COMMENT '成员ID',
union_id VARCHAR(255) COMMENT '成员ID',
member_type TINYINT NOT NULL DEFAULT 1 COMMENT '成员类型：1-企业成员 2-外部联系人',
`creator` varchar(64) COLLATE utf8mb4_unicode_ci   DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`business_id` bigint(20) ,
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
INDEX idx_chat_id (chat_id),
INDEX idx_user_id (external_userid),
INDEX idx_union_id (union_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信群成员关系表';
