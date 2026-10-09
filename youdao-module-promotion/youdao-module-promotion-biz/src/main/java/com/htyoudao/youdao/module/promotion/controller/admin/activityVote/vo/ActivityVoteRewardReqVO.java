package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityVoteRewardReqVO {

    @Schema(description = "奖励id（修改时传）")
    private Long id;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物奖品 5现金红包")
    @NotNull(message = "奖品类型不能为空")
    private Integer prizeType;

    @Schema(description = "奖品id")
    private Long prizeId;

    @Schema(description = "优惠券/包名称")
    private String couponName;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "总库存 0不限库存")
    private Integer totalNum;
}
