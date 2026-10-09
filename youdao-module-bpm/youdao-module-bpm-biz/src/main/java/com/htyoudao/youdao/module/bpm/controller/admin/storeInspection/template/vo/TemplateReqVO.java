package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo.CheckListReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 模板创建 Request VO")
@Data
public class TemplateReqVO {

    @Schema(description = "主键ID")
    private Long templateId;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String templateName;

    @Schema(description = "巡店可见人", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @Min(value = 0, message = "全部")
    @Max(value = 1, message = "指定成员")
    private Integer visibilityType;

    @Schema(description = "指定成员", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private List<TemplateUserVO> userIds;

    @Schema(description = "模板点检项", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private List<TemplateCheckListReqVO> templateChecklists;

}
