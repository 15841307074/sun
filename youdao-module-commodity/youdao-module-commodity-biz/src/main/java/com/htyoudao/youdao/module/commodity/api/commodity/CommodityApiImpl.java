package com.htyoudao.youdao.module.commodity.api.commodity;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.COMMODITY_BLOCK_ERROR;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.COMMODITY_FALLBACK_ERROR;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.*;
import com.htyoudao.youdao.module.commodity.api.VO.CommodityActivityVO;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.CommodityHiddenFilterRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialListRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialOwnDataRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.SeckillSpuVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO.ChangeInfo;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.service.activity.CommodityActivityService;
import com.htyoudao.youdao.module.commodity.service.activity.SeckillActivityService;
import com.htyoudao.youdao.module.commodity.service.afterorder.AfterOrderService;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialInventoryService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.service.storeSingle.ICommodityStoreSingleService;
import com.htyoudao.youdao.module.commodity.service.storeSku.ICommodityStoreSkuService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

@DubboService
//@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Slf4j
public class CommodityApiImpl implements CommodityApi {

    @Resource
    private ICommodityStoreSkuService commodityStoreSkuService;
    @Resource
    private ICommodityStoreSingleService commodityStoreSingleService;
    @Resource
    private AfterOrderService afterOrderService;
    @Resource
    private ICommoditySpusService commoditySpusService;
    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;
    @Resource
    private SeckillActivityService seckillActivityService;
    @Resource
    private CommodityActivityService commodityActivityService;
    @Resource
    private RawMaterialInventoryService rawMaterialInventoryService;
    @Resource
    private RowMaterialStockFlowService stockFlowService;

    @Override
    @SentinelResource(value = "getStoreSkuList", fallback = "getStoreSkuListFallback", blockHandler = "getStoreSkuListExceptionHandler")
    public CommonResult<List<StoreSkuInfoDTO>> getStoreSkuList(Set<Long> storeSkuIds) {
        return success(commodityStoreSkuService.selectSkuListForRpc(storeSkuIds));
    }

    @Override
    public CommonResult<List<StoreSkuInfoDTO>> getStoreSkuListByCommodityIds(Long storeId, Set<Long> commodityIds) {
        return success(commodityStoreSkuService.selectSkuListByCommodityIdsForRpc(storeId, commodityIds));
    }

    public CommonResult<List<StoreSkuInfoDTO>> getStoreSkuListFallback(Set<Long> storeSkuIds, Throwable ex) {
        log.error("CommodityApi.getStoreSkuListFallback", ex);
        throw exception(COMMODITY_FALLBACK_ERROR);
    }

    public CommonResult<List<StoreSkuInfoDTO>> getStoreSkuListExceptionHandler(Set<Long> storeSkuIds,
        BlockException ex) {
        log.error("CommodityApi.getStoreSkuListExceptionHandler", ex);
        throw exception(COMMODITY_BLOCK_ERROR);
    }

    @Override
    @SentinelResource(value = "getAfterList", fallback = "getAfterListFallback", blockHandler = "getAfterListExceptionHandler")
    public CommonResult<List<AfterInfoDTO>> getAfterList(Set<Long> afterIds, Long storeId) {
        return success(afterOrderService.selectAfterListForRpc(afterIds, storeId));
    }

    @Override
    @SentinelResource(value = "getAfterOrderSimpleList", fallback = "getAfterOrderSimpleListFallback", blockHandler = "getAfterOrderSimpleListExceptionHandler")
    public CommonResult<List<AfterOrderSimpleDTO>> getAfterOrderSimpleList() {
        return success(afterOrderService.selectAfterOrderSimpleListForRpc());
    }

    public CommonResult<List<AfterInfoDTO>> getAfterListFallback(Set<Long> afterIds, Long storeId, Throwable ex) {
        log.error("CommodityApi.getAfterListFallback", ex);
        throw exception(COMMODITY_FALLBACK_ERROR);
    }

    public CommonResult<List<AfterOrderSimpleDTO>> getAfterOrderSimpleListFallback(Throwable ex) {
        log.error("CommodityApi.getAfterOrderSimpleListFallback", ex);
        throw exception(COMMODITY_FALLBACK_ERROR);
    }

    public CommonResult<List<AfterInfoDTO>> getAfterListExceptionHandler(Set<Long> afterIds, Long storeId,
        BlockException ex) {
        log.error("CommodityApi.getAfterListExceptionHandler", ex);
        throw exception(COMMODITY_BLOCK_ERROR);
    }

    public CommonResult<List<AfterOrderSimpleDTO>> getAfterOrderSimpleListExceptionHandler(BlockException ex) {
        log.error("CommodityApi.getAfterOrderSimpleListExceptionHandler", ex);
        throw exception(COMMODITY_BLOCK_ERROR);
    }

    @Override
    @SentinelResource(value = "getStoreSingleList", fallback = "getStoreSingleListFallback", blockHandler = "getStoreSingleListExceptionHandler")
    public CommonResult<List<StoreSingleInfoDTO>> getStoreSingleList(Set<Long> storeSingleIds) {
        return success(commodityStoreSingleService.selectSingleListForRpc(storeSingleIds));
    }

