package com.htyoudao.youdao.module.bpm.controller.admin.oa.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - OA项目申请创建 Request VO")
@Data
public class OAProjectCreateReqVO {


    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目名称1111")
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    /**
     * 所属部门ID集合
     */
    @Schema(description = "所属部门ID集合", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门名称不能为空")
    private List<Long> deptIds;


    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "项目负责人ID不能为空")
    private Long projectLeader;

    /**
     * 项目成员
     */
    @Schema(description = "项目成员", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3,4]")
    @NotEmpty(message = "项目成员不能为空")
    private List<Long> projectUsers;

    /**
     * 项目开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "项目开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-01-01")
    private Date startDate;

    /**
     * 项目结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "项目结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-01-01")
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
