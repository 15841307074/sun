package com.htyoudao.youdao.module.infra.dal.dataobject.group;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 群 DO
 *
 * @author 超级管理员
 */
@TableName("infra_group")
@KeySequence("infra_group_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 群名字
     */
    private String name;
    /**
     * 群主id
     */
    private Long ownerId;
    /**
     * 群头像
     */
    private String headImage;
    /**
     * 群头像缩略图
     */
    private String headImageThumb;
    /**
     * 群公告
     */
    private String notice;
    /**
     * 是否被封禁 0:否 1:是
     */
    private Boolean isBanned;
    /**
     * 被封禁原因
     */
    private String reason;
    /**
     * 是否已解散
     */
    private Boolean dissolve;

}
