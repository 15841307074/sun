package com.htyoudao.youdao.module.commodity.service.storeCategory;


import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CategoryStoreSortVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CommodityStoreCategoryRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.StoreCategoryUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreCategoryUpReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 门店下商品分类表 服务类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
public interface ICommodityStoreCategoryService  {

    void deleteSpuByStoreId(Long storeId);

    void deleteAllStoreSpu();

    CommodityStoreCategory getCategoryById(Long storeCategoryId);

    void deleteById(Long commodityStoreCategoryId);

    void checkUniqueRequiredCategory(StoreCategoryUpdateReqVO updateReqVO);

    void updateStoreCategory(@Valid CommodityStoreCategoryUpReqVO upReqVO);

    Long synchronizeSingle(CommodityStoreCategory category);

    List<CommodityStoreCategoryRespVO> getList(Long storeId);

    CommodityStoreCategory selectById(Long commodityStoreCategoryId);

    void sortCategory(@Valid List<CategoryStoreSortVO> categorySortDTOList);

    List<CommodityStoreCategoryRespVO> getListTwo(Long storeId);
}
