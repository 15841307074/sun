package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmNotRelatedReqVO extends PageParam {

    @Schema(description = "任务标题或者内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String taskContent;

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String processDefinitionId;

    @Schema(description = "发起人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long applicantId;



}
