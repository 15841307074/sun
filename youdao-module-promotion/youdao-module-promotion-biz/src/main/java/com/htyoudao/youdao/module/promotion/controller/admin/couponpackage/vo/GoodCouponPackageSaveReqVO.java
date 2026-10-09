package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券包关系新增/修改 Request VO")
@Data
public class GoodCouponPackageSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2755")
    private Long id;

    @Schema(description = "优惠券包id", example = "25908")
    private Long packageId;

    @Schema(description = "优惠券id", example = "26522")
    private Long couponId;

    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "展示标题")
    private String displayTitle;

    @Schema(description = "展示金额")
    private String displayAmount;

    @Schema(description = "划线内容")
    private String underlinedContent;

    private Boolean deleted;

    @Schema(description = "0不需要进社群 1需要进社群", example = "0")
    private Integer communityFlag;

    @Schema(description = "社群二维码")
    private String communityQrImage;
}