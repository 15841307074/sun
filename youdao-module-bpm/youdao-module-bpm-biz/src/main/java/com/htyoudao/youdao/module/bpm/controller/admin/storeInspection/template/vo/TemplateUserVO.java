package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 模板 Response VO")
@Data
public class TemplateUserVO {

    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String userName;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long userId;
}
