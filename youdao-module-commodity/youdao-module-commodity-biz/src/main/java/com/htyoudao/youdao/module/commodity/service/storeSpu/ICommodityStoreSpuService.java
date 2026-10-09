package com.htyoudao.youdao.module.commodity.service.storeSpu;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.DTO.ItemDto;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSpuCountDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.TemplatePriceReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.TemplatePriceUpdatePriceVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.enums.ClientType;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncCategoryDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICommodityStoreSpuService  {




    void deleteCommodityTemplateByCommodityTemplateIdNew(Long commodityTemplateId);

    List<TemplateSpusRespVO> selectCommodityTemplateListNew(Long templateId, Long templateCategoryId);

    void deleteCommodityCategoryByIds(Long templateCategoryId);

    void deleteCommodityTemplatePriceByCommodityIds(@Valid TemplateDelComReqVO comReqVO);

    void updateTemplatePriceV3(@Valid TemplatePriceUpdatePriceVO templatePriceVO);

    void deleteSpuByStoreId(Long storeId);

    void deleteAllStoreSpu();

    void deleteStoreCategory(Long commodityStoreCategoryId);

    void updateStoreCategory(@Valid StoreCategoryUpdateReqVO updateReqVO);

    void deleteStoreSpus( List<Long> spuIds);

    List<CommodityStoreSpuRespVo> storeSingleDownSelectV3(@Valid StoreItemDelistReqVo delistReqVo);

    void singleDownV3(@Valid StoreItemDelistReqVo delistReqVo);


    void singleUpV3(@Valid StoreItemListingReqVo listingReqVo);

    List<CommodityStoreSingleRespVO> packageSelectV3(@Valid StoreItemListingReqVo listingReqVo);

    void packageUpAndDownV3(@Valid StoreItemUnmountReqVo unmountReqVo);



    CommodityStoreRespVo  getSpusInfoV3(@Valid CommodityStoreSpuDetailReqVO detailReqVO);

    void updateCommodityPremiseV3(@Valid CommodityStoreUpdateReqVo updateReqVo);

    List<CommodityStoreRespVo> getAllSpuV3(@Valid CommodityStoreAllReqVO allReqVO);

    void doSync(Long storeId, List<CommoditySyncCategoryDTO> syncData);

    void sortSpu(@Valid List<CommodityStoreSortSpuReqVO> sortReqVOS);



    List<StoreCategoryDTO> getAllSpuFromDB(Long storeId, ClientType clientType);

    void resetStoreCache(Long storeId);

    List<StoreCategoryDTO> appletGetAllSpu(Long storeId, ClientType clientType);

    List<StoreCategoryDTO> appletGetAllSpu(Long storeId, ClientType clientType, Boolean njnz);

    Boolean selectIsExistTag(Long id);

    void appletUpdateSpu(@Valid DCSpuUpdateReqVO reqVO);

    /**
     * 兑换券兑换商品
     * @param commodityId commodityId
     * @param storeId storeId
     * @return ItemDto
     */
    ItemDto getCouponIsUsed(Long commodityId, Long storeId);

    List<StoreSpuCountDTO> saleStoreCountByCommodityIds(Set<Long> commodityIds, Set<Long> storeIds);


    List<CommodityStoreRespVo> getByStoreSpuList(CommodityStoreAllReqVO allReqVO);

    List<StoreSingleSimpleRespVO> getStoreSingle(@Valid StoreSingleSimpleReqVO storeSingleSimpleReqVO);

    PageResult<StoreInfoDTO> getStoreListByCommodityId(RecipeCommodityStoreReqVO recipeCommodityStoreReqVO);


    StoreSingleUpRespVO singleUpSelect(@Valid StoreSingleUpReqVO singleUpReqVo);

    void singleUpConfirm(@Valid StoreSingleUpConfirmReqVO singleUpConfirmReqVO);

    StoreBatchDownSelectRespVO batchDownSelect(@Valid StoreBatchDownSelectReqVO storeBatchDownSelectReqVO);

    void batchDownConfirm(@Valid StoreBatchDownSelectReqVO storeBatchDownSelectReqVO);

    void doSingleDown(Long storeId);

    StoreBatchUpSelectRespVO batchUpSelect(@Valid StoreBatchUpSelectReqVO storeBatchUpSelectReqVO);

    void batchUpConfirm(@Valid StoreBatchUpSelectReqVO storeBatchUpSelectReqVO);

}
