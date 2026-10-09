package com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 模板点检项 DO
 *
 * @author 超级管理员
 */
@TableName("system_template_checklist")
@KeySequence("system_template_checklist_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateChecklistDO extends BusinessBaseDO {

    /**
     * 模板点检项ID
     */
    @TableId
    private Long templateChecklistId;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 大类ID
     */
    private Long typeId;
    /**
     * 大类名称
     */
    private String typeName;
    /**
     * 点检项ID
     */
    private Long checklistId;
    /**
     * 点检项名称
     */
    private String title;
    /**
     * 提示
     */
    private String prompt;
    /**
     * 总分
     */
    private Integer score;
    /**
     * 整改有效期
     */
    private Integer deadline;
    /**
     * 图片规则	{	"imgFlag":"0 不允许上传 1 允许上传",	"imgState":"0 不必传 1 合格时必传 2 不合格时必传 3 不适用时必传",	"imgCount":"1",	"localImgFlag":"0 不允许本地上传 1 允许本地上传"	}
     */
    private String imgRule= "{\"imgFlag\":1,\"imgState\":0,\"imgCount\":1,\"localImgFlag\":1}";
    /**
     * 描述规则	{	"descriptionFlag":"0 不允许填写 1 允许填写",	"qualified":"0 选填 1 必填",	"unqualified":"0 选填 1 必填",	"inapplicability":"0 选填 1 必填"	}
     */
    private String descriptionRule = "{\"descriptionFlag\":0}";
    /**
     * 不适用规则	{	"showFlag":"0 不显示 1显示"	}
     */
    private String inapplicabilityRule = "{\"showFlag\":1}";
    /**
     * 奖惩规则	{	"rewardPunishmentFlag":"0 不填 1填写",	"qualified":"0 选填 1 必填",	"unqualified":"0 选填 1 必填"	}
     */
    private String rewardPunishmentRule = "{\"rewardPunishmentFlag\":0}";
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 大类排序
     */
    private Integer typeSort;
}