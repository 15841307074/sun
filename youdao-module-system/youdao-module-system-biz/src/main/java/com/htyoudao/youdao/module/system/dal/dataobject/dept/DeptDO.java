package com.htyoudao.youdao.module.system.dal.dataobject.dept;

import com.baomidou.mybatisplus.annotation.IdType;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.framework.tenant.core.db.TenantBaseDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表
 *
 * @author ruoyi
 * @author 0090
 */
@TableName("system_dept")
@KeySequence("system_dept_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptDO extends BusinessBaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 部门ID
     */
    @TableId(value = "id", type = IdType.NONE)
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门ID
     *
     * 关联 {@link #id}
     */
    private Long parentId;
    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 层级编号
     */
    private Integer level;

    /**
     * 祖级列表
     */
    private String ancestors;
//    /**
//     * 负责人
//     *
//     * 关联 {@link AdminUserDO#getId()}
//     */
//    private Long leaderUserId;
//    /**
//     * 联系电话
//     */
//    private String phone;
//    /**
//     * 邮箱
//     */
//    private String email;

    /**
     * 部门状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 能否操作 0能 1不能
     */
    private Integer updateFlag;
}
