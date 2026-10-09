package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.task;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "bpm 服务 - 流程的业务信息 Response VO")
@Data
public class BpmBusinessRespVO {


    /**
     * 业务主键
     */
    @TableId
    private Long taskId;
    /**
     * 任务描述
     */
    private String taskDesc;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 门店名称
     */
    private String storeName;
    /**
     * 申请人员联系方式
     */
    private String applicant;
    /**
     * 申请人员Id
     */
    private Long applicantId;
    /**
     * 申请人员手机号
     */
    private String applicantName;
    /**
     * 任务类型
     */
    private Integer taskType;
    /**
     * 处理部门ID
     */
    private Long handleDeptId;
    /**
     * 处理部门名称
     */
    private String handleDeptName;
    /**
     * 流程模板ID
     */
    private String flowId;
    /**
     * 流程模板名称
     */
    private String flowName;

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 优先级 1紧急 2高 3中 4底
     */
    private String priority;
    /**
     * 期望完成时间
     */
    private String completionTime;
    /**
     * 附件地址
     */
    private String fileUrls;
    /**
     * 流程唯一标识
     */
    private String procInstId;

    private String processDefinitionId;

    private String processDefinitionName;

    /**
     * 执行门店类型
     */
    private Integer storeType;

    BpmDeptRespVO deptRespVO = new BpmDeptRespVO();
}
