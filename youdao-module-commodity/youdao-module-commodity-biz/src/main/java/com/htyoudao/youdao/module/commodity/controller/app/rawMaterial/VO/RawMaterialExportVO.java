package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "app - 原材料商品导出 VO")
public class RawMaterialExportVO {

    @ExcelProperty(value = "序号", index = 0)
    @ColumnWidth(8)
    private Integer index;

    @ExcelProperty(value = "商品类别", index = 1)
    @ColumnWidth(15)
    @Schema(description = "类别名称")
    private String stasticsCommodityName;

    @ExcelProperty(value = "商品名称", index = 2)
    @ColumnWidth(20)
    @Schema(description = "商品名称")
    private String commodityName;

    @ExcelProperty(value = "品牌", index = 3)
    @ColumnWidth(15)
    @Schema(description = "品牌名称")
    private String brandName;

    @ExcelProperty(value = "规格", index = 4)
    @ColumnWidth(15)
    @Schema(description = "规格")
    private String specifications;

    @ExcelProperty(value = "单价", index = 5)
    @ColumnWidth(30)
    @Schema(description = "单价")
    private String unitPrice;

    @ExcelProperty(value = "系统库存", index = 6)
    @ColumnWidth(30)
    @Schema(description = "库存数量（系统库存）")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String stock;

    @ExcelProperty(value = "盘点库存", index = 7)
    @ColumnWidth(30)
    @Schema(description = "盘点数量")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String takeCount;

    @ExcelProperty(value = "库存差值", index = 8)
    @ColumnWidth(30)
    @Schema(description = "库存差值")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String inventoryDifference;

    @ExcelProperty(value = "损耗库存", index = 9)
    @ColumnWidth(30)
    @Schema(description = "损耗库存数量")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String lossCount;

    @ExcelProperty(value = "损耗金额", index = 10)
    @ColumnWidth(30)
    @Schema(description = "损耗金额")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String lossAmount;

    @ExcelProperty(value = "库存金额", index = 11)
    @ColumnWidth(30)
    @Schema(description = "系统库存金额")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String stockAmount;

    @ExcelProperty(value = "盘点库存金额", index = 12)
    @ColumnWidth(30)
    @Schema(description = "盘点库存金额")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String takeAmount;

    @ExcelProperty(value = "金额差值", index = 13)
    @ColumnWidth(30)
    @Schema(description = "库存金额差值")
    @ContentStyle(dataFormat = 2) // 数字格式
    private String inventoryDifferenceAmount;


    public RawMaterialExportVO(RawMaterialDetailVO detailVO){
        this.stasticsCommodityName = detailVO.getStasticsCommodityName();
        this.commodityName = detailVO.getCommodityName();
        this.brandName = detailVO.getBrandName();
        this.specifications = detailVO.getSpecifications();
        this.unitPrice = formatBigDecimal(detailVO.getOutPrice()) + "/" + detailVO.getOutUnit() ;
        this.stock = formatBigDecimal(detailVO.getStock()) + detailVO.getChooseUnit();
        this.takeCount = formatBigDecimal(detailVO.getTakeCount()) + detailVO.getChooseUnit();
        this.inventoryDifference = formatBigDecimal(detailVO.getInventoryDifference()) + detailVO.getChooseUnit();
        this.lossCount = formatBigDecimal(detailVO.getLossCount()) + detailVO.getChooseUnit();
        this.lossAmount = formatBigDecimal(detailVO.getLossAmount());
        this.stockAmount = formatBigDecimal(detailVO.getStockAmount());
        this.takeAmount = formatBigDecimal(detailVO.getTakeAmount());
        this.inventoryDifferenceAmount = formatBigDecimal(detailVO.getInventoryDifferenceAmount());
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.4f", value);
    }
}
