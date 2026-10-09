package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券包关系分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GoodCouponPackagePageReqVO extends PageParam {

    @Schema(description = "优惠券包id", example = "25908")
    private Long packageId;

    @Schema(description = "优惠券id", example = "26522")
    private Long couponId;

    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "项目归属")
    private Long projectOwnerShip;

}