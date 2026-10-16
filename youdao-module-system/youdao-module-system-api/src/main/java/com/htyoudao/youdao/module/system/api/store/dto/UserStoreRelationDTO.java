package com.htyoudao.youdao.module.system.api.store.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户 × 门店关系 DTO
 *
 * <p>用于经营异常提醒的批量查询：传一批 userIds，
 * 返回每个人分别管辖哪些门店。
 *
 * @author anomaly-reminder
 */
@Data
public class UserStoreRelationDTO implements Serializable {

    /**
     * 序列化版本号，用于跨服务 RPC 序列化兼容
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，对应 system_users.id
     */
    private Long userId;

    /**
     * 门店ID，对应 system_store_info.store_id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;
}
