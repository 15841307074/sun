package com.htyoudao.youdao.module.promotion.dal.dataobject.activity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.io.Serial;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 活动表实体类
 */
@TableName(value = "activity", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059504L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 是否优惠叠加（0-否，1-是）
     */
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）逗号分隔
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String stackableActivities;

    /**
     * 活动备注（最多200字）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String activityRemark;

    /**
     * 是否全门店参与（0-否，1-是）
     */
    private Integer activityStore = 0;

    /**
     * 参与人群 推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）
     */
    private Integer participantGroup;

    /**
     * 选择人群（存储人群ID，逗号分割）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String selectedGroups;

    /**
     * 活动规则（富文本内容）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String activityRules;

    /**
     * 是否开启（0不开启 1开启）
     */
    private Integer isEnabled = 0;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endDate;

    /**
     * 指定日期逗号分割
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String dayNumbers;

    /**
     * 指定周几逗号分割
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String weekNumbers;

    /**
     * 指定时间段
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String timeRange;

    @Schema(description = "社群专享 1 不开启  2 开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "应用范围 0 门店 1 标签")
    private Integer appScope;


}
