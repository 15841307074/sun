package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * @author dht
 */
@Data
@Schema(name = "短信营销查询分页入参", description = "短信营销查询分页入参")
@ToString(callSuper = true)
public class SmsMarketReqVO extends PageParam {
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
    @Schema(description = "发送人群  0 老客户促活 1自定义人群")
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
     * 起始发送时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "起始发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date startSendTime;

    /**
     * 结束发送时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date endSendTime;

    /**
     * 指定门店的id
     */
    @Schema(description = "指定门店的id")
    private Long desStoreId;

    /**
     * 指定区域id
     */
    @Schema(description = "指定区域id")
    private Long desDeptId;

    /**
     * 发送方式 0 立即发送 1定时发送
     */
    @Schema(description = "发送方式 0 立即发送 1定时发送")
    private Integer sendMethod;
}
