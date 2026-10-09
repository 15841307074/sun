package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户卡片信息 VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "用户卡片信息")
public class UserCardVO {

    @Schema(description = "卡片ID")
    private Long cardId;

    @Schema(description = "卡片名称")
    private String cardName;

    @Schema(description = "卡片类型 1兜底卡 2套系卡 3万能卡 4隐藏卡")
    private Integer cardType;

    @Schema(description = "卡片图片")
    private String cardImgUrl;

    @Schema(description = "持有数量")
    private Integer count;

    @Schema(description = "卡片排序")
    private Integer cardSort;
}