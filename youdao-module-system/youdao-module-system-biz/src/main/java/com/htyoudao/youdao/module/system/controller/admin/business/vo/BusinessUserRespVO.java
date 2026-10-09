package com.htyoudao.youdao.module.system.controller.admin.business.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - 项目选择页面 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessUserRespVO {

    @Schema(description = "ID")
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

    @Schema(description = "角色信息", example = "https://www.iocoder.cn")
    private List<String> roleNames;

}