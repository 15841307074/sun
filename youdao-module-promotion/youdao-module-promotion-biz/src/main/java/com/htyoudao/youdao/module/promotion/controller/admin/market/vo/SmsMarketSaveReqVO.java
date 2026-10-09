package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDeptDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketStoreDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(name = "短信营销新增入参", description = "短信营销新增入参")
@ToString(callSuper = true)
public class SmsMarketSaveReqVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "起始发送时间")
    private Date startSendTime;

    /**
     * 结束发送时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束发送时间")
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

    /**
     *  适用区域
     */
    @Schema(description = "适用区域")
    private List<SmsMarketDeptDO> smsMarketDepts;

    /**
     *  适用门店
     */
    @Schema(description = "适用门店")
    private List<SmsMarketStoreDO> smsMarketStores;
}
