package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "app - 原材料商品累计返回 VO")
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
@ContentStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
public class RawMaterialTotalRespVO {


    /**
     * 库存数量
     */
    @Schema(description = "库存数量（系统库存）")
    @ExcelProperty(value = "系统库存合计", index = 6)
    private BigDecimal stock;


    /**
     * 盘点数量
     */
    @Schema(description = "盘点数量")
    @ExcelProperty(value = "盘点库存合计", index = 7)
    private BigDecimal takeCount;

    /**
     * 库存差值
     */
    @Schema(description = "库存差值")
    @ExcelProperty(value = "库存差值合计", index = 8)
    private BigDecimal inventoryDifference;

    /**
     * 损耗库存数量
     */
    @Schema(description = "损耗库存数量")
    @ExcelProperty(value = "损耗库存合计", index = 9)
    private BigDecimal lossCount;

    /**
     * 损耗金额
     */
    @Schema(description = "损耗金额")
    @ExcelProperty(value = "损耗金额合计", index = 10)
    private BigDecimal lossAmount;


    /**
     * 系统库存金额 = 系统库存 * 单价
     */
    @Schema(description = "系统库存金额")
    @ExcelProperty(value = "库存金额合计", index = 11)
    private BigDecimal stockAmount;


    /**
     * 盘点库存金额
     */
    @Schema(description = "盘点库存金额")
    @ExcelProperty(value = "盘点库存金额合计", index = 12)
    private BigDecimal takeAmount;

    /**
     * 库存金额差值
     */
    @Schema(description = "库存金额差值")
    @ExcelProperty(value = "金额差值合计", index = 13)
    private BigDecimal inventoryDifferenceAmount;

    @Schema(description = "三方实际出库金额")
    @ExcelProperty(value = "三方实际出库金额", index = 14)
    private BigDecimal channelOutAmount;

    @Schema(description = "平台实际出库金额")
    @ExcelProperty(value = "平台实际出库金额", index = 15)
    private BigDecimal outAmount;

    @Schema(description = "实际出库总金额")
    @ExcelProperty(value = "实际出库总金额", index = 16)
    private BigDecimal allOutAmount;

    @Schema(description = "损耗率")
    private BigDecimal attritionRate;
}
