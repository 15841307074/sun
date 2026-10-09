package com.htyoudao.youdao.module.bpm.dal.dataobject.business;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


/**
 * OA 请假申请 DO
 *
 * @author jason
 * @author 0090
 */
@TableName("business_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BpmBusinessDO extends BusinessBaseDO {

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
     * 申请人员名称
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
    private LocalDateTime completionTime;

    /**
     * 任务完成时间
     */
    private LocalDateTime taskCompletionTime;
    /**
     * 附件地址
     */
    private String fileUrls;
    /**
     * 流程唯一标识
     */
    private String procInstId;

    /**
     * 父项流程标识
     */
    private String parentProcInstId;

    /**
     * 执行门店类型 1 全部门店 2部分门店
     */
    private Integer storeType;

    /**
     * 流程状态
     */
    private Integer taskState;

    private String businessKey;

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

    /**
     * 子任务数量，执行门店数量
     */
    private Integer storeCount;

    /**
     * 反馈信息
     */
    private String feedBackMsg;

    /**
     * 动态表单不能为空
     */
    private String formMsg;


    /**
     * 反馈完成时间
     */
    private LocalDateTime feedBackTime;

    /**
     * 抄送已读标识 1 已读 0未读
     */
    private Integer copyFlag;

    /**
     * 抄送已读标识 1 拒绝过 0未拒绝过
     */
    private Integer rejectFlag;

    /**
     * 图标
     */
    private String iconUrl;

    /**
     * OA项目ID
     */
    private Long oaProjectId;

}
