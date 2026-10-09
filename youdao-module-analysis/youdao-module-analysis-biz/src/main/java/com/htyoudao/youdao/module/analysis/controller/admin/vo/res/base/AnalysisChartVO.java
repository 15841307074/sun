package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;


@Schema(description = "管理后台 - 折线图返回对象")
@Data
public class AnalysisChartVO {

    @Schema(description = "x轴时间")
    private List<String> times;

    @Schema(description = "y轴当前时间段数据")
    @NumberFormat(pattern = "0.00")
    private List<Double> currentValues;

    @Schema(description = "y轴同比时间段数据")
    @NumberFormat(pattern = "0.00")
    private List<Double> beforeValues;
}
