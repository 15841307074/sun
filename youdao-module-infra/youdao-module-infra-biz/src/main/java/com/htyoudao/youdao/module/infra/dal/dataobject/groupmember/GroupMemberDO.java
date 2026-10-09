package com.htyoudao.youdao.module.infra.dal.dataobject.groupmember;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 群成员 DO
 *
 * @author 超级管理员
 */
@TableName("infra_group_member")
@KeySequence("infra_group_member_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 群id
     */
    private Long groupId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String userNickName;
    /**
     * 显示昵称备注
     */
    private String remarkNickName;
    /**
     * 用户头像
     */
    private String headImage;
    /**
     * 显示群名备注
     */
    private String remarkGroupName;
    /**
     * 免打扰标识(do not disturb)  0:关闭   1:开启
     */
    private Boolean isDnd;
    /**
     * 是否已退出
     */
    private Boolean quit;
    /**
     * 退出时间
     */
    private LocalDateTime quitTime;

}
