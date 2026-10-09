package com.htyoudao.youdao.module.system.controller.admin.complaint.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Schema(description = "管理后台 - 投诉处理 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ComplaintUpdateReqVO {
    @Schema(name = "id", description = "id")
    private Long id;
    @Schema(name = "dealNote", description = "处理内容")
    @NotNull(message = "处理内容不能为空")
    private String dealNote;

}