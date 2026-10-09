package com.htyoudao.youdao.module.system.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.tenant.core.db.TenantBaseDO;
import com.htyoudao.youdao.module.system.enums.common.SexEnum;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 管理后台的用户 DO
 *
 * @author 0090
 */
@TableName(value = "system_users", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@KeySequence("system_users_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDO extends TenantBaseDO {

    /**
     * 用户ID
     */
    @TableId
    private Long id;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户等级(0-集团,1-区域,2-省,3-市,4 区 5-门店)
     */
    private Integer userLevel;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户类型（00系统用户，01 新增客户 02 供应链员工 03供应链客户员工）
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 用户性别
     * <p>
     * 枚举类 {@link SexEnum}
     */
    private Integer sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 加密后的密码
     * <p>
     * 因为目前使用 {@link BCryptPasswordEncoder} 加密器，所以无需自己处理 salt 盐
     */
    private String password;

    /**
     * 帐号状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 删除标志
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    /**
     * 创建者 废弃
     */
    private String createBy;

    /**
     * 更新者 废弃
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否是全部套餐 0是 1否
     */
    private Integer is_all_product;

    /**
     * 是否是全部商品 0是 1否
     */
    private Integer is_all_commdity;

    /**
     * 是否和配送路线相同 0：是 1：否
     */
    private Integer is_same_line;

    /**
     * 客户编号
     */
    private String user_code;

    /**
     * 上级用户ID
     */
    private Long super_user_id;

    /**
     * 上级用户名称
     */
    private String super_user_name;

    /**
     * 显示名称
     */
    private String reveal_name;

    /**
     * 项目归属 废弃
     */
    private Long project_owner_ship;

    /**
     * 是否是项目负责人 0是 1不是
     */
    private Integer isProject;

    /**
     * 岗位编号数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> postIds;

    /**
     * 1.总部账号 2.项目账号
     */
    private Integer type;

}
