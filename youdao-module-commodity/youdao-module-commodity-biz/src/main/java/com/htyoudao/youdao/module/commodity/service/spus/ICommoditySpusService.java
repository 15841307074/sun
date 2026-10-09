package com.htyoudao.youdao.module.commodity.service.spus;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryBatchHiddenReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommoditySkuListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryDelReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryUpdateReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CommodityCateDateRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.shelfLock.CommoditySpuLockReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommodityGroupSingleRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleDownReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleUpReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.validation.Valid;

import java.util.List;


/**
 * 商品Service接口
 *
 * @author Qizhongann
 * @date 2024-01-16
 */
public interface ICommoditySpusService extends IService<CommoditySpus>
{


    void deleteCategoryV3(@Valid CategoryDelReqVo delReqVo);

    void createSpuV3(@Valid CommoditySpusSaveReqVo saveReqVo);

    void updateSpuV3(@Valid CommoditySpusUpdateReqVo updateReqVo);

    CommodityHiddenFilterRespVO filterHiddenCommodityIds();

    List<CategoryRespVo> getCategoryListV3(@Valid CommodityTypeReqVo typeReqVo);

    List<CommodityCateDateRespVo> getSpuListV3(@Valid CommoditySpusQueryReqVo queryReqVo);

    CommoditySpusByIdRespVo getSpuByIdV3(Long id);

    void deleteSpuByIdV3(Long id);

    List<CategoryRespVo> fuzzySearchCommodityV3(CommoditySpusSearchReqVo searchReqVo);

    List<CommoditySpus> batchModificationV3(@Valid CommodityBatchEditReqVo batchEditReqVo);

    List<CommodityCountRespVO> getSpuCountV3();


    void sortSpuV3(@Valid List<CommoditySpusSortReqVo> spusSortReqVos);

    void updateNameV3(@Valid CommodityUNameReqVo uNameReqVo);

    void updateCategoryV3(@Valid CategoryUpdateReqVo updateReqVo);

    void batchUpdateCategoryHidden(@Valid CategoryBatchHiddenReqVO reqVO);

    List<CommoditySpusByIdRespVo> listForCommodityV3(Long commodityId);

    List<CategoryRespVo>  selectDownSpuV3(Integer isDown);



    List<CommoditySpusByIdRespVo> getOffShelfProductsV3(CommoditySingleDownReqVo singleDownReqVo);

    void singleDownV3(@Valid CommoditySingleDownReqVo singleDownReqVo);

    void singleUpV3(@Valid CommoditySingleUpReqVo singleUpReqVo);

    List<CommodityGroupSingleRespVo> packageSelectV3(@Valid SetMealSingleUpReqVo singleUpReqVo);

    void packageUpAndDownV3(@Valid SetMealUpAndDownReqVo upAndDownReqVo);

    void checkStatusV3();

    /**
     * 根据商品id集合查询商品信息
     *
     * @param commodityIds commodityIds
     * @return List
     */
    List<CommoditySpus> getSpursByCommodityIds(List<Long> commodityIds);

    Boolean selectIsExistTag(Long id);

    List<CommodityDTO> getSpuDTOByCommodityIds(List<Long> commodityIds);

    PageResult<CommodityCouponSpuVO> couponSpuList(CommodityCouponSpuReqVO queryReqVo);

    void updateCommoditySkuFlag(Long commodityId, int skuFlag);

    PageResult<RecipeCommodityRespVO> selectRecipeCommodity(RecipeCommodityReqVO recipeCommodityReqVO);

    PageResult<StoreInfoDTO> getCommodityStoreList(RecipeCommodityStoreReqVO recipeCommodityStoreReqVO);

    /**
     * 单品上架查询，需要查出来联动上架的套餐 ID
     * @param singleUpReqVo
     * @return
     */
    SingleUpRespVO singleUpSelect(@Valid CommoditySingleUpReqVo singleUpReqVo);

    void singleUpConfirm(@Valid SingleUpConfirmReqVO singleUpReqVo);


    BatchDownSelectRespVO batchDownSelect(@Valid BatchDownSelectReqVO batchDownSelectReqVO);

    void batchDownConfirm(@Valid BatchDownSelectReqVO batchDownSelectReqVO);

    PageResult<RecipeCommoditySkuListRespVO> selectCommodityRecipeList(RecipeCommodityReqVO recipeCommodityReqVO);

    BatchUpSelectRespVO batchUpSelect(@Valid BatchUpSelectReqVO batchUpSelectReqVO);

    void batchUpConfirm(@Valid BatchUpSelectReqVO batchUpSelectReqVO);

    void updateShelfLock(CommoditySpuLockReqVO reqVO);
}
