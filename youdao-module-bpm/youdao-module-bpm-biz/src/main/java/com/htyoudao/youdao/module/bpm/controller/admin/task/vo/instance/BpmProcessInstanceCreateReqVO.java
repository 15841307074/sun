package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmProcessInstanceCreateReqVO {

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "流程定义编号不能为空")
    private String processDefinitionId;

    @Schema(description = "流程定义名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "流程定义名称不能为空")
    private String processDefinitionName;

    @Schema(description = "变量实例（动态表单）")
    private Map<String, Object> variables;

    @Schema(description = "发起人自选审批人 Map", example = "{taskKey1: [1, 2]}")
    private Map<String, List<Long>> startUserSelectAssignees;

    @Schema(description = "申请人员联系方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "申请人员联系方式不能为空")
    private String applicant;

    @Schema(description = "申请人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请人员ID不能为空")
    private Long applicantId;

    @Schema(description = "申请人员名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "申请人员名称不能为空")
    private String applicantName;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1 内部 2门店")
    @NotNull(message = "任务类型不能为空")
    private Integer taskType;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "任务描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "任务描述不能为空")
    private String taskDesc;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String storeName;

    @Schema(description = "处理部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long handleDeptId;

    @Schema(description = "处理部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String handleDeptName;

    @Schema(description = "所属部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deptId;

    @Schema(description = "所属部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String deptName;

    /**
     * 优先级 1紧急 2高 3中 4底
     */
    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "优先级不能为空")
    private String priority;

    /**
     * 执行门店类型 1全部门店 2部分门店
     */
    @Schema(description = "执行门店类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer storeType;

    /**
     * 执行门店信息
     */
    @Schema(description = "执行门店信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<StoreInfoReqVO> storeInfos;

    @Schema(description = "执行人", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorUserId;

    @Schema(description = "执行人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorUserName;

    @Schema(description = "执行部门", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorDeptId;

    @Schema(description = "执行部门姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorDeptName;

    @Schema(description = "期望完成时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "期望完成时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime completionTime;

    @Schema(description = "附件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String fileUrls;

    @Schema(description = "项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目不能为空")
    private Long businessId;

    @Schema(description = "动态表单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "动态表单不能为空")
    private String formMsg;

    @Schema(description = "图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    //@NotNull(message = "图标不能为空")
    private String iconUrl;

    @Schema(description = "OA项目ID")
    private Long oaProjectId;

}
