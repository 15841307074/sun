package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 * 券包分享新增 后端用
 */
@Data
public class CouponPackageShareSaveReqVO {

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

}
