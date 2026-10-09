package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/*
日志记录表
 */
@TableName("raw_material_log")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialLogDO extends BusinessBaseDO{

    @Schema(description = "日志记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "日志类型：1配方找不到 2门店下原材料找不到 3门店分类下无对应原材料 4单位不匹配", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer logType;

    @Schema(description = "原材料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unitName;

    @Schema(description = "skuId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    @Schema(description = "0 销售  1 损耗", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;


    @Schema(description = "销量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal consumption;

    @Schema(description = "是否允许替换 1 是 0否")
    private Long isAlternative;
    @Schema(description = "错误单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unitError;


}
