package com.htyoudao.youdao.module.promotion.service.activityMzCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzCommodity.ActivityMzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzCommodity.ActivityMzCommodityMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 满赠活动关联商品 Service 实现
 */
@Service
@Slf4j
@RefreshScope
public class ActivityMzCommodityServiceImpl extends ServiceImpl<ActivityMzCommodityMapper, ActivityMzCommodityDO> implements ActivityMzCommodityService {

    @Resource
    private ActivityMzCommodityMapper activityMzCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityMzCommodityDO> list = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityMzCommodityDO commodityDO = new ActivityMzCommodityDO();
            commodityDO.setCommodityId(commodityId);
            commodityDO.setActivityId(activityId);
            list.add(commodityDO);
        }
        this.saveBatch(list);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityMzCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzCommodityDO::getActivityId, activityId);
        activityMzCommodityMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityMzCommodityDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityMzCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzCommodityDO::getActivityId, activityId);
        return activityMzCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listByActivityId(Long activityId) {
        List<ActivityMzCommodityDO> commodityDOS = this.selectByActivityId(activityId);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream()
                .map(ActivityMzCommodityDO::getCommodityId)
                .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
