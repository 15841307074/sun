package com.htyoudao.youdao.module.promotion.service.activityLotteryCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLotteryCommodity.ActivityLotteryCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnzCommodity.ActivityNjnzCommodityMapper;
import com.htyoudao.youdao.module.promotion.service.activityNjnzCommodity.ActivityNjnzCommodityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RefreshScope
public class ActivityLotteryCommodityServiceImpl extends
    ServiceImpl<ActivityLotteryCommodityMapper, ActivityLotteryCommodityDO> implements ActivityLotteryCommodityService {

    @Resource
    private ActivityLotteryCommodityMapper activityLotteryCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityLotteryCommodityDO> activityNjnzCommodityDOS = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityLotteryCommodityDO activityLotteryCommodityDO = new ActivityLotteryCommodityDO();
            activityLotteryCommodityDO.setCommodityId(commodityId);
            activityLotteryCommodityDO.setActivityId(activityId);
            activityNjnzCommodityDOS.add(activityLotteryCommodityDO);
        }
        this.saveBatch(activityNjnzCommodityDOS);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityLotteryCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityLotteryCommodityDO::getActivityId, activityId);
        activityLotteryCommodityMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityLotteryCommodityDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityLotteryCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityLotteryCommodityDO::getActivityId, id);
        return activityLotteryCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listByActivityId(Long id) {
        List<ActivityLotteryCommodityDO> commodityDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream().map(mm -> mm.getCommodityId())
            .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
