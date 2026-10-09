package com.htyoudao.youdao.module.analysis.service.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangeDTO {
    private String name;
    private Double from;
    private Double to;
//    private Integer type;

    public RangeDTO(Double from, Double to) {
        this.from = from;
        this.to = to;
    }


    public Set<Integer> calcHours() {
        Double from = this.from;
        Double to = this.to;

        if (from == null || to == null){
            return Set.of();
        }

        HashSet<Integer> hours = new HashSet<>();

        int startHour = from.intValue();
        int endHour = to.intValue();

        // 生成小时序列
        for (int hour = startHour; hour <= endHour; hour++) {
            hours.add(hour % 24);
        }
        return hours;
    }

}
