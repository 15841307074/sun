package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店项操作变更日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoreInspectionItemLogRespVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18272")
    @ExcelProperty("日志ID")
    private Long id;

    @Schema(description = "项目ID", example = "25401")
    @ExcelProperty("项目ID")
    private Long businessId;

    @Schema(description = "所属巡店记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2963")
    @ExcelProperty("所属巡店记录ID")
    private Long recordId;

    @Schema(description = "所属巡店明细项ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17557")
    @ExcelProperty("所属巡店明细项ID")
    private Long recordItemId;

    @Schema(description = "操作人ID", example = "17816")
    @ExcelProperty("操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "0090")
    @ExcelProperty("操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("操作时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime operateTime;

    @Schema(description = """
            变更前数据快照{
              "title": "产品腌制比例是否正确",
              "status": 0,          // 0:不合格, 1:合格, 2:不适用
              "score": 0,           // 实际得分
              "max_score": 2,       // 标准总分
              "reward_amount": -300.00,\s
              "images": ["url1", "url2"],
              "comment": "现场腌制均按公司规定制作，不合格"
            }""")
    private String beforeSnap;

    @Schema(description = """
            变更后数据快照{
              "title": "产品腌制比例是否正确",
              "status": 0,          // 0:不合格, 1:合格, 2:不适用
              "score": 0,           // 实际得分
              "max_score": 2,       // 标准总分
              "reward_amount": -300.00,\s
              "images": ["url1", "url2"],
              "comment": "现场腌制均按公司规定制作，不合格"
            }""")
    private String afterSnap;

    @Schema(description = "修改原因备注", example = "你说的对")
    @ExcelProperty("修改原因备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime createTime;

}