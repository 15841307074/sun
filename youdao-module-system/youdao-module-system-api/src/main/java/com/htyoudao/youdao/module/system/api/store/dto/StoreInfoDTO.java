package com.htyoudao.youdao.module.system.api.store.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author dht
 * 优惠券查询使用数据时候的门店数据
 */
@Data
public class StoreInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8093379322715745949L;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 所属仓库ID
     */
    private Long warehouseId;

    /**
     * 所属仓库名称
     */
    private String warehouseName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 删除标识
     */
    private Boolean deleted;
}
