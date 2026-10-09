package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户抽奖记录查询 vo
 */
@Data
// 兼容已保存的旧结果 JSON，不再向前端输出内部抽奖处理状态。
@JsonIgnoreProperties("drawStatus")
public class LotteryUserLogVO {
    private String requestId;
    /** 发奖状态：待处理、处理中、待用户确认、成功、失败。 */
    private String grantStatus;

    /** 奖品id */
    @Schema(name = "lotteryPrizeId", description = "奖品id")
    private Long lotteryPrizeId;
    /** 奖品名称 */
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;
    /** 奖品类型 */
    @Schema(name = "prizeType", description = "奖品类型")
    private int prizeType;
    /** 奖品图片 */
    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;
    /** 红包类型返参 */
    @Schema(name = "packageInfo", description = "红包类型返参")
    private String packageInfo;
    @Schema(name = "outBillNo", description = "红包类型返参")
    public String outBillNo;
}
