package com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 模板可见成员 DO
 *
 * @author 超级管理员
 */
@TableName("system_template_visible_user")
@KeySequence("system_template_visible_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVisibleUserDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long visibilityId;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;
    /**
     * 模板ID
     */
    private Long templateId;

}