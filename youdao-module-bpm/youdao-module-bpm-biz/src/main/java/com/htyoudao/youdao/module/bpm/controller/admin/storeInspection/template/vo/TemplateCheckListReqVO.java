package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 模板点检项 Request VO")
@Data
public class TemplateCheckListReqVO {

    @Schema(description = "主键ID")
    private Long templateChecklistId;

    @Schema(description = "主键ID")
    private Long templateId;


    @Schema(description = "点检项ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String checklistId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String title;

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String typeId;

    @Schema(description = "大类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大类")
    private String typeName;

    @Schema(description = "提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String prompt;

    @Schema(description = "总分", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Integer score;

    @Schema(description = "图片规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String imgRule;

    @Schema(description = "描述规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String descriptionRule;

    @Schema(description = "不适用规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String inapplicabilityRule;

    @Schema(description = "奖惩规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String rewardPunishmentRule;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "大类排序")
    private Integer typeSort;
}
