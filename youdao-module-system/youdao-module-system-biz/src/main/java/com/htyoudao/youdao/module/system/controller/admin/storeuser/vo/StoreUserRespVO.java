package com.htyoudao.youdao.module.system.controller.admin.storeuser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 门店和用户关联 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreUserRespVO {

    @Schema(description = "自增编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4365")
    @ExcelProperty("自增编号")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26974")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6813")
    @ExcelProperty("门店ID")
    private Long storeId;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "类型 1. 店长 ", example = "1")
    @ExcelProperty("类型 1. 店长 ")
    private Integer type;

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24728")
    @ExcelProperty("项目 id")
    private Long businessId;

}