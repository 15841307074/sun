package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 门店骑手信息表关系表 DO
 *
 * @author ssz
 */
@TableName(value = "system_store_delivery", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreDeliveryDO extends BaseDO {
    /**
     * 骑手编号
     */
    @TableId
    private Integer deliveryId;

    /**
     * 绑定门店ID
     */
    private Long storeId;

    /**
     * 骑手姓名
     */
    private String deliveryName;

    /**
     * 骑手电话
     */
    private String deliveryPhone;

}
