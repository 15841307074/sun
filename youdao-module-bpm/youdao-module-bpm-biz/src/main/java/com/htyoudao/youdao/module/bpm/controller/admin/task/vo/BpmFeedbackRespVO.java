package com.htyoudao.youdao.module.bpm.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "OA反馈 - 列表 VO")
@Data
public class BpmFeedbackRespVO {

    @Schema(description = "执行人", example = "12345")
    private Long executorUserId;

    @Schema(description = "执行人", example = "12345")
    private String executorUserName;

    @Schema(description = "所属部门", example = "门店")
    private String storeName;

    @Schema(description = "任务状态", example = "30")
    private Integer taskState;

    @Schema(description = "任务反馈与附件", example = "5")
    private String feedBackMsg;

    @Schema(description = "任务完成时间", example = "2025/10/28 00:30:00")
    private LocalDateTime feedBackTime;

    @Schema(description = "更新时间", example = "2025/10/28 00:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "流程唯一标识", example = "abc-123")
    private String procInstId;

    @Schema(description = "父项流程标识", example = "abc-123")
    private String parentProcInstId;

}
