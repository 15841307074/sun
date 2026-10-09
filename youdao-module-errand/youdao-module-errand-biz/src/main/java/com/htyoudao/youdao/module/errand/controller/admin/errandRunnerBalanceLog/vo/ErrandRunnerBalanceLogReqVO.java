package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "跑腿员余额统计信息")
public class ErrandRunnerBalanceLogReqVO extends PageParam {

    @Schema(description = "跑腿员ID")
    @NotNull(message = "memberId不能为空")
    private Long memberId;
}
