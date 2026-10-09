package com.htyoudao.youdao.module.system.controller.admin.business.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessPageRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7398")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "项目编码;hk,ts", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("项目编码;hk,ts")
    private String code;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("项目名称")
    private String name;

    @Schema(description = "经营方式 0 自营 1 合作", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("经营方式 0 自营 1 合作")
    private Integer manageType;

    @Schema(description = "状态;0-未启用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态;0-未启用 1-启用")
    private Integer status;

    @Schema(description = "有效期开始时间")
    @ExcelProperty("有效期开始时间")
    private LocalDateTime validityStartTime;

    @Schema(description = "有效期结束时间")
    @ExcelProperty("有效期结束时间")
    private LocalDateTime validityEndTime;

    @Schema(description = "logo url", example = "https://www.iocoder.cn")
    @ExcelProperty("logo url")
    private String logoUrl;

    @Schema(description = "描述", example = "https://www.iocoder.cn")
    @ExcelProperty("描述")
    private String comment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "门店数量", example = "111")
    @ExcelProperty("门店数量")
    private Long storeCount;

    @Schema(description = "用户数量", example = "111")
    @ExcelProperty("用户数量")
    private Integer userCount;

}