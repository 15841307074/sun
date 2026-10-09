package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - pc端查询小程序用户入参 Request VO")
@Data
public class WxMemberSendReqVO {

    @Schema(description = "优惠券id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    @Schema(description = "优惠券发放数量")
    private Integer sendNum;

    @Schema(description = "选中的会员")
    private List<WxMemberCouponReqVO> members;

    @Schema(description = "0 优惠券 1券包")
    private Integer couponType;
}
