package com.htyoudao.youdao.module.commodity.api.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;


@Data
public class StoreSpuCountDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -953518147169052388L;

    /**
     * 商品连锁库ID
     */
    private Long commodityId;

    /**
     * 在售门店数量
     */
    private Long storeCount;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片URL")
    private String goodsImage;

    private Integer isSingle;
}
