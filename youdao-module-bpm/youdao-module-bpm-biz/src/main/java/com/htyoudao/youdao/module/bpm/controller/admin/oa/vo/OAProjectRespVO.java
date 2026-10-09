package com.htyoudao.youdao.module.bpm.controller.admin.oa.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

@Schema(description = "管理后台 - OA项目 Response VO")
@Data
public class OAProjectRespVO {

    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "阅读0090")
    private String projectName;

    /**
     * 所属部门ID
     */
    @Schema(description = "所属部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "阅读0090")
    @NotNull(message = "部门名称不能为空")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "阅读0090")
    @NotNull(message = "部门名称不能为空")
    private String deptName;

    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "阅读0090")
    @NotNull(message = "项目负责人ID不能为空")
    private Long projectLeader;

    /**
     * 项目成员（多个成员用逗号分隔）
     */
    @Schema(description = "项目成员（多个成员用逗号分隔）", requiredMode = Schema.RequiredMode.REQUIRED, example = "阅读0090")
    @NotNull(message = "项目成员不能为空")
    private String projectMembers;

    /**
     * 项目开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "项目开始时间")
    private Date startDate;

    /**
     * 项目结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "项目结束时间")
    private Date endDate;

    /**
     * 项目描述（最多500字）
     */
    @Schema(description = "项目描述")
    private String projectDescription;

    /**
     * 项目状态：0-已结束 1-进行中 2-暂停 3-取消
     */
    private Integer status = 1;
}
