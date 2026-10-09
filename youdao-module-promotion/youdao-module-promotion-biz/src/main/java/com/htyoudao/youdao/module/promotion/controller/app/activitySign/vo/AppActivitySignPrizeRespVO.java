package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "小程序 - 签到奖励 Response VO")
@Data
public class AppActivitySignPrizeRespVO {

    @Schema(description = "奖励发放记录ID；仅签到成功返回已发放奖励时有值，填写实物收货地址请传该字段")
    private Long rewardRecordId;

    @Schema(description = "奖品ID/奖励配置ID")
    private Long prizeId;

    @Schema(description = "奖品类型，复用ActivityCqPrizeTypeEnum：1优惠券 2积分 3实物 5现金红包 6优惠券包")
    private Integer prizeType;

    @Schema(description = "奖品内容/奖励名称")
    private String prizeContent;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品值：积分数/红包金额等")
    private BigDecimal prizeValue;

    @Schema(description = "签到规则：1每日签到 2连续签到 3累计签到")
    private Integer signRuleType;

    @Schema(description = "连续/累计天数")
    private Integer signDays;

    @Schema(description = "当前用户是否已获得该奖励；用于奖励按钮展示待完成/已完成")
    private Boolean obtained;

    @Schema(description = "距离获得该奖励还差几天；已获得时为0，每日签到奖励可为空")
    private Integer needDays;

    @Schema(description = "红包领取凭证/领取信息，非红包为空；签到成功当次返回给前端调微信领取")
    private String packageInfo;

    @Schema(description = "红包领取状态：1未领取 2已领取 3已过期/已失效；非红包为空")
    private Integer claimStatus;

    @Schema(description = "实物奖品状态：1填写地址 2待发货 3待收货 9超时未填写；非实物为空")
    private Integer prizeState;

}
