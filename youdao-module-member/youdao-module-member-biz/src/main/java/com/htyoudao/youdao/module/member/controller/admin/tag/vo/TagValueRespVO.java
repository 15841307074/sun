package com.htyoudao.youdao.module.member.controller.admin.tag.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 标签 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TagValueRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2554")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "标签名称", example = "张三")
    @ExcelProperty("标签名称")
    private String name;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25920")
    @ExcelProperty("项目 id")
    private Long businessId;

    @Schema(description = "标签组id", example = "3530")
    @ExcelProperty("标签组id")
    private Long tagGroupId;

}