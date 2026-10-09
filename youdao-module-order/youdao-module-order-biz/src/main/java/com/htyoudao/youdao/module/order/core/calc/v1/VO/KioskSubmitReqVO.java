package com.htyoudao.youdao.module.order.core.calc.v1.VO;

import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;

/**
 * <p>
 * V3 下单入参
 * </p>
 *
 * @author zhangjihe
 * @since 2025-02-13
 */
@Data
public class KioskSubmitReqVO extends SubmitReqVO {
    @Serial
    private static final long serialVersionUID = 2828250939513742049L;

    @Schema(description = "商品结算所需信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotNull(message = "商品结算所需信息不能为空")
    private SettlementReqVO settlementInfo;
}
