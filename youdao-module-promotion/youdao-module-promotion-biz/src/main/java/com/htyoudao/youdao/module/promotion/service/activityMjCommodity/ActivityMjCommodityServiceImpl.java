package com.htyoudao.youdao.module.promotion.service.activityMjCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMjCommodity.ActivityMjCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnzCommodity.ActivityNjnzCommodityMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yangqinglin
 */
@Service
@Slf4j
@RefreshScope
public class ActivityMjCommodityServiceImpl extends
    ServiceImpl<ActivityMjCommodityMapper, ActivityMjCommodityDO> implements ActivityMjCommodityService {

    @Resource
    private ActivityMjCommodityMapper activityMjCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityMjCommodityDO> activityMjCommodityDOS = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityMjCommodityDO activityMjCommodityDO = new ActivityMjCommodityDO();
            activityMjCommodityDO.setCommodityId(commodityId);
            activityMjCommodityDO.setActivityId(activityId);
            activityMjCommodityDOS.add(activityMjCommodityDO);
        }
        this.saveBatch(activityMjCommodityDOS);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityMjCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMjCommodityDO::getActivityId, activityId);
        activityMjCommodityMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityMjCommodityDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityMjCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMjCommodityDO::getActivityId, id);
        return activityMjCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listByActivityId(Long id) {
        List<ActivityMjCommodityDO> commodityDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream().map(mm -> mm.getCommodityId())
            .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
