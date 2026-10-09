package com.htyoudao.youdao.module.system.controller.admin.business.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessRespVO {

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

    @Schema(description = "点餐登录页背景")
    private String dcUrl;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "描述")
    private String comment;

    @Schema(description = "菜单 ids")
    private List<String> menuIds;


    // 项目负责人 信息
    @Schema(description = "项目负责人id")
    private Long userId;

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String nickname;

    @Schema(description = "手机号码", example = "15601691300")
    private String mobile;

}