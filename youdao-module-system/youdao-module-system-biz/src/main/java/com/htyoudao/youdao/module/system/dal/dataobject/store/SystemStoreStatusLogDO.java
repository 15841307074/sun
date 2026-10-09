package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 门店经营状态记录表 DO
 *
 * @author ssz
 */
@TableName(value = "system_store_status_log", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreStatusLogDO extends BusinessBaseDO {
    /**
     * 自增编号
     */
    @TableId
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店状态（0 正常营业 1 闭店 2 临时闭店）
     */
    private Integer storeStatus;
}
