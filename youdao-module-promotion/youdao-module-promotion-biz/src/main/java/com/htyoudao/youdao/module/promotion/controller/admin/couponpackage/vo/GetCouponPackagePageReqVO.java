package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 优惠券包关系分页 Request VO")
@Data
@ToString(callSuper = true)
public class GetCouponPackagePageReqVO extends PageParam {

    /**
     * 优惠券包名
     */
    @Schema(description = "优惠券包名")
    private String remark;
    /**
     * 是否上架(0-否,1-是)
     */
    @Schema(description = "是否上架(0-否,1-是)")
    private Integer isGround;

    /**
     * 领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）
     */
    @Schema(description = "领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）")
    private Integer userRestrictions;

    @Schema(description = "优惠券id", example = "26522")
    private Long couponId;

    @Schema(description = "0 普通券包 1 周周惠券包")
    private Integer packageType;
}
