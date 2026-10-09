package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketStoreDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Data
public class SmsMarketRespVO {


    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
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

    @Schema(description = "人群名称")
    private String crowdName;

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
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
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

    private List<SmsMarketStoreDO> smsMarketStores;

    /**
     *  适用区域
     */
    @Schema(description = "适用区域ids")
    private List<Long> desDeptIds;

    /**
     *  适用门店
     */
    @Schema(description = "适用门店ids")
    private List<Long> desStoreIds;

    /**
     *  适用区域
     */
    private List<Long> crowdIds;
}
