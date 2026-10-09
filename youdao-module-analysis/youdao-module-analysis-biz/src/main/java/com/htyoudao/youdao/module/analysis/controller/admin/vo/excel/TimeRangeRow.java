package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeRangeRow {

    @ExcelProperty(value = "日期", index = 0)
    private String date;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public TimeRangeRow(String date, LocalDateTime startDate, LocalDateTime endDate) {
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
