package com.htyoudao.youdao.module.system.controller.admin.orguser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织和用户关联 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OrgUserRespVO {

    @Schema(description = "自增编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5525")
    @ExcelProperty("自增编号")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21184")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25486")
    @ExcelProperty("组织ID")
    private Long orgId;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "类型 1. 负责人 0.普通", example = "1")
    @ExcelProperty("类型 1. 负责人 0.普通")
    private Integer type;

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "6285")
    @ExcelProperty("项目 id")
    private Long businessId;

}