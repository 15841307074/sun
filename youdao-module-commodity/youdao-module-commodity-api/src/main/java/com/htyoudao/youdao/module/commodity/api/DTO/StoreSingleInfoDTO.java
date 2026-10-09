package com.htyoudao.youdao.module.commodity.api.DTO;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 套餐子项
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-29
 */
@Data
public class StoreSingleInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8309534261722893549L;

    private Long singleId;

    /**
     * 门店SPU ID
     */
    private Long spuId;

    /**
     * 连锁库SKU ID
     */
    private Long originalSkuId;

    /**
     * 连锁库ID
     */
    private Long commodityId;

    /**
     * single 数量
     */
    private Integer qty;

    private String skuName;

    private String spuName;

    private String imageUrl;

    private BigDecimal singlePrice;

    private Integer storeStatus;

    private Integer wxStatus;

    private ErrorCode errorCode;

    private List<CommodityFlavorDTO> commodityFlavors;

    private List<FlavorInfoVO> flavors = new ArrayList<>();

    @Data
    public static class FlavorInfoVO {

        @Schema(description = "属性名", requiredMode = Schema.RequiredMode.REQUIRED, example = "辣度")
        private String name;

        @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "微辣")
        private String value;
    }

}
