package com.htyoudao.youdao.module.commodity.service.group;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySetmealGroupMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author DELL
* @description 针对表【commodity_setmeal_group(套餐内容分组表)】的数据库操作Service实现
* @createDate 2025-02-06 11:00:47
*/
@Service
public class CommoditySetmealGroupServiceImpl     implements ICommoditySetmealGroupService {


    @Resource
    private CommoditySetmealGroupMapper commoditySetmealGroupMapper;

    @Override
    public void save(CommoditySetmealGroup commoditySetmealGroup) {
        commoditySetmealGroupMapper.insert(commoditySetmealGroup);
    }

    @Override
    public void deleteBySpuId(Long commodityId) {
        LambdaQueryWrapper<CommoditySetmealGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommoditySetmealGroup::getCommodityId, commodityId);
        commoditySetmealGroupMapper.delete(queryWrapper);
    }

    @Override
    public List<CommoditySetmealGroup> selectBySpuId(Long commodityId) {

        LambdaQueryWrapper<CommoditySetmealGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommoditySetmealGroup::getCommodityId, commodityId);

        return commoditySetmealGroupMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommoditySetmealGroup> selectBySpuIds(List<Long> commodityIds) {

        LambdaQueryWrapper<CommoditySetmealGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommoditySetmealGroup::getCommodityId, commodityIds);

        return commoditySetmealGroupMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteBySpuIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommoditySetmealGroup> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.in(CommoditySetmealGroup::getCommodityId, commodityIds);
        commoditySetmealGroupMapper.delete(groupLambdaQueryWrapper);
    }
}




