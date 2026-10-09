package com.htyoudao.youdao.module.commodity.service.templateSku;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;

import java.util.List;


/**
 * <p>
 * 模板商品 SKU 表 服务类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
public interface ICommodityTemplateSkusService  {


    void saveBatch(List<CommodityTemplateSkus> addCommodityTemplateSkuList);

    List<CommodityTemplateSkus> selectByTemplateSpuIds(List<Long> templateCommodityIds);

    void deleteByTemplateSpuIds(List<Long> commodityTemplateIds);

    void updateBatch(List<CommodityTemplateSkus> commodityTemplateSkus);

    List<CommodityTemplateSkus> selectByTemplateId(Long commodityTemplateId);
}
