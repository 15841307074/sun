package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 套卡信息VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "套卡信息")
public class CardSetVO {

    @Schema(description = "套卡ID")
    private Long id;

    @Schema(description = "套卡名称")
    private String setName;


    @Schema(description = "已收集数量")
    private Integer collectedCount;

    @Schema(description = "是否已集齐")
    private Boolean complete;

    @Schema(description = "套卡图片")
    private String setImgUrl;
}
