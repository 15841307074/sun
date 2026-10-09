package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.core.KeyValue;
import com.htyoudao.youdao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import com.htyoudao.youdao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - OA项目查询关联项目 Response VO")
@Data
public class BpmBusinessOAProjectQueryRespVO {

    @Schema(description = "任务业务id")
    private Long taskId;
    /**
     * 任务描述
     */
    @Schema(description = "任务业务id")
    private String taskDesc;
    /**
     * 部门ID
     */
    @Schema(description = "任务业务id")
    private Long deptId;
    /**
     * 部门名称
     */
    @Schema(description = "任务业务id")
    private String deptName;
    /**
     * 门店ID
     */
    @Schema(description = "任务业务id")
    private Long storeId;
    /**
     * 门店名称
     */
    @Schema(description = "任务业务id")
    private String storeName;
    /**
     * 申请人员联系方式
     */
    @Schema(description = "申请人员联系方式")
    private String applicant;
    /**
     * 申请人员Id
     */
    @Schema(description = "申请人员Id")
    private Long applicantId;
    /**
     * 申请人员名称
     */
    @Schema(description = "申请人员名称")
    private String applicantName;
    /**
     * 任务类型
     */
    @Schema(description = "任务类型 0 内部任务 1 门店任务")
    private Integer taskType;
    /**
     * 处理部门ID
     */
    @Schema(description = "处理部门ID")
    private Long handleDeptId;
    /**
     * 处理部门名称
     */
    @Schema(description = "处理部门名称")
    private String handleDeptName;
    /**
     * 流程模板ID
     */
    @Schema(description = "流程模板ID")
    private String flowId;
    /**
     * 流程模板名称
     */
    @Schema(description = "流程模板名称")
    private String flowName;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String taskName;
    /**
     * 优先级 1紧急 2高 3中 4底
     */
    @Schema(description = "优先级 1紧急 2高 3中 4底")
    private String priority;
    /**
     * 期望完成时间
     */
    @Schema(description = "期望完成时间")
    private LocalDateTime completionTime;

    /**
     * 任务完成时间
     */
    @Schema(description = "任务完成时间")
    private LocalDateTime taskCompletionTime;
    /**
     * 附件地址
     */
    @Schema(description = "附件地址")
    private String fileUrls;
    /**
     * 流程唯一标识
     */
    @Schema(description = "流程唯一标识")
    private String procInstId;

    /**
     * 父项流程标识
     */
    @Schema(description = "父项流程标识")
    private String parentProcInstId;

    /**
     * 执行门店类型 1 全部门店 2部分门店
     */
    @Schema(description = "执行门店类型 1 全部门店 2部分门店")
    private Integer storeType;

    /**
     * 流程状态
     */
    @Schema(description = "流程状态")
    private Integer taskState;

    private String businessKey;

    /**
     * 执行人ID
     */
    @Schema(description = "执行人ID")
    private String executorUserId;

    /**
     * 执行部门
     */
    @Schema(description = "执行部门")
    private String executorDeptId;

    /**
     * 执行部门
     */
    @Schema(description = "执行部门")
    private String executorStoreId;

    /**
     * 执行人姓名
     */
    @Schema(description = "执行人姓名")
    private String executorUserName;

    /**
     * 执行部门名称
     */
    @Schema(description = "执行部门名称")
    private String executorDeptName;

    /**
     * 执行部门名称
     */
    @Schema(description = "执行部门名称")
    private String executorStoreName;

    /**
     * 子任务数量，执行门店数量
     */
    @Schema(description = "子任务数量，执行门店数量")
    private Integer storeCount;

    /**
     * 反馈信息
     */
    @Schema(description = "反馈信息")
    private String feedBackMsg;

    /**
     * 动态表单不能为空
     */
    @Schema(description = "动态表单不能为空")
    private String formMsg;


    /**
     * 反馈完成时间
     */
    @Schema(description = "反馈完成时间")
    private LocalDateTime feedBackTime;

    /**
     * 抄送已读标识 1 已读 0未读
     */
    @Schema(description = "抄送已读标识 1 已读 0未读")
    private Integer copyFlag;

    /**
     * 抄送已读标识 1 拒绝过 0未拒绝过
     */
    @Schema(description = "任务业务id")
    private Integer rejectFlag;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String iconUrl;

    /**
     * OA项目ID
     */
    @Schema(description = "OA项目ID")
    private Long oaProjectId;
    /**
     * OA项目名称
     */
    @Schema(description = "OA项目名称")
    private String oaProjectName;

    private LocalDateTime createTime;

}
