package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.enums.NumberBooleanEnum;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppShareVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillTime.ActivitySeckillTimeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckill.ActivitySeckillMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon.ActivitySeckillCouponService;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activitySeckillCommodity.ActivitySeckillCommodityService;
import com.htyoudao.youdao.module.promotion.service.activitySeckillTime.ActivitySeckillTimeService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.util.ConvertUtil;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
public class ActivitySeckillServiceImpl implements ActivitySeckillService {

    @Resource
    private ActivitySeckillMapper activitySeckillMapper;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivitySeckillCommodityService activitySeckillCommodityService;

    @Resource
    private ActivitySeckillCouponService activitySeckillCouponService;

    @Resource
    private ActivitySeckillTimeService activitySeckillTimeService;

    @Resource
    private ActivityChannelService activityChannelService;

    @Resource
    private SeckillActivityCacheService seckillActivityCacheService;

    @Resource
    private SeckillStockCacheService stockCacheService;

    @DubboReference
    private CrowdApi crowdApi;

    @DubboReference
    private StoreApi storeApi;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    // 分享图片
    @Value("${activity.seckill.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.seckill.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.seckill.shareDescription}")
    private String shareDescription;


    /**
     * 创建秒杀活动主体
     * @param saveVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createActivitySeckill(ActivitySeckillReqSaveVO saveVO) {
        //新建活动主表
        ActivityDO activityDO = getActivityDO(saveVO);
        Long activityId = activityService.createActivity(activityDO);
        //新建秒杀活动拓展表
        createActivitySeckillDO(saveVO, activityId);
        //新建活动关联门店表
        createActivityStoreDO(saveVO, activityId);
        //新建活动关联商品表
        createActivitySeckillCommodityDO(saveVO, activityId);
        //新建活动关联优惠券表 1103
        createActivitySeckillCouponDO(saveVO.getCouponSaveReqVOS(), activityId);
        //新建活动关联场次表
        createActivitySeckillTimesDO(saveVO, activityId);

        //新建默认的两个短链
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.SEC_KILL.getCode());

        //更新缓存
        ActivitySeckillRespVO activitySeckillRespVO = selectInfo(activityId);

        seckillActivityCacheService.cacheActivity(activitySeckillRespVO);
    }

    private void createActivitySeckillCouponDO(List<ActivitySeckillCouponSaveReqVO> couponSaveReqVOS, Long activityId) {
        if (CollectionUtils.isEmpty(couponSaveReqVOS)){
            return;
        }
        couponSaveReqVOS.forEach(c -> c.setId(null));
        List<ActivitySeckillCouponDO> seckillCouponDOS = new ArrayList<>();
        for (ActivitySeckillCouponSaveReqVO couponSaveReqVO : couponSaveReqVOS) {
            ActivitySeckillCouponDO couponDO = new ActivitySeckillCouponDO();
            BeanUtils.copyProperties(couponSaveReqVO, couponDO);
            couponDO.setActivityId(activityId);
            seckillCouponDOS.add(couponDO);

        }
        activitySeckillCouponService.createBatch(seckillCouponDOS);
    }


    /**
     * 修改秒杀活动主体
     * @param activitySeckillReqSaveVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivitySeckill(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        //修改活动主表
        ActivityDO activityDO = getActivityDO(activitySeckillReqSaveVO);
        activityService.updateActivity(activityDO);
        //修改秒杀活动拓展表
        updateActivitySeckillDO(activitySeckillReqSaveVO);
        //修改秒杀关联门店表
        updateActivityStoreDO(activitySeckillReqSaveVO);

        //修改活动关联商品表
        updateActivitySeckillCommodityDO(activitySeckillReqSaveVO);
        //修改活动关联优惠券表 1103
        updateActivitySeckillCouponDO(activitySeckillReqSaveVO);
        //修改活动关联场次表
        updateActivitySeckillTimesDO(activitySeckillReqSaveVO);

        //更新缓存
        ActivitySeckillRespVO activitySeckillRespVO = selectInfo(activityDO.getId());
        seckillActivityCacheService.cacheActivity(activitySeckillRespVO);
    }

    private void updateActivitySeckillCouponDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        Long activityId = activitySeckillReqSaveVO.getId();
        List<ActivitySeckillCouponSaveReqVO> couponSaveReqVOS = activitySeckillReqSaveVO.getCouponSaveReqVOS();
        if (CollectionUtils.isEmpty(couponSaveReqVOS)){
            return;
        }
        List<ActivitySeckillCouponDO> couponDOS = new ArrayList<>();
        for (ActivitySeckillCouponSaveReqVO couponSaveReqVO : couponSaveReqVOS) {
            ActivitySeckillCouponDO couponDO = new ActivitySeckillCouponDO();
            BeanUtils.copyProperties(couponSaveReqVO, couponDO);
            couponDO.setActivityId(activityId);
            couponDOS.add(couponDO);
        }
        activitySeckillCouponService.updateBatch(couponDOS);
    }

    /**
     * 查询秒杀活动主体
     * @param id
     * @return
     */
    @Override
    public ActivitySeckillRespVO selectInfo(Long id) {
        ActivitySeckillRespVO activitySeckillRespVO    = new ActivitySeckillRespVO();

        //查询活动主表
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO == null){
            throw new ServiceException(ErrorCodeConstants.SECKILL_NOT_FOUND);
        }
        //将主表信息嵌入返回对象
        convertActivitySeckillRespVOWithActivityDO(activitySeckillRespVO,activityDO);
        //将拓展表信息嵌入返回对象
        convertActivitySeckillRespVOWithThisDO(activitySeckillRespVO);
        //将门店信息嵌入返回对象
        coverActivitySeckillRespVOWithStore(activitySeckillRespVO);
        //将活动商品信息嵌入返回对象
        convertActivitySeckillRespVOWithCommodityDO(activitySeckillRespVO);
        //将活动优惠券信息嵌入返回对象
        convertActivitySeckillRespVOWithCouponDO(activitySeckillRespVO);
        //将活动场次信息嵌入返回对象
        convertActivitySeckillRespVOWithTimesDO(activitySeckillRespVO);
        //将推广渠道嵌入返回对象
        convertActivitySeckillRespVOWithChannelDO(activitySeckillRespVO);

