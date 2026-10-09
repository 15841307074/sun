package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityAnswerRewardReqVO {

    @Schema(description = "奖励档位 ID，新增可不传")
    private Long id;

    @Schema(description = "答对题数档位")
    @NotNull(message = "奖励档位答对题数不能为空")
    private Integer correctCount;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物 5现金红包")
    @NotNull(message = "奖品类型不能为空")
    private Integer prizeType;

    @Schema(description = "奖品id(根据类型判断是优惠券 id 还是商品 id)")
    private Long awardId;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称不能为空")
    private String prizeName;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券包名称，奖品类型为优惠券包时返回")
    private String packageName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "奖品总库存，0 不限")
    private Integer totalNum;

    @Schema(description = "已领取数量，以有效发放记录统计为准")
    private Integer usedNum;
}
