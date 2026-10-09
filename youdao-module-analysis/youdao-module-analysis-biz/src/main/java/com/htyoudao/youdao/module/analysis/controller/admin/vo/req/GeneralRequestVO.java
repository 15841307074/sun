package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Data;

@Data
public class GeneralRequestVO extends AggregationRequestVO{

    @Schema(description = "订单来源 0点餐 1微信 2支付宝")
    private Integer orderFrom;

    @Schema(description = "指标列表")
    @NotEmpty(message = "指定指标列表不能为空")
    private Set<String> metrics;

}
