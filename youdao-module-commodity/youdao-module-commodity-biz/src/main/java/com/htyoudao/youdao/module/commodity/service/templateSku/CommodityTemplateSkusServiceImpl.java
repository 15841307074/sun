package com.htyoudao.youdao.module.commodity.service.templateSku;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateSkusMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 模板商品 SKU 表 服务实现类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
@Service
public class CommodityTemplateSkusServiceImpl  implements ICommodityTemplateSkusService {


    @Resource
    private CommodityTemplateSkusMapper commodityTemplateSkusMapper;

    @Override
    public void saveBatch(List<CommodityTemplateSkus> addCommodityTemplateSkuList) {
        commodityTemplateSkusMapper.insertBatch(addCommodityTemplateSkuList);
    }

    @Override
    public List<CommodityTemplateSkus> selectByTemplateSpuIds(List<Long> templateCommodityIds) {
        LambdaQueryWrapper<CommodityTemplateSkus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityTemplateSkus::getCommodityTemplateId, templateCommodityIds);

        return commodityTemplateSkusMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByTemplateSpuIds(List<Long> commodityTemplateIds) {
        commodityTemplateSkusMapper.delete(new LambdaQueryWrapper<CommodityTemplateSkus>().in(CommodityTemplateSkus::getCommodityTemplateId, commodityTemplateIds));
    }

    @Override
    public void updateBatch(List<CommodityTemplateSkus> commodityTemplateSkus) {
        commodityTemplateSkusMapper.updateBatch(commodityTemplateSkus);
    }

    @Override
    public List<CommodityTemplateSkus> selectByTemplateId(Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateSkus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityTemplateSkus::getTemplateId, commodityTemplateId);
        return commodityTemplateSkusMapper.selectList(queryWrapper);
    }
}
