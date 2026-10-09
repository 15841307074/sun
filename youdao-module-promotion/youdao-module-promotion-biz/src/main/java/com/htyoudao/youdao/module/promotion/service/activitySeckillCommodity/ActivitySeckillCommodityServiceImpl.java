package com.htyoudao.youdao.module.promotion.service.activitySeckillCommodity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckillCommodity.ActivitySeckillCommodityMapper;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillStockCacheService;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class ActivitySeckillCommodityServiceImpl implements ActivitySeckillCommodityService{

    @Resource
    private  ActivitySeckillCommodityMapper activitySeckillCommodityMapper;

    @Resource
    private SeckillStockCacheService stockCacheService;

    @Override
    public void createBatch(List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS) {
        activitySeckillCommodityMapper.insertBatch(activitySeckillCommodityDOS);
    }

    @Override
    public void updateBatch(List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS) {
        if (CollectionUtils.isEmpty(activitySeckillCommodityDOS)){
            return;
        }

        Long activityId = activitySeckillCommodityDOS.get(0).getActivityId();

        //若活动库存修改后 去掉对应的商品库存缓存， 重新设置
        resetCache(activityId, activitySeckillCommodityDOS);

        List<ActivitySeckillCommodityDO> insertList = new ArrayList<>();
       List<ActivitySeckillCommodityDO> updateList = new ArrayList<>();
       List<Long> currentIds = new ArrayList<>();
        for (ActivitySeckillCommodityDO activitySeckillCommodityDO : activitySeckillCommodityDOS) {
            if (activitySeckillCommodityDO.getId() == null) {
                insertList.add(activitySeckillCommodityDO);
            }else {
                currentIds.add(activitySeckillCommodityDO.getId());
                LambdaQueryWrapper<ActivitySeckillCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ActivitySeckillCommodityDO::getId, activitySeckillCommodityDO.getId());
                if (activitySeckillCommodityMapper.exists(queryWrapper)) {
                    updateList.add(activitySeckillCommodityDO);
                }else {
                    activitySeckillCommodityDO.setId(null);
                    insertList.add(activitySeckillCommodityDO);
                }
            }
        }





        LambdaQueryWrapper<ActivitySeckillCommodityDO> deleteWrapper = new LambdaQueryWrapper<>();
        if (!currentIds.isEmpty()) {
            deleteWrapper.notIn(ActivitySeckillCommodityDO::getId, currentIds);
        }

        deleteWrapper.eq(ActivitySeckillCommodityDO::getActivityId,activityId);
        activitySeckillCommodityMapper.delete(deleteWrapper);
        if (!insertList.isEmpty()) {
            activitySeckillCommodityMapper.insertBatch(insertList);
        }
        if (!updateList.isEmpty()) {
            activitySeckillCommodityMapper.updateBatch(updateList);
        }

    }

    private void resetCache(Long activityId, List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS) {

        List<ActivitySeckillCommodityDO> oldCommodityList = activitySeckillCommodityMapper.selectList(
            ActivitySeckillCommodityDO::getActivityId, activityId);

        if (CollectionUtils.isEmpty(oldCommodityList)){
            return;
        }

        Map<Long, Integer> oldAcStockMap = oldCommodityList.stream()
            .collect(Collectors.toMap(ActivitySeckillCommodityDO::getCommodityId,
                ActivitySeckillCommodityDO::getActivityStock));

        for (ActivitySeckillCommodityDO activitySeckillCommodityDO : activitySeckillCommodityDOS) {
            Long commodityId = activitySeckillCommodityDO.getCommodityId();
            //，若活动库存修改后 去掉对应的商品库存缓存， 重新设置
            if (!Objects.equals(oldAcStockMap.get(commodityId), activitySeckillCommodityDO.getActivityStock())){
                stockCacheService.deleteStockCache(activityId, commodityId);
            }
        }
    }

    @Override
    public List<ActivitySeckillCommodityDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivitySeckillCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillCommodityDO::getActivityId, activityId);


        return activitySeckillCommodityMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivitySeckillCommodityDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillCommodityDO::getActivityId, id);
        activitySeckillCommodityMapper.delete(queryWrapper);
    }

    @Override
    public void updateCommodityInfo(ActivityCommodityDTO activityCommodityDTO) {
        LambdaUpdateWrapper<ActivitySeckillCommodityDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivitySeckillCommodityDO::getCommodityId, activityCommodityDTO.getCommodityId());
        updateWrapper.set(ActivitySeckillCommodityDO::getCommodityName, activityCommodityDTO.getCommodityName());
        activitySeckillCommodityMapper.update(updateWrapper);

        List<ActivityCommodityDTO.ActivityCommodityPriceDTO> commodityPriceList = activityCommodityDTO.getCommodityPriceList();

        for (ActivityCommodityDTO.ActivityCommodityPriceDTO activityCommodityPriceDTO : commodityPriceList) {
            LambdaUpdateWrapper<ActivitySeckillCommodityDO> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(ActivitySeckillCommodityDO::getSkuId, activityCommodityPriceDTO.getSkuId());
            updateWrapper2.set(ActivitySeckillCommodityDO::getCommodityPrice, activityCommodityPriceDTO.getCommodityPrice());
            activitySeckillCommodityMapper.update(updateWrapper2);
        }

    }
}
