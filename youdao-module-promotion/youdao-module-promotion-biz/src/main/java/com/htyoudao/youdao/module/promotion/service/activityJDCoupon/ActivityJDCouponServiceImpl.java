package com.htyoudao.youdao.module.promotion.service.activityJDCoupon;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillTimeRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJDCoupon.ActivityJDCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckill.ActivitySeckillCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.ActivitySeckillService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillActivityCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillCouponStockCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.StockPreheatService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityJDCouponServiceImpl implements ActivityJDCouponService {

    @Resource
    private ActivityJDCouponMapper activityJDCouponMapper;


    @Override
    public void createBatch(List<ActivityJDCouponDO> activityJDCommodityDOS) {
        activityJDCouponMapper.insertBatch(activityJDCommodityDOS);
    }

    @Override
    public void updateBatch(List<ActivityJDCouponDO> couponDOS) {
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
    public List<ActivityJDCouponDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityJDCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDCouponDO::getActivityId, activityId);
        return activityJDCouponMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivityJDCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDCouponDO::getActivityId, id);
        activityJDCouponMapper.delete(queryWrapper);
    }

    @Override
    public List<ActivityJDCouponDO> selectActivityByCouponId(Long id) {
        //获取与优惠券有关的集点活动
        LambdaQueryWrapper<ActivityJDCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ActivityJDCouponDO::getId, id);

        return activityJDCouponMapper.selectList(lambdaQueryWrapper);
    }
}
