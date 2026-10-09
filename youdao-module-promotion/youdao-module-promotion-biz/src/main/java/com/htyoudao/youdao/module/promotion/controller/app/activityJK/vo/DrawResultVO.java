package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 抽卡结果响应VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "抽卡结果")
public class DrawResultVO {
    /** 卡片id */
    @Schema(name = "jkPrizeId", description = "卡片id")
    private Long jkCardId;
    /** 卡片名称 */
    @Schema(name = "prizeName", description = "卡片名称")
    private String jkCardName;
    /** 卡片类型 */
    @Schema(name = "jkCardType", description = "卡片类型")
    private int jkCardType;
    /** 卡片图片 */
    @Schema(name = "jkCardImgUrl", description = "卡片图片")
    private String jkCardImgUrl;
    /** 红包类型返参 */
    @Schema(name = "packageInfo", description = "红包类型返参")
    private String packageInfo;
    @Schema(name = "outBillNo", description = "红包类型返参")
    public String outBillNo;

}
