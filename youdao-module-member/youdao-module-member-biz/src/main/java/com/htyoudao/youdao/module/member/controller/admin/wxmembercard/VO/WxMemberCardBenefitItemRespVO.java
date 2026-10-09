package com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 会员卡权益项 Response VO")
public class WxMemberCardBenefitItemRespVO {

    @Schema(description = "关联 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "权益场景 1=会员权益 2=生日礼 3=升级权益")
    private Integer benefitScene;

    @Schema(description = "券类型 1=优惠券 2=券包")
    private Integer couponType;

    @Schema(description = "券/券包 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    @Schema(description = "权益名称")
    private String benefitName;

    @Schema(description = "发放数量")
    private Integer sendNum;

    @Schema(description = "重复周期 1=按周 2=按月")
    private Integer repeatType;

    @Schema(description = "发放日期值，按周=1~7，按月=1~31")
    private Integer issueValue;
}
