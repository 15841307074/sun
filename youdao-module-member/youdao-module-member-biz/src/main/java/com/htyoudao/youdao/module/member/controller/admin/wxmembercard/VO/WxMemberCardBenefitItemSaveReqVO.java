package com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 会员卡权益项保存 Request VO")
public class WxMemberCardBenefitItemSaveReqVO {

    @Schema(description = "关联 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "券类型 1=优惠券 2=券包", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "券类型不能为空")
    private Integer couponType;

    @Schema(description = "券/券包 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "券/券包 ID 不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    @Schema(description = "发放数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放数量不能为空")
    @Min(value = 1, message = "发放数量必须大于 0")
    private Integer sendNum;

    @Schema(description = "重复周期 1=按周 2=按月")
    private Integer repeatType;

    @Schema(description = "发放日期值，按周=1~7，按月=1~31")
    private Integer issueValue;
}
