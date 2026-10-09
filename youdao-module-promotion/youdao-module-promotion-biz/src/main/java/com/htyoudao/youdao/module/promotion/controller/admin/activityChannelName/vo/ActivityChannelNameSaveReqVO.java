package com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 渠道名称新增/修改 Request VO")
@Data
public class ActivityChannelNameSaveReqVO {

    @Schema(description = "主键ID（更新时必传）", example = "1")
    private Long id;

    @Schema(description = "渠道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认渠道(微信H5路径)")
    @NotNull(message = "渠道名称不能为空")
    private String name;





}

