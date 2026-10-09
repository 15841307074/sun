package com.htyoudao.youdao.module.system.dal.dataobject.storeuser;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 门店和用户关联 DO
 *
 * @author 零零玖零
 */
@TableName("system_store_user")
@KeySequence("system_store_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUserDO extends BusinessBaseDO {

    /**
     * 自增编号
     */
    @TableId
    private Long id;

    /**
     * 所属组织id
     */
    private Long orgId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 类型 1. 店长 
     */
    private Integer type;

    /**
     * 门店是否可见 0不可见 1可见
     */
    private Integer visible;
}