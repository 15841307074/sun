package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.TreeMap;

@Data
public class AnalysisRecordsVO {

    @Schema(description = "指标值")
    private TreeMap<String, Double> values;


    // 新增会员相关字段
    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberMobile;

    @Schema(description = "支付时间")
    private String payTime; // 建议使用String类型存储格式化后的时间，如"yyyy-MM-dd HH:mm:ss"

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "支付金额")
    private Double payAmount;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "门店名称")
    private String storeName;
}
