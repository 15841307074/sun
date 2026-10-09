package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理后台 - 折线图参数对象
 * 用于查询指定指标随时间变化的趋势数据
 */
@Schema(description = "管理后台 - 折线图参数对象 Request VO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LineChartRequestVO extends AggregationRequestVO {

    /**
     * 指标code
     * 用于指定需要查询的指标类型，如订单金额、订单数量等
     */
    @Schema(description = "指标code，如 orderAmount, orderCount 等", example = "orderAmount")
    @NotNull
    private String code;

    /**
     * 查询类型/时间粒度
     * DAY: 按天统计
     * HOUR: 按小时统计
     */
    @Schema(description = "查询类型 天:DAY 小时:HOUR", example = "DAY")
    @NotNull
    private String type;

}
