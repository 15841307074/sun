package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "bpm 服务 - 部门 Response VO")
@Data
public class BpmDeptRespVO {

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    private String name;

    @Schema(description = "父部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long parentId;
}
