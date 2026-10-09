package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 33483
 */
@Schema(description = "管理后台 - 组织机构负责人 Request VO")
@Data
public class OrgUserChargedReqVO {

    @Schema(description = "组织人员关系表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "orgUserId不能为空")
    private Long orgUserId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "组织ID不能为空")
    private Long orgId;
}
