package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.shelfLock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommoditySpuLockReqVO {
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;

    /**
     * 上架锁定 1锁定 0未锁定
     */
    @Schema(description = "上架锁定 1锁定 0未锁定")
    @NotNull(message = "上架锁定")
    private Integer shelfLock;

}