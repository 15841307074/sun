package com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "DC -点餐机修改商品分组 Request VO")
@Data
public class DCGroupUpdateReqVO {

    @Schema(description = "DC -点餐机修改商品分组id")
    @NotNull(message = "商品分组id不能是空")
    private Long commodityStoreGroupId;
    /**
     * 同一商品是否可选多份 1 是 0否
     */
    @Schema(description = "同一商品是否可选多份 1 是 0否")
    @NotNull(message = "同一商品是否可选多份 1 是 0否不能是空")
    private Integer chooseMany;
}
