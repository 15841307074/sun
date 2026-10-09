package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 签到记录分页 Response VO")
@Data
public class ActivitySignRecordPageRespVO {

    @Schema(description = "签到人数：该活动参与签到活动汇总人数")
    private Long signUserCount;

    @Schema(description = "签到次数：该活动参与签到次数汇总")
    private Long signCount;

    @Schema(description = "当前页数据列表")
    private List<ActivitySignRecordRespVO> list;

    @Schema(description = "总条数")
    private Long total;
}
