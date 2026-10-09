package com.htyoudao.youdao.module.promotion.dal.dataobject.market;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 短信营销
 * </p>
 *
 * @author dht
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "营销短信实体", description = "营销短信实体")
@TableName("sms_market")
public class SmsMarketDO extends BusinessBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 计划名称
     */
    @Schema(description = "计划名称")
    private String planName;

    /**
     * 发送场景 0客户促活
     */
    @Schema(description = "发送场景 0客户促活")
    private Integer sendingScenario;

    /**
     * 指定门店 0 全部 1部分 2不指定
     */
    @Schema(description = "指定门店 0 全部 1部分 2不指定")
    private Integer desStore;

    /**
     * 指定区域 0全部 1部分 2不指定
     */
    @Schema(description = "指定区域 0全部 1部分 2不指定")
    private Integer desDept;

    /**
     * 发送人群  0 老客户促活
     */
    @Schema(description = "发送人群  0 老客户促活")
    private Integer sendPeople;

    @Schema(description = "人群id")
    private String crowdId;

    /**
     * 短信模版id
     */
    @Schema(description = "短信模版id")
    private Long templateId;

    @Schema(description = "模板内容")
    private String templateContent;

    @Schema(description = "模板内容")
    private String shortCode;

    /**
     * 优惠券包id
     */
    @Schema(description = "优惠券包id")
    private Long couponPackageId;

    /**
     * 发送状态 0已发送 1未发送
     */
    @Schema(description = "发送状态 0已发送 1未发送")
    private Integer sendStatus;

    /**
     * 发送方式 0 立即发送 1定时发送
     */
    @Schema(description = "发送方式 0 立即发送 1定时发送")
    private Integer sendMethod;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    /**
     * 发送人数
     */
    @Schema(description = "发送人数")
    private Integer sendNum;

    /**
     *  操作人
     */
    @Schema(description = "操作人")
    private String operUser;

    @TableField(exist = false)
    private List<SmsMarketStoreDO> smsMarketStores;
}
