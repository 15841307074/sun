package com.htyoudao.youdao.module.commodity.service.category;


import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryIdReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySaveReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySortReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 商品分类Service接口
 *
 * @author Qizhongnan
 * @date 2024-01-10
 */
public interface ICommodityCategoryService {


    List<CategoryRespVo> getCommodityCategoryList();

    Long createCategoryV3(@Valid CategorySaveReqVo saveReqVo);

    CategoryRespVo getCategoryInfo(@Valid CategoryIdReqVo idReqVo);

    CommodityCategory getById(Long id);

    void sortCategory(@Valid List<CategorySortReqVo> categorySortReqVoList);

    void deleteById(Long id);

    List<CommodityCategory> selectListByAsc();

    List<CommodityCategory> selectAllListByAsc();

    List<CommodityCategory> selectListByIdsAndAsc(List<Long> categoryIds);

    void updateById(CommodityCategory commodityCategory);

    void updateHiddenByIds(List<Long> categoryIds, Integer isHidden);
}
