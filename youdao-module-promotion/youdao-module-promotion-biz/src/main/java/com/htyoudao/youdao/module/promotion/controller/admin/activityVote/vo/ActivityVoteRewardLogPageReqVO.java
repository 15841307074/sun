package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ActivityVoteRewardLogPageReqVO extends PageParam {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品状态")
    private Integer prizeState;

    @Schema(description = "红包领取状态 1未领取 2已领取 3已失效")
    private List<Integer> claimStatus ;

    @Schema(description = "收货地址 1未填写 2已填写")
    private Integer receiveAddress;

    @Schema(description = "快递单号 1未填写 2已填写")
    private Integer trackingNumber;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "导出来源 999=手动任务绕过限制")
    private Integer exportSource;
}
