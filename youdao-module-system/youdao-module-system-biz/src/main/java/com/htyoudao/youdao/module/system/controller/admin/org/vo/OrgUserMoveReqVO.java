package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构人员移动/移除 Request VO")
@Data
public class OrgUserMoveReqVO {

    @Schema(description = "被移动组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @NotNull(message = "被移动组织ID不能为空")
    private Long oldOrgId;

    @Schema(description = "人员表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "人员表ID不能为空")
    private List<Long> userIds;

    @Schema(description = "目标组织IDs", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private List<Long> newOrgIds;
}
