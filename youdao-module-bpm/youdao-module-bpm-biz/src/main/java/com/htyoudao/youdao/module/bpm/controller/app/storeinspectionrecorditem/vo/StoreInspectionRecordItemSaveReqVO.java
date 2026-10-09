package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店记录明细快照新增/修改 Request VO")
@Data
public class StoreInspectionRecordItemSaveReqVO {

    @Schema(description = "记录明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4595")
    private Long id;

    @Schema(description = "应传图片数量快照")
    private Integer imgCountSnap;

    @Schema(description = "检查结果：0 不合格, 1 合格, 2 不适用", example = "2")
    @NotNull(message = "actualStatus不能为空")
    private Integer actualStatus;

    @Schema(description = "实际得分")
    @NotNull(message = "actualScore不能为空")
    private Integer actualScore;

    @Schema(description = "奖惩金额")
    private BigDecimal rewardAmount;

    @Schema(description = "巡店人备注")
    private String actualComment;

    @Schema(description = "图片地址(多图用逗号或JSON数组存储)")
    private String actualImages;

    @Schema(description = "是否已整改：0 否, 1 是")
    private Boolean isRectified;

    @Schema(description = "整改完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime rectifyTime;

}