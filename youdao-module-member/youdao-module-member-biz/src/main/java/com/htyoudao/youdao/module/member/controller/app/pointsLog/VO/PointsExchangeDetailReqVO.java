package com.htyoudao.youdao.module.member.controller.app.pointsLog.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小程序积分商品兑换详情请求参数。
 */
@Data
@Schema(description = "小程序积分商品兑换详情请求参数")
public class PointsExchangeDetailReqVO {

    /**
     * 会员 ID，用于校验兑换记录归属。
     */
    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    /**
     * 积分兑换记录 ID，与兑换记录编号至少传一个。
     */
    @Schema(description = "积分兑换记录ID，与兑换记录编号至少传一个")
    private Long pointsLogId;

    /**
     * 兑换记录编号，与积分兑换记录 ID 至少传一个。
     */
    @Schema(description = "兑换记录编号，与积分兑换记录ID至少传一个")
    private String logCode;
}
