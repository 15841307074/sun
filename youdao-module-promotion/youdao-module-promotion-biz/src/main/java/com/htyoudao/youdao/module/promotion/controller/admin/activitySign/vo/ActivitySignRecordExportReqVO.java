package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.htyoudao.youdao.framework.excel.core.pojo.SearchAfterSupport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 签到记录导出 Request VO")
@Data
public class ActivitySignRecordExportReqVO implements SearchAfterSupport {

    @Schema(description = "活动ID，当前活动详情页必传", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "手机号", example = "13800138000")
    private String memberMobile;

    @Schema(description = "签到门店ID", example = "101")
    private Long storeId;

    @Schema(description = "连签周期；必填，前端默认从活动详情continuousCycleDays取值；允许用户输入>0整数，最多365；用于汇总计算用户满足连签周期的次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "连签周期不能为空")
    @Min(value = 1, message = "连签周期最小为1")
    @Max(value = 365, message = "连签周期最大为365")
    private Integer continuousCycleDays;

    @Schema(description = "签到开始时间", example = "2026-06-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime signTimeStart;

    @Schema(description = "签到结束时间", example = "2026-06-30 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime signTimeEnd;

    @Schema(description = "导出来源：1 PC端导出；999 手动导出。PC不传默认1，超过30万拒绝；999跳过30万限制", example = "1")
    private Integer exportSource;

    @JsonIgnore
    @Schema(hidden = true, description = "ES search_after 游标，仅导出内部使用")
    private List<FieldValue> searchAfter;

    @Override
    public void setSearchAfter(List<FieldValue> searchAfter) {
        this.searchAfter = searchAfter;
    }
}

