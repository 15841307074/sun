package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "优惠券包分享实体", description = "优惠券包分享实体")
public class CouponPackageShareRespVO {

    /**
     * 主键id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 优惠券id
     */
    @Schema(description = "优惠券id")
    private Long packageId;

    /**
     * 分享标题
     */
    @Schema(description = "分享标题")
    private String shareTitle;

    /**
     * 分享内容
     */
    @Schema(description = "分享内容")
    private String shareNote;

    /**
     * 分享图片
     */
    @Schema(description = "分享图片")
    private String shareLittleImgUrl;

    /**
     * 小程序分享路径
     */
    @Schema(description = "小程序分享路径")
    private String wxShareUrl;

    /**
     * H5分享路径
     */
    @Schema(description = "H5分享路径")
    private String htmlShareUrl;

    /**
     * 二维码路径
     */
    @Schema(description = "二维码路径")
    private String erCodeUrl;

    /**
     * 海报路径
     */
    @Schema(description = "海报路径")
    private String postersImgUrl;

    @Schema(description = "默认小图")
    private String shareLargeImgUrl;
}
