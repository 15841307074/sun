package com.htyoudao.youdao.module.bpm.controller.admin.oa.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance.BpmAppGetSumNumRespVO;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - OA项目分页 Request VO")
@Data
public class OAProjectPageRespVO extends PageParam {

    @Schema(description = "OA项目ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;


    /**
     * 项目描述（最多500字）
     */
    @Schema(description = "项目描述")
    private String projectDescription;


    private String deptIds;

    @Schema(description = "所属部门")
    private List<DeptRespDTO> deptList;


    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID")
    private Long projectLeader;

    @Schema(description = "项目负责人信息")
    private AdminUserRespDTO projectLeaderDTO;

    @Schema(description = "项目成员ID列表")
    private List<Long> projectUsers;

    @Schema(description = "项目成员列表")
    private List<AdminUserRespDTO> projectUserDTOs;


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


    @Schema(description = "任务总数", example = "100")
    private Long totalTasks = 0L;

    @Schema(description = "已完成任务数", example = "45")
    private Long completedTasks = 0L;

    @Schema(description = "统计结果", example = "100")
    private BpmAppGetSumNumRespVO sumNumRespVO;

}
