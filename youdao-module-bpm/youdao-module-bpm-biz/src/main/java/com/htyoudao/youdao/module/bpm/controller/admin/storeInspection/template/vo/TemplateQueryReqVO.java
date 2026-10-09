package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 模板 Response VO")
@Data
public class TemplateQueryReqVO extends PageParam {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String templateName;

}
