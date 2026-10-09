package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmProcessInstanceQueryReqVO extends PageParam {

    @Schema(description = "任务标题或者内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String taskContent;

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String processDefinitionId;

    @Schema(description = "发起人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantId;

    @Schema(description = "发起部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantDeptId;

    @Schema(description = "发起门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantStoreId;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer taskType;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String taskState;

    @Schema(description = "执行人", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorUserId;


    @Schema(description = "执行门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> executorStoreId;


    @Schema(description = "执行部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long executorDeptId;

    @Schema(description = "创建时间开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间结束", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeEnd;

    @Schema(description = "任务完成时间开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime taskCompletionTimeStart;

    @Schema(description = "任务完成时间结束", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime taskCompletionTimeEnd;

    @Schema(description = "项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目不能为空")
    private Long businessId;

    @Schema(description = "是否是全部", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "是否是全部不能为空")
    private Integer isAll;

    private List<String> proInstIdList;

    private List<Long> applicantIdList;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.REQUIRED, example = "createTime/completionTime")
    private String sortField;

    @Schema(description = "升序降序", requiredMode = Schema.RequiredMode.REQUIRED, example = "asc/desc")
    private String sortOrder;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "asc/desc")
    private String priority;

    @Schema(description = "抄送标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "0/1")
    private Integer copyFlag;

    @Schema(description = "oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long oaProjectId;

    @Schema(description = "关联oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 不关联 1关联 2全部")
    private Integer oaProjectFlag;

    @Schema(description = "发起部门ID及下属所有部门", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> applicantDeptIdList;
}
