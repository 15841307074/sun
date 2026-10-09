package com.htyoudao.youdao.framework.excel.core.service.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryLogStatisticsVO {


    @ExcelIgnore  // 添加这个注解，Excel导出时会忽略该字段
    @Schema(name = "storeId", description = "门店ID")
    private Long storeId;

    @ExcelProperty("抽奖门店")
    @Schema(name = "storeName", description = "抽奖门店")
    private String storeName;


    @ExcelProperty("参与人数")
    @Schema(name = "participantsCount", description = "参与人数")
    private String participantsCount;

    @ExcelProperty("抽奖次数")
    @Schema(name = "lotteryCount", description = "抽奖次数")
    private String lotteryCount;



}
