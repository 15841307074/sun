package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 模板 Response VO")
@Data
public class TemplateQueryRespVO {

    @Schema(description = "主键ID")
    private Long templateId;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String templateName;

    @Schema(description = "大类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    private List<String> typeName;

    @Schema(description = "点检项目数", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Integer checklistCount;

    @Schema(description = "点检项总分值", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Integer sumScore;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime createTime;
}
