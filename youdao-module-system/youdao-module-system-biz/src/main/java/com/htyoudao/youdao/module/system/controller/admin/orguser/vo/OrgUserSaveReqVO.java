package com.htyoudao.youdao.module.system.controller.admin.orguser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @author 33483
 */
@Schema(description = "管理后台 - 组织和用户关联新增/修改 Request VO")
@Data
public class OrgUserSaveReqVO {

    @Schema(description = "没用", requiredMode = Schema.RequiredMode.REQUIRED, example = "5525")
    private Long id;

    @Schema(description = "用户IDS", requiredMode = Schema.RequiredMode.REQUIRED, example = "21184")
    private List<Long> userIds;

    @Schema(description = "没用", requiredMode = Schema.RequiredMode.REQUIRED, example = "21184")
    private Long userId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25486")
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    @Schema(description = "没用", example = "1")
    private Integer type;

    @Schema(description = "没用", example = "1")
    private Integer visible;

}