package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "app - 商品 小料 VO")
public class CondimentDto implements Serializable {

    @Schema(description = "小料ID")
    private Long condimentId;

    @Schema(description = "小料名称")
    private String condimentName;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "价格")
    private BigDecimal condimentPrice;

    @Schema(description = "状态 1启用 0禁用")
    private Integer status;

    @Schema(description = "数量")
    private Integer number;

}
