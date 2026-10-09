package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构门店移除/移动 Request VO")
@Data
public class OrgStoreMoveReqVO {

    @Schema(description = "旧组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @NotNull(message = "旧组织ID不能为空")
    private Long oldOrgId;

    @Schema(description = "门店IDS", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @NotNull(message = "门店IDS不能为空")
    private List<Long> storeIds;

    @Schema(description = "新组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private Long newOrgId;
}
