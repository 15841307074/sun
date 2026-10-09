package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;


import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 余额流水分页查询请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额流水分页查询请求参数")
public class BalanceLogPageReqVO extends PageParam {


    @Schema(description = "流水类型：1赏金入账 2提现扣减 3退款扣回 4提现失败退回 5人工调整 6赏金解冻")
    private Integer flowType;

    @Schema(description = "方向：1收入 2支出")
    private Integer direction;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