        return activitySeckillRespVO;
    }

    private void convertActivitySeckillRespVOWithCouponDO(ActivitySeckillRespVO activitySeckillRespVO) {

        Long activityId = activitySeckillRespVO.getId();

        List<ActivitySeckillCouponDO> seckillCouponDOS = activitySeckillCouponService.selectByActivityId(activityId);
        List<ActivitySeckillCouponRespVO> list = new ArrayList<>();

        seckillCouponDOS.forEach(doObj -> {
            ActivitySeckillCouponRespVO voObj = new ActivitySeckillCouponRespVO();
            BeanUtils.copyProperties(doObj, voObj);
            if (doObj.getCouponId() != null){
                GoodCouponDO goodCouponDO = goodCouponMapper.selectById(doObj.getCouponId());
                if (goodCouponDO != null) {
                    voObj.setCoupon(goodCouponDO);
                    voObj.setCouponName(goodCouponDO.getCouponName());
                    list.add(voObj);
                }else {
                    log.error("coupon not found:{}", doObj.getCouponId());
                }
            }
        });

        activitySeckillRespVO.setActivitySeckillCouponRespVOS(list);
    }

    private void coverActivitySeckillRespVOWithStore(ActivitySeckillRespVO activitySeckillRespVO) {
        List<ActivityStoreDO> activityStoreDOS = activityStoreService.selectByActivityId(activitySeckillRespVO.getId());
        List<Long> storeIds = activityStoreDOS.stream().map(ActivityStoreDO::getStoreId).toList();
        List<ActivitySeckillStoreNameRespVO>   activitySeckillStoreNameRespVOS = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(storeIds)) {
            activitySeckillRespVO.setStoreIds(storeIds);
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
            if (ObjectUtil.isNotEmpty(storesByStoreIds.getData())) {
                for (StoreInfoDTO datum : storesByStoreIds.getData()) {
                    ActivitySeckillStoreNameRespVO nameRespVO = new ActivitySeckillStoreNameRespVO();
                    nameRespVO.setStoreName(datum.getStoreName());
                    nameRespVO.setStoreId(datum.getStoreId());
                    activitySeckillStoreNameRespVOS.add(nameRespVO);
                }
            }

        }
        activitySeckillRespVO.setStoreIds(storeIds);
        activitySeckillRespVO.setActivitySeckillStoreNameRespVOS(activitySeckillStoreNameRespVOS);

    }

    //将推广渠道嵌入返回对象
    private void convertActivitySeckillRespVOWithChannelDO(ActivitySeckillRespVO activitySeckillRespVO) {
        Long activityId = activitySeckillRespVO.getId();
        List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityId(activityId);
        List<ActivityChannelRespVO>  activityChannelRespVOList = new ArrayList<>();
        for (ActivityChannelDO activityChannelDO : activityChannelDOList) {
            ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
            BeanUtils.copyProperties(activityChannelDO,activityChannelRespVO);
            activityChannelRespVOList.add(activityChannelRespVO);
        }
        activitySeckillRespVO.setActivityChannelRespVOList(activityChannelRespVOList);
    }

    /**
     * 查询秒杀活动推广
     * @param id
     * @return
     */
    @Override
    public ActivitySeckillSpreadRespVO selectSpread(Long id) {
        ActivitySeckillSpreadRespVO activitySeckillSpreadRespVO = new ActivitySeckillSpreadRespVO();
        LambdaQueryWrapper<ActivitySeckillDO>   queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillDO::getActivityId, id);
        ActivitySeckillDO activitySeckillDO = activitySeckillMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(activitySeckillDO)) {
            throw new ServiceException(ErrorCodeConstants.JD_NOT_FOUND);
        }
        activitySeckillSpreadRespVO.setId(id);
        activitySeckillSpreadRespVO.setShareImageUrl(activitySeckillDO.getShareImageUrl());
        activitySeckillSpreadRespVO.setShareTitle(activitySeckillDO.getShareTitle());
        activitySeckillSpreadRespVO.setShareDescription(activitySeckillDO.getShareDescription());


        //将链接渠道嵌入
        //convertActivitySeckillSpreadRespVOWithChannel(activitySeckillSpreadRespVO);


        return activitySeckillSpreadRespVO;
    }

    /**
     * 删除秒杀活动
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivitySeckill(Long id) {
        //删除主表
         activityService.deleteActivity(id);
         //删除拓展表
        LambdaQueryWrapper<ActivitySeckillDO>   queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillDO::getActivityId, id);
        activitySeckillMapper.delete(queryWrapper);
        //删除门店表
        activityStoreService.deleteByActivityId(id);
        //删除商品表
        activitySeckillCommodityService.deleteByActivityId(id);
        //删除优惠券
        activitySeckillCouponService.deleteByActivityId(id);
        //删除场次表
        activitySeckillTimeService.deleteByActivityId(id);
        //删除推广渠道表
        activityChannelService.deleteByActivityId(id);
        //删除缓存
        seckillActivityCacheService.removeActivity(String.valueOf(id));
        //去掉库存缓存
        stockCacheService.deleteStockByActivityId(id);
    }

    /**
     * 修改推广页面
     * @param activitySeckillSpreadReqVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpread(ActivitySeckillSpreadSaveReqVO activitySeckillSpreadReqVO) {
         LambdaUpdateWrapper<ActivitySeckillDO> updateWrapper = new LambdaUpdateWrapper<>();
         updateWrapper.eq(ActivitySeckillDO::getActivityId,activitySeckillSpreadReqVO.getId());
         updateWrapper.set(ActivitySeckillDO::getShareImageUrl,activitySeckillSpreadReqVO.getShareImageUrl());
         updateWrapper.set(ActivitySeckillDO::getShareTitle,activitySeckillSpreadReqVO.getShareTitle());
         updateWrapper.set(ActivitySeckillDO::getShareDescription,activitySeckillSpreadReqVO.getShareDescription());
         activitySeckillMapper.update(updateWrapper);


         //新建并修改推广链接
         //activityChannelService.createAndUpdateChannel(activitySeckillSpreadReqVO.getActivityChannelList(),activitySeckillSpreadReqVO.getId(),ActivityChannelTypeEnum.SEC_KILL.getCode());

        //更新缓存
        ActivitySeckillRespVO activitySeckillRespVO = selectInfo(activitySeckillSpreadReqVO.getId());

        seckillActivityCacheService.cacheActivity(activitySeckillRespVO);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(ActivitySeckillEnabledUpdateReqVO activitySeckillEnabledUpdateReqVO) {
        Long activityId = activitySeckillEnabledUpdateReqVO.getId();
        Integer isEnabled = activitySeckillEnabledUpdateReqVO.getIsEnabled();

        activityService.updateStatus(activityId,isEnabled);

        ActivitySeckillRespVO activitySeckillRespVO = selectInfo(activityId);
        seckillActivityCacheService.cacheActivity(activitySeckillRespVO);
    }

    @Override
    public ActivitySeckillAppShareVO getShareVO(Long activityId) {
        ActivitySeckillAppShareVO appShareVO = new ActivitySeckillAppShareVO();
        LambdaQueryWrapper<ActivitySeckillDO>   queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillDO::getActivityId, activityId);
        ActivitySeckillDO activitySeckillDO = activitySeckillMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(activitySeckillDO,appShareVO);
        return appShareVO;
    }

    /**
     * 将链接渠道嵌入
     * @param activitySeckillSpreadRespVO
     */
    /*private void convertActivitySeckillSpreadRespVOWithChannel(ActivitySeckillSpreadRespVO activitySeckillSpreadRespVO) {
       // List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityId(activitySeckillSpreadRespVO.getId());
        List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityIdWithIsEnable(activitySeckillSpreadRespVO.getId());

        List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();

        activityChannelDOList.forEach(activityChannelDO -> {
            ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
            BeanUtils.copyProperties(activityChannelDO, activityChannelRespVO);
            activityChannelRespVOS.add(activityChannelRespVO);
        });
        activitySeckillSpreadRespVO.setActivityChannelRespVOS(activityChannelRespVOS);
    }*/

    /**
     * 将场次嵌入
     * @param activitySeckillRespVO
     */
    private void convertActivitySeckillRespVOWithTimesDO(ActivitySeckillRespVO activitySeckillRespVO) {
        Long activityId = activitySeckillRespVO.getId();

        List<ActivitySeckillTimeDO> activitySeckillTimeDOList = activitySeckillTimeService.selectByActivityId(activityId);
        List<ActivitySeckillTimeRespVO> activitySeckillTimeRespVOS = new ArrayList<>();
        activitySeckillTimeDOList.forEach(activitySeckillTimeDO -> {
            ActivitySeckillTimeRespVO  activitySeckillTimeRespVO = new ActivitySeckillTimeRespVO();
            BeanUtils.copyProperties(activitySeckillTimeDO,activitySeckillTimeRespVO);
            activitySeckillTimeRespVOS.add(activitySeckillTimeRespVO);
        });
        activitySeckillRespVO.setActivitySeckillTimeRespVOS(activitySeckillTimeRespVOS);

    }

    /**
     * 将商品信息嵌入
     * @param activitySeckillRespVO
     */
    private void convertActivitySeckillRespVOWithCommodityDO(ActivitySeckillRespVO activitySeckillRespVO) {
        Long activityId = activitySeckillRespVO.getId();

       List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS =  activitySeckillCommodityService.selectByActivityId(activityId);
        List<ActivitySeckillCommodityRespVO> activitySeckillCommodityRespVOS = new ArrayList<>();

        activitySeckillCommodityDOS.forEach(doObj -> {
            ActivitySeckillCommodityRespVO voObj = new ActivitySeckillCommodityRespVO();
            BeanUtils.copyProperties(doObj, voObj);
            activitySeckillCommodityRespVOS.add(voObj);
        });



        activitySeckillRespVO.setActivitySeckillCommodityRespVOS(activitySeckillCommodityRespVOS);

    }

    /**
     * 将扩展表信息嵌入
     * @param activitySeckillRespVO
     */
    private void convertActivitySeckillRespVOWithThisDO(ActivitySeckillRespVO activitySeckillRespVO) {
        Long activityId = activitySeckillRespVO.getId();
        LambdaQueryWrapper<ActivitySeckillDO> activitySeckillQueryWrapper = new LambdaQueryWrapper<>();
        activitySeckillQueryWrapper.eq(ActivitySeckillDO::getActivityId, activityId);
        ActivitySeckillDO activitySeckillDO = activitySeckillMapper.selectOne(activitySeckillQueryWrapper);

        BeanUtils.copyProperties(activitySeckillDO,activitySeckillRespVO);
        //将 id 复原
        activitySeckillRespVO.setId(activityId);
    }

    /**
     * 活动主表信息嵌入反回对象
     * @param activityDO
     */

    private void convertActivitySeckillRespVOWithActivityDO(ActivitySeckillRespVO activitySeckillRespVO, ActivityDO activityDO) {
        BeanUtils.copyProperties(activityDO,activitySeckillRespVO);
        //将人群参数加入
        if (activityDO.getSelectedGroups() != null) {
            activitySeckillRespVO.setSelectedGroupList(ConvertUtil.convertStringToList(activityDO.getSelectedGroups()));
            List<Long> longs = ConvertUtil.convertStringToList(activityDO.getSelectedGroups());
            List<ActivitySeckillCrowdRespVO>  activitySeckillCrowdRespVOS = new ArrayList<>();

            List<CrowdNameDTO> byIds = crowdApi.getByIds(longs);
            if (ObjectUtil.isNotEmpty(byIds)) {
                for (CrowdNameDTO byId : byIds) {
                    ActivitySeckillCrowdRespVO activitySeckillCrowdRespVO = new ActivitySeckillCrowdRespVO();
                    BeanUtils.copyProperties(byId, activitySeckillCrowdRespVO);
                    activitySeckillCrowdRespVOS.add(activitySeckillCrowdRespVO);
                }
            }
            activitySeckillRespVO.setActivitySeckillCrowdRespVOList(activitySeckillCrowdRespVOS);



        }
        //可叠加活动
        if (activityDO.getStackableActivities() != null) {
            activitySeckillRespVO.setStackableActivitieList(ConvertUtil.convertStringToInList(activityDO.getStackableActivities()));
        }
        //周集合
        if (activityDO.getWeekNumbers() != null) {
            activitySeckillRespVO.setWeekNumberList(ConvertUtil.convertStringToInList(activityDO.getWeekNumbers()));
        }
        //日集合
        if (activityDO.getDayNumbers() != null) {
            activitySeckillRespVO.setDayNumberList(ConvertUtil.convertStringToInList(activityDO.getDayNumbers()));
        }

    }



    /**
     * 修改活动场次信息
     * @param activitySeckillReqSaveVO
     */
    private void updateActivitySeckillTimesDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        Long activityId = activitySeckillReqSaveVO.getId();
        List<ActivitySeckillTimeSaveReqVO> activitySeckillTimeSaveReqVOList = activitySeckillReqSaveVO.getActivitySeckillTimeSaveReqVOList();
        List<ActivitySeckillTimeDO> activitySeckillTimeDOS = new ArrayList<>();
        for (ActivitySeckillTimeSaveReqVO activitySeckillTimeSaveReqVO : activitySeckillTimeSaveReqVOList) {
            ActivitySeckillTimeDO activitySeckillTimeDO = new ActivitySeckillTimeDO();
            BeanUtils.copyProperties(activitySeckillTimeSaveReqVO, activitySeckillTimeDO);
            activitySeckillTimeDO.setActivityId(activityId);
            activitySeckillTimeDOS.add(activitySeckillTimeDO);
        }
        activitySeckillTimeService.updateBatch(activitySeckillTimeDOS);
    }

    /**
     * 修改活动商品信息
     * @param activitySeckillReqSaveVO
     */
    private void updateActivitySeckillCommodityDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        Long activityId = activitySeckillReqSaveVO.getId();
        List<ActivitySeckillCommoditySaveReqVO> commoditySaveReqVOList = activitySeckillReqSaveVO.getActivitySeckillCommoditySaveReqVOList();
        if (CollectionUtils.isEmpty(commoditySaveReqVOList)){
            return;
        }
        List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS = new ArrayList<>();
        for (ActivitySeckillCommoditySaveReqVO activitySeckillCommoditySaveReqVO : commoditySaveReqVOList) {
            ActivitySeckillCommodityDO activitySeckillCommodityDO = new ActivitySeckillCommodityDO();
            BeanUtils.copyProperties(activitySeckillCommoditySaveReqVO, activitySeckillCommodityDO);
            activitySeckillCommodityDO.setActivityId(activityId);
            activitySeckillCommodityDOS.add(activitySeckillCommodityDO);

        }
        activitySeckillCommodityService.updateBatch(activitySeckillCommodityDOS);

    }

    /**
     * 修改活动门店信息
     * @param activitySeckillReqSaveVO
     */
    private void updateActivityStoreDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {

        activityStoreService.deleteByActivityId(activitySeckillReqSaveVO.getId());


        if (ObjectUtil.isNotEmpty(activitySeckillReqSaveVO.getStoreIds()) && activitySeckillReqSaveVO.getActivityStore()== NumberBooleanEnum.FALSE.getNumberValue()){
            List<Long> storeIds = activitySeckillReqSaveVO.getStoreIds();
            Long activityId = activitySeckillReqSaveVO.getId();
            activityStoreService.createBatch(storeIds, activityId);
        }
    }

    /**
     * 新建活动关联门店表
     * @param activitySeckillReqSaveVO
     * @param activityId
     */
    private void createActivityStoreDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO, Long activityId) {
        if (ObjectUtil.isNotEmpty(activitySeckillReqSaveVO.getStoreIds()) && activitySeckillReqSaveVO.getActivityStore()== NumberBooleanEnum.FALSE.getNumberValue()){
            List<Long> storeIds = activitySeckillReqSaveVO.getStoreIds();
            activityStoreService.createBatch(storeIds, activityId);
        }
    }

    /**
     * 新建活动关联场次表
     * @param activitySeckillReqSaveVO
     * @param activityId
     */
    private void createActivitySeckillTimesDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO, Long activityId) {
        List<ActivitySeckillTimeSaveReqVO> activitySeckillTimeSaveReqVOList = activitySeckillReqSaveVO.getActivitySeckillTimeSaveReqVOList();
        activitySeckillTimeSaveReqVOList.forEach(c->c.setId(null));
        List<ActivitySeckillTimeDO> activitySeckillTimeDOS = new ArrayList<>();
        for (ActivitySeckillTimeSaveReqVO activitySeckillTimeSaveReqVO : activitySeckillTimeSaveReqVOList) {
            ActivitySeckillTimeDO activitySeckillTimeDO = new ActivitySeckillTimeDO();
            BeanUtils.copyProperties(activitySeckillTimeSaveReqVO, activitySeckillTimeDO);
            activitySeckillTimeDO.setActivityId(activityId);
            activitySeckillTimeDOS.add(activitySeckillTimeDO);
        }
        activitySeckillTimeService.createBatch(activitySeckillTimeDOS);
    }

    /**
     * 新建活动关联商品表
     * @param activitySeckillReqSaveVO
     * @param activityId
     */
    private void createActivitySeckillCommodityDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO, Long activityId) {
        List<ActivitySeckillCommoditySaveReqVO> commoditySaveReqVOList = activitySeckillReqSaveVO.getActivitySeckillCommoditySaveReqVOList();
        if (CollectionUtils.isEmpty(commoditySaveReqVOList)){
            return;
        }
        commoditySaveReqVOList.forEach(c->c.setId(null));
        List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS = new ArrayList<>();
        for (ActivitySeckillCommoditySaveReqVO activitySeckillCommoditySaveReqVO : commoditySaveReqVOList) {
            ActivitySeckillCommodityDO activitySeckillCommodityDO = new ActivitySeckillCommodityDO();
            BeanUtils.copyProperties(activitySeckillCommoditySaveReqVO, activitySeckillCommodityDO);
            activitySeckillCommodityDO.setActivityId(activityId);
            activitySeckillCommodityDOS.add(activitySeckillCommodityDO);

        }
        activitySeckillCommodityService.createBatch(activitySeckillCommodityDOS);
    }

    /**
     * 新建秒杀活动拓展表
     * @param activitySeckillReqSaveVO
     * @param activityId
     */
    private void createActivitySeckillDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO, Long activityId) {
        ActivitySeckillDO activitySeckillDO = new ActivitySeckillDO();
        BeanUtils.copyProperties(activitySeckillReqSaveVO, activitySeckillDO);
        activitySeckillDO.setActivityId(activityId);
        activitySeckillDO.setShareDescription(shareDescription);
        activitySeckillDO.setShareTitle(shareTitle);
        activitySeckillDO.setShareImageUrl(shareImageUrl);
        activitySeckillMapper.insert(activitySeckillDO);
    }

    /**
     * 创建这个活动主表对象
     * @param activitySeckillReqSaveVO
     * @return
     */
     private ActivityDO getActivityDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(activitySeckillReqSaveVO, activityDO);
        if (activitySeckillReqSaveVO.getId() != null) {
            activityDO.setId(activitySeckillReqSaveVO.getId());
        }
        //选择人群（存储人群ID，逗号分割）
        if (ObjectUtil.isNotEmpty(activitySeckillReqSaveVO.getSelectedGroupList())) {
            List<Long> selectedGroupList = activitySeckillReqSaveVO.getSelectedGroupList();
            activityDO.setSelectedGroups(ConvertUtil.convertListToString(selectedGroupList));
        }
        //可叠加的活动
        if (ObjectUtil.isNotEmpty(activitySeckillReqSaveVO.getStackableActivitieList())){
            List<Integer> stackableActivitieList = activitySeckillReqSaveVO.getStackableActivitieList();
            activityDO.setStackableActivities(ConvertUtil.convertInListToString(stackableActivitieList));
        }

        activityDO.setWeekNumbers(ConvertUtil.convertInListToString(activitySeckillReqSaveVO.getWeekNumberList()));
        activityDO.setDayNumbers(ConvertUtil.convertInListToString(activitySeckillReqSaveVO.getDayNumberList()));
        activityDO.setActivityType(ActivityTypeEnum.SEC_KILL.getCode());

        boolean type = CollectionUtils.isEmpty(activitySeckillReqSaveVO.getActivitySeckillCommoditySaveReqVOList());
        activityDO.setDiscountType(type ? 1 : 2 );
        return activityDO;
    }

    /**
     * 修改秒杀活动拓展表
     * @param activitySeckillReqSaveVO
     */

    private void updateActivitySeckillDO(ActivitySeckillReqSaveVO activitySeckillReqSaveVO) {
        LambdaUpdateWrapper<ActivitySeckillDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(ActivitySeckillDO::getActivityId, activitySeckillReqSaveVO.getId());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getImageUrl, activitySeckillReqSaveVO.getImageUrl());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getBackgroundColor, activitySeckillReqSaveVO.getBackgroundColor());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getIsStoreLimit, activitySeckillReqSaveVO.getIsStoreLimit());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getStoreLimitCount, activitySeckillReqSaveVO.getStoreLimitCount());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getShareSetting, activitySeckillReqSaveVO.getShareSetting());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getIsBarrageShow, activitySeckillReqSaveVO.getIsBarrageShow());
        lambdaUpdateWrapper.set(ActivitySeckillDO::getCommunityFlag, activitySeckillReqSaveVO.getCommunityFlag());
        activitySeckillMapper.update(lambdaUpdateWrapper);


    }
}
