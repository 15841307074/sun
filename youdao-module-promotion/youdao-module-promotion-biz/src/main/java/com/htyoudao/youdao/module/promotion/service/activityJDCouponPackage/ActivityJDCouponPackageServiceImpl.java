package com.htyoudao.youdao.module.promotion.service.activityJDCouponPackage;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJDCouponPackage.ActivityJDCouponPackageMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityJDCouponPackageServiceImpl implements ActivityJDCouponPackageService {

    @Resource
    private ActivityJDCouponPackageMapper activityJDCouponPackageMapper;


    @Override
    public void createBatch(List<ActivityJDCouponPackageDO> activityJDCouponPackageDOS) {
        activityJDCouponPackageMapper.insertBatch(activityJDCouponPackageDOS);
    }

    @Override
    public void updateBatch(List<ActivityJDCouponPackageDO> couponDOS) {
        /*if (CollectionUtils.isEmpty(couponDOS)){
            return;
        }
        Long activityId = couponDOS.get(0).getActivityId();

        //若活动库存修改后 去掉对应的商品库存缓存， 重新设置
        resetCache(activityId, couponDOS);

        List<ActivitySeckillCouponDO> insertList = new ArrayList<>();
        List<ActivitySeckillCouponDO> updateList = new ArrayList<>();
        List<Long> currentIds = new ArrayList<>();
        for (ActivitySeckillCouponDO couponDO : couponDOS) {
            if (couponDO.getId() == null) {
                insertList.add(couponDO);
            }else {
                currentIds.add(couponDO.getId());
                LambdaQueryWrapper<ActivitySeckillCouponDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ActivitySeckillCouponDO::getId, couponDO.getId());
                if (couponMapper.exists(queryWrapper)) {
                    updateList.add(couponDO);
                }else {
                    couponDO.setId(null);
                    insertList.add(couponDO);
                }
            }
        }



        LambdaQueryWrapper<ActivitySeckillCouponDO> deleteWrapper = new LambdaQueryWrapper<>();
        if (!currentIds.isEmpty()) {
            deleteWrapper.notIn(ActivitySeckillCouponDO::getId, currentIds);
        }

        deleteWrapper.eq(ActivitySeckillCouponDO::getActivityId,activityId);
        couponMapper.delete(deleteWrapper);
        if (!insertList.isEmpty()) {
            couponMapper.insertBatch(insertList);
        }
        if (!updateList.isEmpty()) {
            couponMapper.updateBatch(updateList);
        }*/
    }

    @Override
    public List<ActivityJDCouponPackageDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityJDCouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDCouponPackageDO::getActivityId, activityId);
        return activityJDCouponPackageMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivityJDCouponPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDCouponPackageDO::getActivityId, id);
        activityJDCouponPackageMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityJDCouponPackageDO> selectActivityByCouponId(Long id) {
        //获取与优惠券包有关的集点活动
        LambdaQueryWrapper<ActivityJDCouponPackageDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ActivityJDCouponPackageDO::getId, id);

        return activityJDCouponPackageMapper.selectList(lambdaQueryWrapper);
    }
}
