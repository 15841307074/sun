package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Data;

@Data
@Schema(description = "app - 原材料商品返回 VO")
public class RawMaterialDetailVO {

    /**
     * 主键ID
     */
    @Schema(description = "原材料ID")
    private Long id;

    /**
     * 门店ID
     */
    @Schema(description = "门店ID")
    private Long storeId;

    /**
     * 类别ID
     */
    @Schema(description = "类别ID")
    private Long categoryId;

    /**
     * 原始商品ID
     */
    @Schema(description = "原始商品ID")
    private Long commodityId;

    /**
     * 类别名称
     */
    @Schema(description = "类别名称")
    private String categoryName;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String commodityName;

    /**
     * 商品编号
     */
    @Schema(description = "商品编号")
    private String commodityCode;

    /**
     * 商品统计名称
     */
    @Schema(description = "商品统计名称")
    private String stasticsCommodityName;

    /**
     * 规格
     */
    @Schema(description = "规格")
    private String specifications;

    /**
     * 规格单位换算规则
     */
    @Schema(description = "规格单位换算规则")
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
    private String minUnit;

    /**
     * 最小单位单价
     */
    @Schema(description = "最小单位单价")
    private BigDecimal unitPrice;


    /**
     * 出库单位
     */
    @Schema(description = "出库单位")
    private String outUnit;

    /**
     * 出库单价
     */
    @Schema(description = "出库单价")
    private BigDecimal outPrice;

    /**
     * 库存数量
     */
    @Schema(description = "库存数量（系统库存）")
    private BigDecimal stock;

    /**
     * 停用状态(0停用 1启用)
     */
    @Schema(description = "停用状态(0停用 1启用)")
    private Integer isEnable;

// ==========需要计算的信息=============

    /**
     * 系统库存金额 = 系统库存 * 单价
     */
    @Schema(description = "系统库存金额")
    private BigDecimal stockAmount;


    /**
     * 损耗库存数量
     */
    @Schema(description = "损耗库存数量")
    private BigDecimal lossCount;


    /**
     * 损耗金额
     */
    @Schema(description = "损耗金额")
    private BigDecimal lossAmount;


    /**
     * 盘点数量
     */
    @Schema(description = "盘点数量")
    private BigDecimal takeCount;


    /**
     * 盘点库存金额
     */
    @Schema(description = "盘点库存金额")
    private BigDecimal takeAmount;

    /**
     * 库存差值
     */
    @Schema(description = "库存差值")
    private BigDecimal inventoryDifference;

    /**
     * 库存金额差值
     */
    @Schema(description = "库存金额差值")
    private BigDecimal inventoryDifferenceAmount;


//========保存时候填写的信息

    @Schema(description = "盘点选择的单位")
    private String chooseUnit;


    /**
     * 根据 选择单位和换算规则 计算出最小单位需要乘的比例
     * @param chooseUnit
     * @return
     */
    public BigDecimal caleRateByChooseUnit(String chooseUnit) {
        String specificationsRules = this.specificationsRules;
        if (StringUtils.isEmpty(specificationsRules) || StringUtils.isEmpty(chooseUnit)){
            return BigDecimal.ONE;
        }

        JSONArray rulesArray = JSON.parseArray(specificationsRules);
        for (int i = 0; i < rulesArray.size(); i++) {
            JSONObject rule = rulesArray.getJSONObject(i);
            String beforeUnit = rule.getString("beforeUnit");
            if (chooseUnit.equals(beforeUnit)) {
                BigDecimal beforeNumber = rule.getBigDecimal("beforeNumber"); //1箱
                BigDecimal afterNumber = rule.getBigDecimal("afterNumber"); //100包

                // 检查转换数值有效性
                if (beforeNumber.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO; // 除零保护
                }

                // 计算当前步骤的转换比例
                return beforeNumber.divide(afterNumber, 10, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ONE;
    }


    /**
     * 根据换算比例，计算展示金额
     * @param rate
     */
    public void caleAmountByRate(BigDecimal rate){
        //盘点库存金额 = 最小盘点库存 * 最小单位单价
        this.takeAmount = unitPrice.multiply(this.takeCount);
        //系统库存金额 = 最小系统库存 * 最小单位单价
        this.stockAmount = unitPrice.multiply(this.stock);
        //盘点库存 = 盘点库存 * 换算比例
        this.takeCount = takeCount.multiply(rate);
        //盘点库存 = 盘点库存 * 换算比例
        this.stock = stock.multiply(rate);

        //库存差额 =  盘点数量 - 库存数量
        this.inventoryDifference = this.takeCount.subtract(this.stock);
        //库存金额差值 = 库存差额 * 单价
        this.inventoryDifferenceAmount = this.takeAmount.subtract(this.stockAmount);
    }


}
