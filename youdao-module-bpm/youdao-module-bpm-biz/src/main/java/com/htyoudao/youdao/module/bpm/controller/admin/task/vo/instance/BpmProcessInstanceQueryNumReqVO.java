package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@Schema(description = "管理后台 - 统计任务 Request VO")
public class BpmProcessInstanceQueryNumReqVO {

    @Schema(description = "发起人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantId;

    @Schema(description = "发起部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantDeptId;

    @Schema(description = "发起门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantStoreId;

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String processDefinitionId;

    @Schema(description = "执行人", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> executorUserId;

    @Schema(description = "执行门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> executorStoreId;


    @Schema(description = "执行部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long executorDeptId;

    @Schema(description = "创建时间开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime createTimeStart;

    @Schema(description = "创建时间结束", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime createTimeEnd;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer taskType;

    @Schema(description = "项目 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long oaProjectId;

    @Schema(description = "关联oa项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 不关联 1关联 2全部")
    @NotNull
    private Integer oaProjectFlag;

    private List<String> proInstIdList;



    /**
     * 处理时间参数，如果为空则设置默认时间
     * createTimeEnd 默认为现在时间
     * createTimeStart 默认为一个月前的今天  // 注释更新为“一个月前”
     */
    public void processTimeParams() {
        // 如果 createTimeEnd 为空、空字符串或者是默认的 1970 年，设置为现在时间
        if (createTimeEnd == null || createTimeEnd.year() == 1970 ||
                (createTimeEnd.toString().trim().isEmpty())) {
            createTimeEnd = new DateTime();
        }

        // 如果 createTimeStart 为空、空字符串或者是默认的 1970 年，设置为一个月前的今天  // 注释更新
        if (createTimeStart == null || createTimeStart.year() == 1970 ||
                (createTimeStart.toString().trim().isEmpty())) {
            // 核心修改：将年份偏移改为月份偏移，-1 表示往前推181天
            createTimeStart = new DateTime().offset(DateField.YEAR, -1);
        }
    }

    @Schema(description = "发起部门ID及下属所有部门", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> applicantDeptIdList;
}
