package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dht
 */
@Schema(description = "老板助手 - 部门+用户信息 Response VO")
@Data
public class AppDeptUserRespVO implements Serializable {

    @Schema(description = "部门id", example = "1024")
    private Long id;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String name;

    @Schema(description = "父部门 ID", example = "1024")
    private Long parentId;

    @Schema(description = "等级  最大5级", example = "1")
    private Integer level;

    @Schema(description = "项目id", example = "10")
    private Long businessId;

    @Schema(description = "0部门 1人员", example = "1")
    private Integer deptFlag;

    @Schema(description = "0主管 1普通", example = "1")
    private Integer type;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String mobile;
}
