package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构添加门店 Req VO")
@Data
@ToString(callSuper = true)
public class OrgStoreSaveReqVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @NotNull(message = "门店ID不能为空")
    private List<Long> storeIds;
}
