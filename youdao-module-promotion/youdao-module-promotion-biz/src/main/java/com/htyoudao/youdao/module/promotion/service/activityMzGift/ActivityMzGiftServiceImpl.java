package com.htyoudao.youdao.module.promotion.service.activityMzGift;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 满赠活动赠送商品 Service 实现
 */
@Service
@Slf4j
@RefreshScope
public class ActivityMzGiftServiceImpl extends ServiceImpl<ActivityMzGiftMapper, ActivityMzGiftDO> implements ActivityMzGiftService {

    @Resource
    private ActivityMzGiftMapper activityMzGiftMapper;

    @Resource
    private CommodityApi commodityApi;

    @Override
    public void createBatch(List<ActivityMzGiftDO> giftList, Long activityId) {
        if (CollectionUtils.isEmpty(giftList)) {
            return;
        }
        for (ActivityMzGiftDO gift : giftList) {
            gift.setId(null); // 清空id，由雪花算法重新生成，避免复用DO对象时主键冲突
            gift.setActivityId(activityId);
        }
        this.saveBatch(giftList);
    }

    @Override
    public void createBatchWithStoreId(List<ActivityMzGiftDO> giftList, Long activityId, Long storeId) {
        if (CollectionUtils.isEmpty(giftList)) {
            return;
        }
        for (ActivityMzGiftDO gift : giftList) {
            gift.setId(null); // 清空id，由雪花算法重新生成，避免多门店循环复用时主键冲突
            gift.setActivityId(activityId);
            gift.setStoreId(storeId);
        }
        this.saveBatch(giftList);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityMzGiftDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzGiftDO::getActivityId, activityId);
        activityMzGiftMapper.delete(queryWrapper);
    }

    @Override
    public void deleteByActivityIdAndStoreIds(Long activityId, List<Long> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) {
            return;
        }
        LambdaQueryWrapper<ActivityMzGiftDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzGiftDO::getActivityId, activityId);
        queryWrapper.in(ActivityMzGiftDO::getStoreId, storeIds);
        activityMzGiftMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityMzGiftDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityMzGiftDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzGiftDO::getActivityId, activityId);
        return activityMzGiftMapper.selectList(queryWrapper);
    }

    @Override
    public List<ActivityMzGiftDO> selectByActivityIdAndStoreId(Long activityId, Long storeId) {
        LambdaQueryWrapper<ActivityMzGiftDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzGiftDO::getActivityId, activityId);
        queryWrapper.eq(ActivityMzGiftDO::getStoreId, storeId);
        return activityMzGiftMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommodityDTO> listGiftCommodityByActivityId(Long activityId) {
        List<ActivityMzGiftDO> giftList = this.selectByActivityId(activityId);
        if (CollectionUtils.isEmpty(giftList)) {
            return List.of();
        }
        List<Long> commIds = giftList.stream()
                .map(ActivityMzGiftDO::getGiftCommodityId)
                .collect(Collectors.toList());
        CommonResult<List<CommodityDTO>> commodityList = commodityApi.getCommodityList(commIds);
        return commodityList.getData();
    }
}