    public CommonResult<List<StoreSingleInfoDTO>> getStoreSingleListFallback(Set<Long> storeSingleIds, Throwable ex) {
        log.error("CommodityApi.getStoreSingleListFallback", ex);
        throw exception(COMMODITY_FALLBACK_ERROR);
    }

    public CommonResult<List<StoreSingleInfoDTO>> getStoreSingleListExceptionHandler(Set<Long> storeSingleIds,
        BlockException ex) {
        log.error("CommodityApi.getStoreSingleListExceptionHandler", ex);
        throw exception(COMMODITY_BLOCK_ERROR);
    }

    @Override
    public CommonResult<List<CommodityDTO>> getCommodityList(List<Long> commodityIds) {
        return success(commoditySpusService.getSpuDTOByCommodityIds(commodityIds));
    }

    @Override
    public CommonResult<CommodityHiddenFilterDTO> filterHiddenCommodityIds() {
        CommodityHiddenFilterRespVO filter = commoditySpusService.filterHiddenCommodityIds();
        return success(new CommodityHiddenFilterDTO(filter.getCommodityIds(), filter.getIsHidden()));
    }

    @Override
    public CommonResult<ItemDto> getCouponIsUsed(Long commodityId, Long storeId) {
        return success(commodityStoreSpuService.getCouponIsUsed(commodityId, storeId));
    }

    @Override
    public CommonResult<List<StoreSpuCountDTO>> saleStoreCountByCommodityIds(Set<Long> commodityIds,
        Set<Long> storeIds) {
        List<CommoditySpus> list = commoditySpusService.list();

        List<StoreSpuCountDTO> storeSpuCountDTOS = commodityStoreSpuService.saleStoreCountByCommodityIds(commodityIds,
            storeIds);
        Map<Long, Long> collect = storeSpuCountDTOS.stream()
            .collect(Collectors.toMap(StoreSpuCountDTO::getCommodityId, StoreSpuCountDTO::getStoreCount));

        List<StoreSpuCountDTO> result = new ArrayList<>();
        for (CommoditySpus commoditySpus : list) {
            StoreSpuCountDTO storeSpuCountDTO = new StoreSpuCountDTO();
            storeSpuCountDTO.setCommodityId(commoditySpus.getCommodityId());
            storeSpuCountDTO.setStoreCount(collect.getOrDefault(commoditySpus.getCommodityId(), 0L));
            storeSpuCountDTO.setGoodsImage(commoditySpus.getImageUrl());
            storeSpuCountDTO.setGoodsName(commoditySpus.getCommodityName());
            storeSpuCountDTO.setIsSingle(commoditySpus.getIsSingle());
            result.add(storeSpuCountDTO);
        }

        return success(result);
    }

    @Override
    public CommonResult<List<SeckillSpuDTO>> seckillCommodityByActivityId(Long storeId, Long activityId,
        Integer times) {
        List<SeckillSpuVO> list = seckillActivityService.seckillList(storeId, activityId, times);

        if (CollectionUtils.isEmpty(list)){
            return CommonResult.success(List.of());
        }
        String jsonString = JSON.toJSONString(list);
        return CommonResult.success(JSON.parseArray(jsonString, SeckillSpuDTO.class));
    }

    @Override
    public CommonResult<List<CommodityActivityDTO>> getCommodityActivityList(CommodityActivityVO body) {
        return CommonResult.success(commodityActivityService.getCommodityActivityList(body));
    }

    @Override
    public CommonResult<List<MaterialListRespDTO>> modifyCommodity(CommoditySplitMaterialReqVO commoditySplitMaterialReqVO) {
        MaterialOwnDataRespVo materialListResp = rawMaterialInventoryService.splitCommodity(commoditySplitMaterialReqVO);
        String jsonString = JSON.toJSONString(materialListResp.getRespVoList());
        List<MaterialListRespDTO> listRespDTOS = JSON.parseArray(jsonString, MaterialListRespDTO.class);
        return CommonResult.success(listRespDTOS);
    }

    @Override
    public Boolean changeStock(StockChangeVO param) {
        String jsonString = JSON.toJSONString(param);
        StockChangeDTO stockChangeDTO = JSON.parseObject(jsonString, StockChangeDTO.class);
        return  stockFlowService.changeStock(stockChangeDTO);
    }

    @Override
    public Boolean cancelReturnStock(String orderSn) {

        List<RawMaterialStockFlow> flows = stockFlowService.flowByReferenceInfo(orderSn, StockChangeEnum.SALE);
        if (CollectionUtils.isEmpty(flows)){
            log.warn("订单:{}没查到销售扣除库存流水", orderSn);
            return true;
        }

        StockChangeDTO stockChangeDTO = new StockChangeDTO();
        stockChangeDTO.setType(StockChangeEnum.CANCEL_RETURN);
        stockChangeDTO.setReferenceId(orderSn);
        stockChangeDTO.setStoreId(flows.get(0).getStoreId());
        List<StockChangeDTO.ChangeInfo> changeInfos = new ArrayList<>();
        stockChangeDTO.setChangeList(changeInfos);
        for (RawMaterialStockFlow flow : flows) {
            StockChangeDTO.ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setQuantity(flow.getChangeQuantity().abs());
            changeInfo.setRawMaterialId(flow.getRawMaterialId());
            changeInfos.add(changeInfo);
        }
        return stockFlowService.changeStock(stockChangeDTO);
    }
}
