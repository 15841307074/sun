package com.htyoudao.youdao.module.commodity.service.templateGroup;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateSetmealGroupMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author DELL
* @description 针对表【commodity_template_setmeal_group(套餐内容分组表)】的数据库操作Service实现
* @createDate 2025-02-07 11:25:42
*/
@Service
public class CommodityTemplateSetmealGroupServiceImpl implements ICommodityTemplateSetmealGroupService {

    @Resource
    private CommodityTemplateSetmealGroupMapper commodityTemplateSetmealGroupMapper;

    @Override
    public void saveBatch(List<CommodityTemplateSetmealGroup> addCommodityTemplateSetmealGroupList) {

        commodityTemplateSetmealGroupMapper.insertBatch(addCommodityTemplateSetmealGroupList);
    }

    @Override
    public void deleteByTemplateSpuIds(List<Long> commodityTemplateIds) {

        LambdaQueryWrapper<CommodityTemplateSetmealGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityTemplateSetmealGroup::getCommodityTemplateId, commodityTemplateIds);

        commodityTemplateSetmealGroupMapper.delete(queryWrapper);
    }

    @Override
    public List<CommodityTemplateSetmealGroup> selectByTemplateSpuIds(List<Long> templateCommodityIds) {
        LambdaQueryWrapper<CommodityTemplateSetmealGroup> templateGroupQueryWrapper = new LambdaQueryWrapper<>();
        templateGroupQueryWrapper.in(CommodityTemplateSetmealGroup::getCommodityTemplateId, templateCommodityIds);
        return commodityTemplateSetmealGroupMapper.selectList(templateGroupQueryWrapper);
    }

    @Override
    public List<CommodityTemplateSetmealGroup> selectByTemplateId(Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateSetmealGroup> templateGroupQueryWrapper = new LambdaQueryWrapper<>();
        templateGroupQueryWrapper.eq(CommodityTemplateSetmealGroup::getTemplateId, commodityTemplateId);
        return commodityTemplateSetmealGroupMapper.selectList(templateGroupQueryWrapper);
    }
}




