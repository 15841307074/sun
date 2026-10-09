package com.htyoudao.youdao.module.commodity.api;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.DTO.*;
import com.htyoudao.youdao.module.commodity.api.VO.CommodityActivityVO;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-10
 */
@Tag(name = "RPC 服务 - 商品")
public interface CommodityApi {

    @Operation(summary = "根据skuID集合查询商品信息")
    CommonResult<List<StoreSkuInfoDTO>> getStoreSkuList(@RequestParam Set<Long> stoerSkuIds);

    @Operation(summary = "根据门店和连锁商品ID集合查询门店SKU信息")
    CommonResult<List<StoreSkuInfoDTO>> getStoreSkuListByCommodityIds(@RequestParam Long storeId,
                                                                       @RequestParam Set<Long> commodityIds);

    @Operation(summary = "根据afterID集合查询加购商品信息")
    CommonResult<List<AfterInfoDTO>> getAfterList(@RequestParam Set<Long> afterIds, @RequestParam Long storeId);

    @Operation(summary = "查询加购商品列表")
    CommonResult<List<AfterOrderSimpleDTO>> getAfterOrderSimpleList();

    @Operation(summary = "根据singleID集合查询商品信息")
    CommonResult<List<StoreSingleInfoDTO>> getStoreSingleList(@RequestParam Set<Long> storeSingleIds);

    @Operation(summary = "根据commodityID集合查询连锁商品库商品信息")
    CommonResult<List<CommodityDTO>> getCommodityList(@RequestParam List<Long> commodityIds);

    @Operation(summary = "获取商品隐藏状态过滤条件")
    CommonResult<CommodityHiddenFilterDTO> filterHiddenCommodityIds();

    /**
     * 兑换券 兑换商品
     *
     * @param commodityId commodityId
     * @param storeId     storeId
     * @return CommonResult<ItemDto>
     */
    CommonResult<ItemDto> getCouponIsUsed(Long commodityId, Long storeId);


    @Operation(summary = "根据商品原始id集合查询在售门店数量")
    CommonResult<List<StoreSpuCountDTO>> saleStoreCountByCommodityIds(@RequestParam Set<Long> commodityIds, Set<Long> storeIds);


    @Operation(summary = "根据活动ID 场次，获取商品信息")
    CommonResult<List<SeckillSpuDTO>> seckillCommodityByActivityId(Long storeId, Long activityId, Integer times);

    /**
     * 获取商品活动列表
     *
     * @param body body
     * @return CommonResult<List < CommodityActivityDTO>>
     */
    CommonResult<List<CommodityActivityDTO>> getCommodityActivityList(CommodityActivityVO body);

    /**
     * 把商品拆分成原材料
     *
     * @return CommonResult<List>
     */
    @Operation(summary = "把商品拆分成原材料")
    CommonResult<List<MaterialListRespDTO>> modifyCommodity(@RequestBody CommoditySplitMaterialReqVO commoditySplitMaterialReqVO);

    /**
     * 扣减/增加 库存接口
     */
    @Operation(summary = "扣减/增加 库存接口")
    Boolean changeStock(StockChangeVO param);


    /**
     * 退款返还 库存接口
     */
    @Operation(summary = "退款返还 库存接口")
    Boolean cancelReturnStock(String orderSn);
}
