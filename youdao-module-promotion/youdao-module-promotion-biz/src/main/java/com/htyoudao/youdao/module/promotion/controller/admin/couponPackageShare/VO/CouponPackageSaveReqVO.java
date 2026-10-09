package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 优惠券包分享子新增/修改 Request VO")
@Data
public class CouponPackageSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15697")
    private Long id;

    @Schema(description = "优惠券id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5133")
    @NotNull(message = "优惠券id不能为空")
    private Long packageId;


    @Schema(description = "分享标题")
    private String shareTitle;


    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareLittleImgUrl;

    @Schema(description = "小程序分享路径")
    private String wxShareUrl;

    @Schema(description = "H5分享路径")
    private String htmlShareUrl;

    @Schema(description = "二维码路径")
    private String erCodeUrl;

    @Schema(description = "海报路径")
    private String postersImgUrl;

    @Schema(description = "分享大图")
    private String shareLargeImgUrl;
}
