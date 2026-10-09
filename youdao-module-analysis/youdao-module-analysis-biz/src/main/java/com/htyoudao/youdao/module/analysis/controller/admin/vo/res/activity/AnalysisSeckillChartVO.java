package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

import java.util.List;


@Schema(description = "管理后台 - 折线图返回对象")
@Data
public class AnalysisSeckillChartVO {

    @Schema(description = "x轴时间")
    private List<String> times;

    @Schema(description = "y轴PV时间段数据")
    private List<Long> pvValues;

    @Schema(description = "y轴UV时间段数据")
    private List<Long> uvValues;
}
