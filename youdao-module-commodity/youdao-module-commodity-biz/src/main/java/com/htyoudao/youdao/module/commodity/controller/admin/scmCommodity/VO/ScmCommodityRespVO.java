package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO;

import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "供应链商品返回对象")
@Data
public class ScmCommodityRespVO {

    @Schema(description = "ID")
    private Long id ;


    @Schema(description = " 二级类目名称")
    private String stasticsCommodityName;

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "商品编号")
    private String commodityCode;


    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "规格")
    private String specifications;


    @Schema(description = "最小单位")
    private String minUnit;

    @Schema(description = "上下架 0否 1是")
    private Integer onTheShelf;

    @Schema(description = "所有单位")
    private List<String> allUnit;

    /**
     * 出库单价
     */
    private BigDecimal outPrice;
    /**
     * 出库单位
     */
    private String outUnit;

    private List<ScmUnitConversion> scmUnitConversionList = new ArrayList<>();
}
