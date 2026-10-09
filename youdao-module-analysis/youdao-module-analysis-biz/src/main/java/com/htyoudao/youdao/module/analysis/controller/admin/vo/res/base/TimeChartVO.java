package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 折线图返回对象 Response VO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeChartVO {
    @Schema(description = "时间")
    private String time;

    @Schema(description = "指标值")
    private Double value;

    public static List<TimeChartVO> convert(Map<String, Double> currentResult) {
        List<TimeChartVO> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : currentResult.entrySet()) {
            TimeChartVO timeChartVO = new TimeChartVO();
            timeChartVO.setTime(entry.getKey());
            timeChartVO.setValue(entry.getValue());
            result.add(timeChartVO);
        }
        return result;
    }
}
