package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "抽签记录")
public class DrawRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "签码")
    private String signCode;

    @Schema(description = "获取方式")
    private Integer obtainType;

    @Schema(description = "结果状态")
    private Integer resultStatus;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品内容")
    private String prizeContent;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品状态")
    private Integer prizeState;

    @Schema(description = "抽签时间")
    private LocalDateTime drawTime;
}
