package com.htyoudao.youdao.module.bpm.controller.app.task.VO.getSum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BpmProcessInstanceSumReqVO {

    @Schema(description = "oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull(message = "oa项目ID不能为空")
    private Long oaProjectId;
}
