package com.htyoudao.youdao.module.commodity.service.templateCategory;

import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortSpuReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;

import jakarta.validation.Valid;
import java.util.List;

/**
 * <p>
 * 模板商品分类 服务类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
public interface ICommodityTemplateCategoryService  {


    List<CommodityTemplateCategory> selectByTemplateId(Long commodityTemplateId);

    void saveBatch(List<CommodityTemplateCategory> addList);

    void deleteById(Long templateCategoryId);

    void sortCategory(List<CommodityTemplateSortReqVO> sortReqVOS);


}
