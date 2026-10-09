package com.htyoudao.youdao.module.bpm.controller.admin.oa.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OAProjectRelationshipReqVO {

    @Schema(description = "oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "oa项目ID不能为空")
    private Long oaProjectId;

    @Schema(description = "oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "oa项目ID不能为空")
    private List<Long> businessTaskIds;
}
