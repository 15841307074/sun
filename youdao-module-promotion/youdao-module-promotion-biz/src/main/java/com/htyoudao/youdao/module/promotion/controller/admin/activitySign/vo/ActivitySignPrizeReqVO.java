package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 签到活动奖励配置 Request VO")
@Data
public class ActivitySignPrizeReqVO {

    @Schema(description = "奖励配置ID；新增不传，修改传", example = "9001")
    private Long id;

    @Schema(description = "奖品类型，复用ActivityCqPrizeTypeEnum：1优惠券 2积分 3实物 5现金红包 6优惠券包", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "奖品类型不能为空")
    private Integer prizeType;

    @Schema(description = "奖品业务ID，如券ID、券包ID、实物ID；积分/红包可为空", example = "123")
    private Long prizeId;

    @Schema(description = "奖品内容/奖励名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日签到积分")
    @NotNull(message = "奖品内容不能为空")
    private String prizeContent;

    @Schema(description = "优惠券/优惠券包名称；仅详情回显使用，前端创建/修改传了也不入库")
    private String couponName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品值：积分数/红包金额等，红包保留4位小数", example = "10.0000")
    private BigDecimal prizeValue;

    @Schema(description = "签到规则：1每日签到 2连续签到 3累计签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "签到规则不能为空")
    private Integer signRuleType;

    @Schema(description = "连续/累计天数；signRuleType=2或3时必传", example = "7")
    private Integer signDays;

    @Schema(description = "发放方式：1当日发放 2次日发放；第一期默认1", example = "1")
    private Integer grantMode;

}
