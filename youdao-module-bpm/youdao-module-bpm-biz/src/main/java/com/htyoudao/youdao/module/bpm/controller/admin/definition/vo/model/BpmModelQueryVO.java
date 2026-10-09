package com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.model;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 流程模型 筛选")
@Data
public class BpmModelQueryVO  extends PageParam {

    @Schema(description = "流程名称/流程描述")
    private String name;

    @Schema(description = "流程分类编号", example = "1")
    private String category;

    @Schema(description = "流程分类名字", example = "请假")
    private String categoryName;

    @Schema(description = "应用部门")
    private Long deptId;

    private Integer suspensionState;
}
