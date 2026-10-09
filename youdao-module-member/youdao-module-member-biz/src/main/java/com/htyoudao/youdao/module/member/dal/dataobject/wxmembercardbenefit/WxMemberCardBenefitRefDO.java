package com.htyoudao.youdao.module.member.dal.dataobject.wxmembercardbenefit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName("wx_member_card_benefit_ref")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WxMemberCardBenefitRefDO extends BusinessBaseDO {

    @Schema(description = "关联 ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "会员卡 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberCardId;

    @Schema(description = "权益场景 1=会员权益 2=生日礼 3=升级权益")
    private Integer benefitScene;

    @Schema(description = "券类型 1=优惠券 2=券包")
    private Integer couponType;

    @Schema(description = "券/券包 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    @Schema(description = "发放数量")
    private Integer sendNum;

    @Schema(description = "重复周期 1=按周 2=按月")
    private Integer repeatType;

    @Schema(description = "发放日期值，按周=1~7，按月=1~31")
    private Integer issueValue;
}
