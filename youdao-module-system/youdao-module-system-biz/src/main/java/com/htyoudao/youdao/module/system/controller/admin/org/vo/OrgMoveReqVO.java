package com.htyoudao.youdao.module.system.controller.admin.org.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构移动 Request VO")
@Data
public class OrgMoveReqVO {
    @Schema(description = "被移动组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @NotNull(message = "被移动组织ID不能为空")
    private Long oldOrgId;

    @Schema(description = "目标组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "目标组织ID不能为空")
    private Long newOrgId;
}
