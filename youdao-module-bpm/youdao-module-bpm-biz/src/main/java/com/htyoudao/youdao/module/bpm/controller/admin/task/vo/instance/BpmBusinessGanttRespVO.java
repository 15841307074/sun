package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmBusinessGanttRespVO {

    private String taskId;

    private String taskName;

    private Integer taskType;

    private String priority;

    private String procInstId;

    private Integer taskState;

    private LocalDateTime createTime;

    private String createTimeStr;

    private LocalDateTime completionTime;

    private String completionTimeStr;

    private String handleUserId;

    private String handleUserName;

    private String handleDeptId;

    private String handleDeptName;

    private String startDate;

    private String endDate;

    /**
     * 执行人ID
     */
    private String executorUserId;

    /**
     * 执行部门
     */
    private String executorDeptId;

    /**
     * 执行部门
     */
    private String executorStoreId;

    /**
     * 执行人姓名
     */
    private String executorUserName;

    /**
     * 执行部门名称
     */
    private String executorDeptName;

    /**
     * 执行部门名称
     */
    private String executorStoreName;

}
