package com.htyoudao.youdao.module.system.controller.admin.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.dubbo.common.logger.FluentLogger.S;

@Schema(description = "管理后台 - 角色精简信息 Response VO")
@Data
public class RoleSimpleByBusinessRespVO {

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long businessId;

}
