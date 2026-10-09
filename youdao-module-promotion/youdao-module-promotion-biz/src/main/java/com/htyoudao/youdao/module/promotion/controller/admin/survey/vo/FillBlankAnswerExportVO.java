package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 填空题答案导出 VO")
@Data
public class FillBlankAnswerExportVO {

    @ExcelProperty(index = 0, value = "序号")
    @ColumnWidth(8)
    private Integer seq;

    @ExcelProperty(index = 1, value = "提交答卷时间")
    @ColumnWidth(22)
    private LocalDateTime submitTime;

    @ExcelProperty(index = 2, value = "答案文本")
    @ColumnWidth(50)
    private String answerText;

    @ExcelProperty(index = 3, value = "答卷ID")
    @ColumnWidth(20)
    private Long answerId;
}
