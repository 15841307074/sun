package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 签到活动数据分析折线图 Request VO")
@Data
public class ActivitySignDailyAnalysisReqVO {

  @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
  @NotNull(message = "活动ID不能为空")
  private Long activityId;


  @Schema(description = "开始时间；不传时默认最近30天")
  @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime startTime;

  @Schema(description = "结束时间；不传时默认最近30天")
  @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime endTime;

  @Schema(hidden = true, description = "后端调试参数：true 时跳过中间件，直接查询 MySQL")
  private Boolean forceMysql;
}
