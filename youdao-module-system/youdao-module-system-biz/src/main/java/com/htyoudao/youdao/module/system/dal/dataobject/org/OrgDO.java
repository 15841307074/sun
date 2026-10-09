package com.htyoudao.youdao.module.system.dal.dataobject.org;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 组织机构 DO
 *
 * @author 零零玖零
 */
@TableName("system_org")
//@KeySequence("system_org_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgDO extends BusinessBaseDO {

    /**
     * 组织ID
     */
    @TableId
    private Long id;
    /**
     * 组织名称
     */
    private String name;
    /**
     * 祖级列表
     */
    private String ancestors;
    /**
     * 上级ID
     */
    private Long parentId;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 组织类型
     */
    private Integer type;
    /**
     * 组织编号
     */
    private String code;
    /**
     * 状态 0-未启用 1-启用
     */
    private Integer status;
    /**
     * 层级编号
     */
    private Integer level;


}