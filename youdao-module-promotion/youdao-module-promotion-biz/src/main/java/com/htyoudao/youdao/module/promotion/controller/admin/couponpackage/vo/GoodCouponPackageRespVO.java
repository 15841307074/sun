package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.excel.annotation.*;

import java.math.BigDecimal;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券包关系 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GoodCouponPackageRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2755")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "优惠券包id", example = "25908")
    @ExcelProperty("优惠券包id")
    private Long packageId;

    @Schema(description = "优惠券id", example = "26522")
    @ExcelProperty("优惠券id")
    private Long couponId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer num;

    @Schema(description = "项目归属")
    @ExcelProperty("项目归属")
    private Long projectOwnerShip;

    /**
     * 展示标题
     */
    @Schema(description = "展示标题")
    private String displayTitle;
    /**
     * 展示金额
     */
    @Schema(description = "展示金额")
    private String displayAmount;
    /**
     * 划线内容
     */
    @Schema(description = "划线内容")
    private String underlinedContent;

    GoodCouponRespVO goodCouponRespVO = new GoodCouponRespVO();

    @Schema(description = "社群二维码")
    private String communityQrImage;

    @Schema(description = "0不需要进社群 1需要进社群", example = "0")
    private Integer communityFlag;
}