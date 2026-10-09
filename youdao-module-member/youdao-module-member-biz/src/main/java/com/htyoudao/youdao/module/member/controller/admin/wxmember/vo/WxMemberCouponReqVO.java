package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

/**
 * @author dht
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 发放优惠券里的member Request VO")
@Data
public class WxMemberCouponReqVO {

    /**
     * 会员id
     */
    @Schema(description = "会员id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

    /**
     * 会员昵称
     */
    @Schema(description = "会员昵称")
    private String memberNickName;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String memberMobile;
}
