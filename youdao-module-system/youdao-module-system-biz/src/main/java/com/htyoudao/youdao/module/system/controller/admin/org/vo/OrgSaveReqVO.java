package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构新增/修改 Request VO")
@Data
public class OrgSaveReqVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private Long id;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "组织名称不能为空")
    private String name;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Long parentId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Long userId;
}