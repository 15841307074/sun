package com.htyoudao.youdao.module.system.dal.dataobject.orguser;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 组织和用户关联 DO
 *
 * @author 零零玖零
 */
@TableName("system_org_user")
@KeySequence("system_org_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgUserDO extends BusinessBaseDO {

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
     * 组织ID
     */
    private Long orgId;
    /**
     * 类型 1. 负责人 0.普通
     */
    private Integer type;
    /**
     * 组织  用户是否可见 0不可见 1可见
     */
    private Integer visible;

}