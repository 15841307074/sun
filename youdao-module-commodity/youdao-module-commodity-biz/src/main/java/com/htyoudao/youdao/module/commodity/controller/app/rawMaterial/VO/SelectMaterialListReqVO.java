package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "门店原材料查询")
public class SelectMaterialListReqVO {

    /**
     * 门店 ID
     */
    @Schema(description = "门店 ID")
    @NotNull(message = "门店 ID 不能为空")
    private Long storeId;

    /**
     * 商品统计名称
     */
    @Schema(description = "商品统计名称")
    private String stasticsCommodityName;

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
     * 停用状态(0停用 1启用)
     */
    @Schema(description = "停用状态(0停用 1启用)")
    private Integer isEnable;

    @Schema(description = "根据库存排序规则，true升序 false降序")
    private Boolean isAsc = true;
}
