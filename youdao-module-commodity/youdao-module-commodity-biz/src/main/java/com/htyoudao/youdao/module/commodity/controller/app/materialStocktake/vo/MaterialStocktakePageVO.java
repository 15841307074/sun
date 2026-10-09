package com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MaterialStocktakePageVO {

    /**
     * 盘点单ID
     */
    @Schema(description = "盘点单ID")
    private Long id;

    /**
     * 门店ID
     */
    @Schema(description = "门店ID")
    private Long storeId;


    @Schema(description = "门店名称")
    private String storeName;


    /**
     * 盘点状态(1:盘盈单 2.盘亏单)
     */
    @Schema(description = "盘点状态(1:盘盈单 2.盘亏单)")
    private Integer stocktakeStatus;

    /**
     * 盘点备注
     */
    @Schema(description = "盘点备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
