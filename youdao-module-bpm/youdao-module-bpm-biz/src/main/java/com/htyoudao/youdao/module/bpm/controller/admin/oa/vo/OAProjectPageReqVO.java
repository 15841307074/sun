package com.htyoudao.youdao.module.bpm.controller.admin.oa.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - OA项目分页 Request VO")
@Data
public class OAProjectPageReqVO extends PageParam {

    /**
     * 请输入项目名称/描述
     */
    @Schema(description = "请输入项目名称/描述")
    private String keyword;

    /**
     * 所属部门ID
     */
    @Schema(description = "所属部门ID")
    private String deptId;


    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID")
    private Long projectLeader;

    /**
     * 项目成员
     */
    @Schema(description = "项目成员")
    private String projectUser;


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

    @Schema(description = "是否需要查看全部任务")
    @NotNull
    private Boolean isAll = false;
}
