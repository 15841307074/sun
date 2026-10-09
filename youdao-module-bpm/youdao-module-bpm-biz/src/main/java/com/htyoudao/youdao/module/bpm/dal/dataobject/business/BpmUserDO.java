package com.htyoudao.youdao.module.bpm.dal.dataobject.business;

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
public class BpmUserDO extends TenantBaseDO {

    /**
     * 用户ID
     */
    @TableId
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 手机号码
     */
    private String mobile;


}
