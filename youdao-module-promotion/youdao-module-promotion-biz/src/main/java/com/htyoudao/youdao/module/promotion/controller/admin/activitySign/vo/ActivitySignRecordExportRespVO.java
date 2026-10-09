package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 管理后台 - 签到记录导出 Response VO。 */
@Data
public class ActivitySignRecordExportRespVO {

  @ExcelProperty("会员昵称")
  @Schema(description = "会员昵称")
  private String memberName;

  @ExcelProperty("联系方式")
  @Schema(description = "联系方式")
  private String memberMobile;

  @ExcelProperty("会员ID")
  @Schema(description = "本次触发签到的会员ID")
  private String memberId;

  @ExcelProperty("签到门店")
  @Schema(description = "签到门店")
  private String storeName;

  @ExcelProperty("签到门店ID")
  @Schema(description = "签到门店ID")
  private String storeId;

  @ExcelProperty("签到日期")
  @Schema(description = "签到日期")
  private LocalDate signDate;

  @ExcelProperty("签到时间")
  @Schema(description = "签到时间")
  private LocalDateTime signTime;

  @ExcelProperty("签到类型")
  @Schema(description = "签到类型")
  private String signTypeName;

  @ExcelProperty("连签周期次数")
  @Schema(description = "根据查询入参 continuousCycleDays 计算出的连签周期次数")
  private Integer continuousCycleCount;

  @ExcelProperty("连续签到天数")
  @Schema(description = "连续签到天数")
  private Integer maxContinuousDays;

  @ExcelProperty("累计签到天数")
  @Schema(description = "累计签到天数")
  private Integer totalSignDays;
}
