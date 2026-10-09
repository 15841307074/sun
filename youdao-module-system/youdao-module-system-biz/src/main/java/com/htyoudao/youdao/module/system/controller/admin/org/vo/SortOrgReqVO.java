package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织排序 Request VO")
@Data
public class SortOrgReqVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "序号不能空")
    private Integer sort;
}
