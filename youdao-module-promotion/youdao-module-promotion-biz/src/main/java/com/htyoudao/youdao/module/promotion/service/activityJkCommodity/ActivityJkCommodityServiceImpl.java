package com.htyoudao.youdao.module.promotion.service.activityJkCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkCommodity.ActivityJkCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMjCommodity.ActivityMjCommodityMapper;
import com.htyoudao.youdao.module.promotion.service.activityMjCommodity.ActivityMjCommodityService;
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
public class ActivityJkCommodityServiceImpl extends
    ServiceImpl<ActivityJkCommodityMapper, ActivityJkCommodityDO> implements ActivityJkCommodityService {

    @Resource
    private ActivityJkCommodityMapper activityJkCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityJkCommodityDO> activityjkCommodityDOS = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityJkCommodityDO activityJkCommodityDO = new ActivityJkCommodityDO();
            activityJkCommodityDO.setCommodityId(commodityId);
            activityJkCommodityDO.setActivityId(activityId);
            activityjkCommodityDOS.add(activityJkCommodityDO);
        }
        this.saveBatch(activityjkCommodityDOS);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityJkCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJkCommodityDO::getActivityId, activityId);
        activityJkCommodityMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityJkCommodityDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityJkCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJkCommodityDO::getActivityId, id);
        return activityJkCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listByActivityId(Long id) {
        List<ActivityJkCommodityDO> commodityDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream().map(mm -> mm.getCommodityId())
            .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
