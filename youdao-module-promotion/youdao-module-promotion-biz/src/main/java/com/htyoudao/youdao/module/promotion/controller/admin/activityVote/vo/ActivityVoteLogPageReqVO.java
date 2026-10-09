package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ActivityVoteLogPageReqVO {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "选项id")
    private Long optionId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "参与门店id")
    private Long storeId;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "导出来源 999=手动任务绕过限制")
    private Integer exportSource;
}
