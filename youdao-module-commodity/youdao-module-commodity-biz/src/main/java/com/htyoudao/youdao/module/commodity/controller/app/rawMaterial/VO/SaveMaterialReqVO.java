package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "保存原材料")
public class SaveMaterialReqVO {

    /**
     * 门店ID
     */
    @Schema(description = "门店 ID")
    @NotNull(message = "门店 ID 不能为空")
    private Long storeId;

    /**
     * 类别ID
     */
    @Schema(description = "类别ID")
    @NotNull(message = "类别ID 不能为空")
    private Long categoryId;

    /**
     * 原始商品ID
     */
    @Schema(description = "商品ID")
    @NotNull(message = "商品ID 不能为空")
    private Long commodityId;

    /**
     * 类别名称
     */
    @Schema(description = "类别名称")
    @NotNull(message = "类别名称 不能为空")
    private String categoryName;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    @NotNull(message = "商品名称 不能为空")
    @DiffLogField(name = "商品名称")
    private String commodityName;

    /**
     * 商品编号
     */
    @Schema(description = "商品编号")
    @NotNull(message = "商品编号 不能为空")
    private String commodityCode;

    /**
     * 商品统计名称
     */
    @Schema(description = "商品统计名称")
    @NotNull(message = "商品统计名称 不能为空")
    @DiffLogField(name = "商品统计名称")
    private String stasticsCommodityName;

    /**
     * 规格
     */
    @Schema(description = "规格")
    @DiffLogField(name = "规格")
    private String specifications;

    /**
     * 规格单位换算规则
     */
    @Schema(description = "规格单位换算规则")
    @DiffLogField(name = "换算规则")
    private String specificationsRules;

    /**
     * 品牌ID
     */
    @Schema(description = "品牌ID")
    private Long brandId;

    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 类型
     */
    @Schema(description = "类型")
    private String commodityType;

    /**
     * 最小单位
     */
    @Schema(description = "最小单位")
    @DiffLogField(name = "最小单位")
    private String minUnit;

    /**
     * 最小单位单价
     */
    @Schema(description = "最小单位单价")
    @DiffLogField(name = "最小单位单价")
    private BigDecimal unitPrice;

    /**
     * 出库单位
     */
    @Schema(description = "出库单位")
    @DiffLogField(name = "出库单位")
    private String outUnit;

    /**
     * 出库单价
     */
    @Schema(description = "出库单价")
    @DiffLogField(name = "出库单价")
    private BigDecimal outPrice;

    /**
     * 采购时间
     */
    @Schema(description = "采购时间")
    @DiffLogField(name = "采购时间")
    private LocalDateTime inTime;


    private Long id;
}
