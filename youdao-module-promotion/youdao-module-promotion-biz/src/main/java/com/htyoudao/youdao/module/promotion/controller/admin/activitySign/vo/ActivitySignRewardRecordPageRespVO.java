package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 发放记录分页 Response VO")
@Data
public class ActivitySignRewardRecordPageRespVO {

    @Schema(description = "发放人数：该活动奖励发放人数之和")
    private Long rewardUserCount;

    @Schema(description = "发放次数：该活动奖励发放次数之和")
    private Long rewardIssueCount;

    @Schema(description = "当前页数据列表")
    private List<ActivitySignRewardRecordRespVO> list;

    @Schema(description = "总条数")
    private Long total;
}
