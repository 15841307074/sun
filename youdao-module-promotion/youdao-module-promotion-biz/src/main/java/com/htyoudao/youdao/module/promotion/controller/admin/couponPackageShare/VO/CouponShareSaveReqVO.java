package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 优惠券包推广新增/修改 Request VO")
@Data
public class CouponShareSaveReqVO {

    /**
     * 优惠券id
     */
    @Schema(description = "优惠券id")
    private Long couponId;
    /**
     * 会员id
     */
    @Schema(description = "会员id")
    private Long memberId;
    /**
     * 会员手机号
     */
    @Schema(description = "会员手机号")
    private String memberMobile;
    /**
     * 发放数量
     */
    @Schema(description = "发放数量")
    private Integer couponNum;
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
    /**
     * 分享大图
     */
    @Schema(description = "分享大图")
    private String shareLargeImgUrl;
}
