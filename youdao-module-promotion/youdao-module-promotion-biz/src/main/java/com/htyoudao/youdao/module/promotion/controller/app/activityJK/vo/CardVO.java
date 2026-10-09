package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 卡片信息VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "卡片信息")
public class CardVO {

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

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;

    @Schema(description = "卡片描述")
    private String cardDesc;

    @Schema(description = "卡片排序")
    private Integer cardSort;
}
