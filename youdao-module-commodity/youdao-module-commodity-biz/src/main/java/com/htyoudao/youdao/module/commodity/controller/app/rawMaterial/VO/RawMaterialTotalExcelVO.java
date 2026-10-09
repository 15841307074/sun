package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
@ContentStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialTotalExcelVO {

    // 前5列为空，确保从第6列开始
    @ExcelProperty(value = "", index = 0)
    private String empty1;

    @ExcelProperty(value = "", index = 1)
    private String empty2;

    @ExcelProperty(value = "", index = 2)
    private String empty3;

    @ExcelProperty(value = "", index = 3)
    private String empty4;

    @ExcelProperty(value = "", index = 4)
    private String empty5;

    @ExcelProperty(value = "", index = 5)
    private String empty6;


    @ExcelProperty(value = "系统库存合计", index = 6)
    private String totalStockWithLabel;

    @ExcelProperty(value = "盘点库存合计", index = 7)
    private String totalTakeCountWithLabel;

    @ExcelProperty(value = "库存差值合计", index = 8)
    private String totalInventoryDifferenceWithLabel;

    @ExcelProperty(value = "损耗库存合计", index = 9)
    private String totalLossCountWithLabel;

    @ExcelProperty(value = "损耗金额合计", index = 10)
    private String totalLossAmountWithLabel;

    @ExcelProperty(value = "库存金额合计", index = 11)
    private String totalStockAmountWithLabel;

    @ExcelProperty(value = "盘点库存金额合计", index = 12)
    private String totalTakeAmountWithLabel;

    @ExcelProperty(value = "金额差值合计", index = 13)
    private String totalInventoryDifferenceAmountWithLabel;

    @ExcelProperty(value = "三方实际出库金额", index = 14)
    private String channelOutAmountWithLabel;

    @ExcelProperty(value = "平台实际出库金额", index = 15)
    private String actualOutAmountWithLabel;

    @ExcelProperty(value = "实际出库总金额", index = 16)
    private String allOutAmountWithLabel;

    // 添加方法将数值转换为带标签的字符串
    public RawMaterialTotalExcelVO(RawMaterialTotalRespVO totalRespVO) {
        if (totalRespVO == null){
            return;
        }
        this.totalStockWithLabel = "系统库存合计：" + formatBigDecimal(totalRespVO.getStock());
        this.totalTakeCountWithLabel = "盘点库存合计：" + formatBigDecimal(totalRespVO.getTakeCount());
        this.totalInventoryDifferenceWithLabel = "库存差值合计：" + formatBigDecimal(totalRespVO.getInventoryDifference());
        this.totalLossCountWithLabel = "损耗库存合计：" + formatBigDecimal(totalRespVO.getLossCount());
        this.totalLossAmountWithLabel = "损耗金额合计：" + formatBigDecimal(totalRespVO.getLossAmount());
        this.totalStockAmountWithLabel = "库存金额合计：" + formatBigDecimal(totalRespVO.getStockAmount());
        this.totalTakeAmountWithLabel = "盘点库存金额合计：" + formatBigDecimal(totalRespVO.getTakeAmount());
        this.totalInventoryDifferenceAmountWithLabel = "金额差值合计：" + formatBigDecimal(totalRespVO.getInventoryDifferenceAmount());
        this.channelOutAmountWithLabel = "三方实际出库金额合计：" + formatBigDecimal(totalRespVO.getChannelOutAmount());
        this.actualOutAmountWithLabel = "平台实际出库金额合计：" + formatBigDecimal(totalRespVO.getOutAmount());
        this.allOutAmountWithLabel = "实际出库总金额合计：" + formatBigDecimal(totalRespVO.getAllOutAmount());

    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.4f", value);
    }
}