package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OrgRespVO {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    @ExcelProperty("组织ID")
    private Long id;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("组织名称")
    private String name;

    @Schema(description = "祖级列表")
    @ExcelProperty("祖级列表")
    private String ancestors;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    @ExcelProperty("上级ID")
    private Long parentId;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序号")
    private Integer sort;

    @Schema(description = "组织类型", example = "2")
    @ExcelProperty("组织类型")
    private Integer type;

    @Schema(description = "组织编号")
    @ExcelProperty("组织编号")
    private String code;

    @Schema(description = "状态 0-未启用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态 0-未启用 1-启用")
    private Integer status;

    @Schema(description = "层级编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("层级编号")
    private Integer level;

    @Schema(description = "业务线ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27690")
    @ExcelProperty("业务线ID")
    private Integer businessId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}