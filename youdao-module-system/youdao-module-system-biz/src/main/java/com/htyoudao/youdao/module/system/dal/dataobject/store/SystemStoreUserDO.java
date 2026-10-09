package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 门店用户关系表 DO
 *
 * @author ssz
 */
@TableName(value = "system_store_user", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreUserDO extends BusinessBaseDO {
    /**
     * 自增编号
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 所属组织id
     */
    private Long orgId;

    /**
     * 类型 1. 店长
     */
    private Integer type;
    /**
     * 门店是否可见 0不可见 1可见
     */
    private Integer visible;
}
