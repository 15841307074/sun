package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class MetricsSettingRequestVO {

    /**
     * 指标来源  总览:general 历史:history 点餐:dc
     */
    @Schema(description = "指标来源  总览:general 历史:history 点餐:dc")
    @NotNull
    private String key;

    @Schema(description = "指标code")
    @NotEmpty
    private List<String> metrics;
}
