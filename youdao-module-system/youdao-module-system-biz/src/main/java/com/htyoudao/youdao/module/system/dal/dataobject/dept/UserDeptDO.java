package com.htyoudao.youdao.module.system.dal.dataobject.dept;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户部门关系表
 *
 * @author dht
 */
@TableName("system_user_dept")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeptDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(value = "user_dept_id", type = IdType.ASSIGN_ID)
    private Long userDeptId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 用户的权限类型  0 负责人 1 普通
     */
    private Integer type;
}
