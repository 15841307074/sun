package com.htyoudao.youdao.module.system.dal.dataobject.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 项目 DO
 *
 * @author 零零玖零
 */
@TableName("system_business")
@KeySequence("system_business_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 项目编码;hk,ts
     */
    private String code;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 经营方式 0 自营 1 合作
     */
    private Integer manageType;
    /**
     * 状态;0-未启用 1-启用
     */
    private Integer status;
    /**
     * 有效期开始时间
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime validityStartTime;
    /**
     * 有效期结束时间
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime validityEndTime;
    /**
     * logo url
     */
    private String logoUrl;

    /**
     * 点餐登录页背景
     */
    private String dcUrl;

    /**
     * 描述
     */
    private String comment;
    /**
     * 菜单ID，多个逗号分隔
     */
    private String menuIds;
    /**
     * 项目负责人 id
     */
    private Long userId;

}