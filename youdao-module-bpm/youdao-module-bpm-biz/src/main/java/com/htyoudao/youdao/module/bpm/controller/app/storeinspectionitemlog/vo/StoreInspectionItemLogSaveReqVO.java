package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店项操作变更日志新增/修改 Request VO")
@Data
public class StoreInspectionItemLogSaveReqVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18272")
    private Long id;

    @Schema(description = "项目ID", example = "25401")
    private Long businessId;

    @Schema(description = "所属巡店记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2963")
    @NotNull(message = "所属巡店记录ID不能为空")
    private Long recordId;

    @Schema(description = "所属巡店明细项ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17557")
    @NotNull(message = "所属巡店明细项ID不能为空")
    private Long recordItemId;

    @Schema(description = "操作人ID", example = "17816")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "0090")
    private String operatorName;

    @Schema(description = "操作时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime operateTime;

    @Schema(description = "变更前数据快照")
    private String beforeSnap;

    @Schema(description = "变更后数据快照")
    private String afterSnap;

    @Schema(description = "修改原因备注", example = "你说的对")
    private String remark;

}