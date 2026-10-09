package com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 订单生成后加购商品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AfterOrderRespVO {

    @Schema(description = "订单生成后加购商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15719")
    @ExcelProperty("订单生成后加购商品ID")
    private Long afterId;

    @Schema(description = "商品ID", example = "5573")
    @ExcelProperty("商品ID")
    private Long commodityId;

    @Schema(description = "加购价格", example = "14445")
    @ExcelProperty("加购价格")
    private BigDecimal afterPrice;

    @Schema(description = "划线价格", example = "23423")
    @ExcelProperty("划线价格")
    private BigDecimal strikeThroughPrice;

    @Schema(description = "状态 1开启 0下架", example = "1")
    @ExcelProperty("状态 1开启 0下架")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商品名称", example = "王五")
    @ExcelProperty("商品名称")
    private String commodityName;

    @Schema(description = "商品缩略图", example = "https://www.iocoder.cn")
    @ExcelProperty("商品缩略图")
    private String thumbnailUrl;

    @Schema(description = "销量", example = "12")
    @ExcelProperty("销量")
    private Long salesVolumes;

    @Schema(description = "回显标注", example = "王五")
    private String dictValue;

}
