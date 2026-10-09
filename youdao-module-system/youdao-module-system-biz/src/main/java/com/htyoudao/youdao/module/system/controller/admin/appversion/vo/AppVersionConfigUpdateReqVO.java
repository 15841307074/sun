package com.htyoudao.youdao.module.system.controller.admin.appversion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 修改版本配置；除 ID 外，null 字段不修改")
public class AppVersionConfigUpdateReqVO {
    @NotNull
    @Positive
    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Size(max = 100)
    private String projectName;

    @Size(max = 100)
    private String versionCode;

    @Size(max = 10)
    private String code;

    @Size(max = 200)
    private String content;

    @Pattern(regexp = "[01]", message = "强制升级标识只能为0或1")
    private String forceUpdate;

    @Min(0)
    @Max(1)
    private Integer activeVersionCode;

    @Size(max = 100)
    private String filePath;

    @Size(max = 100)
    private String moduleName;
}
