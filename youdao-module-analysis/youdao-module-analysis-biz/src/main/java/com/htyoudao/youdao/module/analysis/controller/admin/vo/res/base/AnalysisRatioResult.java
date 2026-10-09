package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisRatioResult {

    @Schema(description = "占比")
    private Double total;

    @Schema(description = "值")
    private List<AnalysisRangeVO> data;

    public AnalysisRatioResult(Map<String, Double> currentResult) {
        if (CollectionUtils.isEmpty(currentResult)) {
            this.total = 0.0;
            this.data = Collections.emptyList();
            return;
        }

        double total = currentResult.values().stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();

        List<AnalysisRangeVO> resultList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : currentResult.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue() != null ? entry.getValue() : 0.0;
            double ratio = total == 0.0 ? 0.0 : value / total;
            resultList.add(new AnalysisRangeVO(key, value, null, ratio));
        }

        this.data = resultList;
        this.total = total;
    }


    public AnalysisRatioResult(Map<String, Double> currentResult, Map<String, Double> beforeResult) {
        if (CollectionUtils.isEmpty(currentResult)) {
            this.total = 0.0;
            this.data = Collections.emptyList();
            return;
        }

        double total = currentResult.values().stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();

        List<AnalysisRangeVO> resultList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : currentResult.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue() != null ? entry.getValue() : 0.0;
            double ratio = total == 0.0 ? 0.0 : value / total;
            resultList.add(new AnalysisRangeVO(key, value, beforeResult.getOrDefault(key, 0.0), ratio));
        }
        this.data = resultList;
        this.total = total;
    }


    public AnalysisRatioResult(Map<String, Double> currentResult, Map<String, Double> beforeResult, List<RangeDTO> list) {
        if (CollectionUtils.isEmpty(currentResult)) {
            this.total = 0.0;
            this.data = Collections.emptyList();
            return;
        }

        double total = currentResult.values().stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();

        List<AnalysisRangeVO> resultList = new ArrayList<>();

        for (RangeDTO rangeDTO : list) {
            String key = rangeDTO.getName();
            Double value = currentResult.getOrDefault(key, 0.0);
            double ratio = total == 0.0 ? 0.0 : value / total;
            resultList.add(new AnalysisRangeVO(key, value, beforeResult.getOrDefault(key, 0.0), ratio));
        }

        this.data = resultList;
        this.total = total;
    }
}
