package com.htyoudao.youdao.module.promotion.service.activityNjnzCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnzCommodity.ActivityNjnzCommodityMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author villky
 */
@Service
@Slf4j
@RefreshScope
public class ActivityNjnzCommodityServiceImpl extends
    ServiceImpl<ActivityNjnzCommodityMapper, ActivityNjnzCommodityDO> implements ActivityNjnzCommodityService {

    @Resource
    private ActivityNjnzCommodityMapper activityNjnzCommodityMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<Long> commodityIds, Long activityId) {
        List<ActivityNjnzCommodityDO> activityNjnzCommodityDOS = new ArrayList<>();
        for (Long commodityId : commodityIds) {
            ActivityNjnzCommodityDO activityNjnzCommodityDO = new ActivityNjnzCommodityDO();
            activityNjnzCommodityDO.setCommodityId(commodityId);
            activityNjnzCommodityDO.setActivityId(activityId);
            activityNjnzCommodityDOS.add(activityNjnzCommodityDO);
        }
        this.saveBatch(activityNjnzCommodityDOS);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityNjnzCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityNjnzCommodityDO::getActivityId, activityId);
        activityNjnzCommodityMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityNjnzCommodityDO> selectByActivityId(Long id) {
        LambdaQueryWrapper<ActivityNjnzCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityNjnzCommodityDO::getActivityId, id);
        return activityNjnzCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listByActivityId(Long id) {
        List<ActivityNjnzCommodityDO> commodityDOS = this.selectByActivityId(id);
        if (CollectionUtils.isEmpty(commodityDOS)) {
            return List.of();
        }
        List<Long> commIds = commodityDOS.stream().map(mm -> mm.getCommodityId())
            .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
