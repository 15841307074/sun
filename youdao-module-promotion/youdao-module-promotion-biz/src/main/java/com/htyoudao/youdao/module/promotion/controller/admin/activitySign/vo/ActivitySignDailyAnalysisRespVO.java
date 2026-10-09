package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动数据分析折线图 Response VO")
@Data
public class ActivitySignDailyAnalysisRespVO {

  @Schema(description = "日期列表，格式 yyyy-MM-dd")
  private List<String> dateList;

  @Schema(description = "每日浏览量 PV，与 dateList 下标对应")
  private List<Long> pvList;

  @Schema(description = "每日访客量 UV，与 dateList 下标对应")
  private List<Long> uvList;
}
