package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 抽到的卡片信息VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "抽到的卡片信息")
public class DrawCardVO {

    @Schema(description = "卡片ID")
    private Long cardId;

    @Schema(description = "卡片名称")
    private String cardName;

    @Schema(description = "卡片类型 1兜底卡 2套系卡 3万能卡 4隐藏卡")
    private Integer cardType;

    @Schema(description = "卡片类型名称")
    private String cardTypeName;

    @Schema(description = "卡片图片")
    private String cardImgUrl;

    @Schema(description = "抽取概率")
    private BigDecimal probability;

    @Schema(description = "是否是新获得的卡片")
    private Boolean isNew;

    @Schema(description = "获得数量")
    private Integer count;
}
