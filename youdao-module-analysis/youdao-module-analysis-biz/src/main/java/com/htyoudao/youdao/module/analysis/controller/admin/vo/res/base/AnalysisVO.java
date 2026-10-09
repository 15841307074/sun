package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnalysisVO<T>{

    @Schema(description = "当前时间段 数据")
    private T current;

    @Schema(description = "环比时间段 数据")
    private T before;

    public AnalysisVO(T current, T before) {
        this.current = current;
        this.before = before;
    }

}
