package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 33483
 */
@Data
@Schema(description = "app - 通过组织查询用户 Response VO")
public class DeptUserRespVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名称", example = "1")
    private String name;

    @Schema(description = "用户头像", example = "1")
    private String mobile;

    @Schema(description = "0部门 1人员", example = "1")
    private Integer deptFlag;

    @Schema(description = "部门名称", example = "1")
    private String deptName;

    @Schema(description = "部门ID", example = "1")
    private Long parentId;
}
