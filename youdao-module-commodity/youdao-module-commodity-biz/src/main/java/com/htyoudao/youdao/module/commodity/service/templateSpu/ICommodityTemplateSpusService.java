package com.htyoudao.youdao.module.commodity.service.templateSpu;

import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortSpuReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import java.util.List;

/**
 * <p>
 * 模板商品表 服务类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
public interface ICommodityTemplateSpusService  {

    void saveBatch(List<CommodityTemplateSpus> addCommodityTemplateSpusList);

    List<CommodityTemplateSpus> selectByTemplateCategoryId(Long templateCategorId);

    List<CommodityTemplateSpus> selectByTemplateCategoryIds(List<Long> templateCategorIds);

    void deleteByIds(List<Long> commodityTemplateIds);

    List<CommodityTemplateSpus> selectByTemplateId(Long commodityTemplateId);

    void sortSpu(@Valid List<CommodityTemplateSortSpuReqVO> sortReqVOS);

    List<CommodityTemplateSpus> selectByCommodityIds(List<Long> commodityIds, Long commodityTemplateId);




    void changeSpuNameAndImage(@NotNull(message = "商品ID不能为空") Long commodityId, String commodityName, List<String> imageUrlVO);

    void changeSpuName(@NotNull(message = "商品ID不能为空") Long commodityId, @NotEmpty(message = "商品名称不能为空") String commodityName);

    TemplateCommodityDetailRespVO getTemplateSpuInfo(@Valid TemplateCommodityDetailReqVO detailReqVO);

    void updateTemplateCommodity(@Valid TemplateCommodityUpdateReqVO updateReqVO);
}
