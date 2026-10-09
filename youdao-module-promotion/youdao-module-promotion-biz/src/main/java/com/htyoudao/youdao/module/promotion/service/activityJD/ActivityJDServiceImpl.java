package com.htyoudao.youdao.module.promotion.service.activityJD;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.*;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActPointRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJD.ActivityJDMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityJDCommodity.ActivityJDCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityJDCoupon.ActivityJDCouponService;
import com.htyoudao.youdao.module.promotion.service.activityJDCouponPackage.ActivityJDCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.activitySeckillTime.ActivitySeckillTimeService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.JD_INVENTORY_UPDATE_ERROR;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.JD_UPDATE_ERROR;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
public class ActivityJDServiceImpl implements ActivityJDService{

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityJDCommodityService activityJDCommodityService;

    @Resource
    private ActivityJDCouponService activityJDCouponService;

    @Resource
    private ActivityJDCouponPackageService activityJDCouponPackageService;

    @Resource
    private ActivitySeckillTimeService activitySeckillTimeService;

    @Resource
    private ActivityChannelService activityChannelService;

    @Resource
    private ActivityJDCacheService activityJDCacheService;
    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private BzOrderApi bzOrderApi;

    @Resource
    private ActivityJDMapper activityJDMapper;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private CouponPackageService couponPackageService;

    // 分享图片
    @Value("${activity.collect.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.collect.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.collect.shareDescription}")
    private String shareDescription;
    @Autowired
    private ActivityJDService activityJDService;

    /**
     * 创建集点活动主体
     * @param saveVO 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createActivityJD(ActivityJDReqSaveVO saveVO) {
        //新建活动主表
        ActivityDO activityDO = getActivityDO(saveVO);
        Long activityId = activityService.createActivity(activityDO);
        //新建集点活动拓展表 activity_jd
        createActivityJDDO(saveVO, activityId);
        //新建活动关联门店表 activity_store
        List<Long> activityStoreDO = createActivityStoreDO(saveVO, activityId);
        //新建活动关联商品表 activity_jd_commodity
        createActivityJDCommodityDO(saveVO, activityId);
        //新建活动兑换优惠券表 activity_jd_coupon
        createActivityJDCouponDO(saveVO.getCouponList(), activityId);
        //新建活动兑换优惠券包表 activity_jd_coupon_package
        createActivityJDCouponPackageDO(saveVO.getCouponPackageList(), activityId);


        //新建默认的两个短链
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.COLLECT.getCode());

        //获取活动缓存对象
        ActivityJDFullRespVO activityJDRespVO = selectFullInfo(activityId);
        //设置集点活动缓存
        activityJDCacheService.cacheActivity(activityJDRespVO, RedisKeyConstants.JD_ACTIVITY);
        //设置门店参与集点活动缓存
        setActivityStoreCache(saveVO, activityStoreDO, activityId);
        //设置优惠券缓存
        buildCoupon(saveVO.getCouponList());
        activityJDCacheService.cacheActivityCoupon(saveVO.getCouponList(), activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
        //设置优惠券包缓存
        buildCouponPackage(saveVO.getCouponPackageList());
        activityJDCacheService.cacheActivityCouponPackage(saveVO.getCouponPackageList(), activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
    }
    @Override
    public  void createUrl(Long businessId,String sortPath){
        //新建默认的两个短链
        activityChannelService.creatChannle(businessId, sortPath);

    }
    private void buildCouponPackage(List<ActivityJDCouponPackageReqSaveVO> couponPackageList) {
        for (ActivityJDCouponPackageReqSaveVO activityJDCouponPackageReqSaveVO : couponPackageList) {
            Long couponPackageId  = activityJDCouponPackageReqSaveVO.getId();
            CouponPackageRespVO couponPackageRespVO = couponPackageService.selectById(couponPackageId);
            activityJDCouponPackageReqSaveVO.setCouponPackageRespVO(couponPackageRespVO);
        }
    }

    private void buildCoupon(List<ActivityJDCouponReqSaveVO> couponList) {
        for (ActivityJDCouponReqSaveVO activityJDCouponReqSaveVO : couponList) {
            Long couponId  = activityJDCouponReqSaveVO.getId();
            GoodCouponRespVO coupon = goodCouponService.getCouponById(couponId);
            activityJDCouponReqSaveVO.setGoodCouponRespVO(coupon);
        }
    }


    @Override
    public ActivityJDSpreadRespVO selectSpread(Long id) {
        ActivityJDSpreadRespVO activityJDSpreadRespVO = new ActivityJDSpreadRespVO();
       LambdaQueryWrapper<ActivityJDDO> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.eq(ActivityJDDO::getActivityId, id);
        ActivityJDDO activityJDDO = activityJDMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(activityJDDO)) {
            throw new ServiceException(ErrorCodeConstants.JD_NOT_FOUND);
        }

        BeanUtils.copyProperties(activityJDDO, activityJDSpreadRespVO);

        //List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();
       // List<ActivityChannelDO> activityChannelDOS = activityChannelService.selectByActivityId(id);
        //List<ActivityChannelDO> activityChannelDOS = activityChannelService.selectByActivityIdWithIsEnable(id);
        /*if (!CollectionUtils.isEmpty(activityChannelDOS)) {
            for (ActivityChannelDO activityChannelDO : activityChannelDOS) {
                ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
                BeanUtils.copyProperties(activityChannelDO, activityChannelRespVO);
                activityChannelRespVOS.add(activityChannelRespVO);
            }
        }*/
        //activityJDSpreadRespVO.setActivityChannelRespVOS(activityChannelRespVOS);
        activityJDSpreadRespVO.setId(activityJDDO.getActivityId());
        return activityJDSpreadRespVO;
    }


    @Override
    public void updateSpread(ActivityJDSpreadSaveReqVO activityJDSpreadSaveReqVO) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        LambdaUpdateWrapper<ActivityJDDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityJDDO::getActivityId, activityJDSpreadSaveReqVO.getId());
        updateWrapper.set(ActivityJDDO::getShareImageUrl, activityJDSpreadSaveReqVO.getShareImageUrl());
        updateWrapper.set(ActivityJDDO::getShareTitle, activityJDSpreadSaveReqVO.getShareTitle());
        updateWrapper.set(ActivityJDDO::getShareDescription, activityJDSpreadSaveReqVO.getShareDescription());
        updateWrapper.set(ActivityJDDO::getUpdater, loginUserId);
        updateWrapper.set(ActivityJDDO::getUpdateTime,new Date());
        activityJDMapper.update(updateWrapper);

        //activityChannelService.createAndUpdateChannel(activityJDSpreadSaveReqVO.getActivityChannelList(),activityJDSpreadSaveReqVO.getId(),ActivityChannelTypeEnum.COLLECT.getCode());

        //更新缓存
        ActivityJDFullRespVO activityJDFullRespVO = selectFullInfo(activityJDSpreadSaveReqVO.getId());
        activityJDCacheService.cacheActivity(activityJDFullRespVO, RedisKeyConstants.JD_ACTIVITY);

    }

    @Override
    public ActivityCollectAppShareVO getShareVO(Long activityId) {

        LambdaQueryWrapper<ActivityJDDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDDO::getActivityId, activityId);
        ActivityJDDO activityJDDO = activityJDMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(activityJDDO)) {
            throw new ServiceException(ErrorCodeConstants.JD_NOT_FOUND);
        }
        ActivityCollectAppShareVO activityCollectAppShareVO = new ActivityCollectAppShareVO();
        BeanUtils.copyProperties(activityJDDO, activityCollectAppShareVO);
        return activityCollectAppShareVO;
    }

    private void setActivityStoreCache(ActivityJDReqSaveVO saveVO, List<Long> activityStoreDO, Long activityId) {
        //部分门店
        if (ObjectUtil.isNotEmpty(saveVO.getStoreIds()) && saveVO.getActivityStore() == 2 && activityStoreDO != null) {
            activityJDCacheService.cacheStoreActivity(activityStoreDO, activityId, RedisKeyConstants.JD_ACTIVITY_STORE);
            return;
        }
        //全部门店
        if (saveVO.getActivityStore() == 1 ) {
            CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
            if (allStoreList.isSuccess() && ObjectUtil.isNotEmpty(allStoreList.getData())) {
                List<StoreInfoDTO> checkedData = allStoreList.getCheckedData();
                activityStoreDO = checkedData.stream()
                        .map(StoreInfoDTO::getStoreId)
                        .toList();
                activityJDCacheService.cacheStoreActivity(activityStoreDO, activityId, RedisKeyConstants.JD_ACTIVITY_STORE);
            }
        }
    }

    /**
     * 创建这个活动主表对象
     * @param activityJDReqSaveVO 参数
     * @return ActivityDO
     */
    private ActivityDO getActivityDO(ActivityJDReqSaveVO activityJDReqSaveVO) {
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(activityJDReqSaveVO, activityDO);
        if (activityJDReqSaveVO.getId() != null) {
            activityDO.setId(activityJDReqSaveVO.getId());
        }
        activityDO.setActivityType(ActivityTypeEnum.JD.getCode());

        return activityDO;
    }



    private void createActivityJDCouponDO(List<ActivityJDCouponReqSaveVO> couponSaveReqVOS, Long activityId) {
        if (CollectionUtils.isEmpty(couponSaveReqVOS)){
            return;
        }
        couponSaveReqVOS.forEach(c -> c.setGoodsId(null));
        List<ActivityJDCouponDO> jdCouponDOS = new ArrayList<>();
        for (ActivityJDCouponReqSaveVO couponSaveReqVO : couponSaveReqVOS) {
            ActivityJDCouponDO couponDO = new ActivityJDCouponDO();
            BeanUtils.copyProperties(couponSaveReqVO, couponDO);
            couponDO.setActivityId(activityId);
            jdCouponDOS.add(couponDO);

        }
        activityJDCouponService.createBatch(jdCouponDOS);
    }

    private void createActivityJDCouponPackageDO(List<ActivityJDCouponPackageReqSaveVO> couponPackageList, Long activityId) {

        if (CollectionUtils.isEmpty(couponPackageList)){
            return;
        }
        couponPackageList.forEach(c -> c.setGoodsId(null));
        List<ActivityJDCouponPackageDO> activityJDCouponPackageDOS = new ArrayList<>();
        for (ActivityJDCouponPackageReqSaveVO couponPackageSaveReqVO : couponPackageList) {
            ActivityJDCouponPackageDO couponPackageDO = new ActivityJDCouponPackageDO();
            BeanUtils.copyProperties(couponPackageSaveReqVO, couponPackageDO);
            couponPackageDO.setActivityId(activityId);
            activityJDCouponPackageDOS.add(couponPackageDO);

        }
        activityJDCouponPackageService.createBatch(activityJDCouponPackageDOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityJD(ActivityJDReqSaveVO activityJDReqSaveVO) {

        //获取修改前集点活动信息
        ActivityJDFullRespVO activityJDFullRespVO = activityJDService.selectFullInfo(activityJDReqSaveVO.getId());
        //上架活动不允许修改
        Integer isEnabled = activityJDFullRespVO.getIsEnabled();
        if (isEnabled != null && isEnabled == 1) {
            throw exception(JD_UPDATE_ERROR);
        }

        //判断库存是否发生修改
        //优惠券判断
        List<ActivityJDCouponReqSaveVO> couponListUpdate = activityJDReqSaveVO.getCouponList();
        List<ActivityJDCouponCompareVO> couponAfter = BeanUtils.toBean(couponListUpdate, ActivityJDCouponCompareVO.class);
        List<ActivityJDCouponRespSaveVO> couponListBefore = activityJDFullRespVO.getCouponList();
        List<ActivityJDCouponCompareVO> couponBefore = BeanUtils.toBean(couponListBefore, ActivityJDCouponCompareVO.class);
        boolean couponBN = isContentEqualWithDuplicatesCoupon(couponAfter, couponBefore);
        //优惠券包判断
        List<ActivityJDCouponPackageReqSaveVO> couponPackageListUpdate = activityJDReqSaveVO.getCouponPackageList();
        List<ActivityJDCouponPackageCompareVO> couponPackageAfter = BeanUtils.toBean(couponPackageListUpdate, ActivityJDCouponPackageCompareVO.class);
        List<ActivityJDCouponPackageRespSaveVO> couponPackageListBefore = activityJDFullRespVO.getCouponPackageList();
        List<ActivityJDCouponPackageCompareVO> couponPackageBefore = BeanUtils.toBean(couponPackageListBefore, ActivityJDCouponPackageCompareVO.class);
        boolean couponPackageBN = isContentEqualWithDuplicatesCouponPackage(couponPackageAfter, couponPackageBefore);
        if (!couponBN) {
            //优惠券发生修改
            //库存修改是否合理
            for (ActivityJDCouponCompareVO activityJDCouponCompareVO : couponAfter){
                Integer inventory = activityJDCouponCompareVO.getInventory();
                Integer collectCount = activityJDCacheService.getCacheInventory(activityJDCouponCompareVO.getId(),
                        activityJDReqSaveVO.getId(),
                        RedisKeyConstants.JD_ACTIVITY_CLAIMED_NUM,
                        RedisKeyConstants.JD_COUPON);
                if (inventory != null && inventory < collectCount) {
                    throw exception(JD_INVENTORY_UPDATE_ERROR);
                }
            }
            //删除优惠券库存缓存
            activityJDCacheService.remove(String.valueOf(activityJDReqSaveVO.getId()), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
            //新增优惠券库存缓存
            buildCoupon(couponListUpdate);
            activityJDCacheService.cacheActivityCoupon(couponListUpdate, activityJDReqSaveVO.getId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
        }
        if (!couponPackageBN) {
            //优惠券包发生修改
            //库存修改是否合理
            for (ActivityJDCouponPackageCompareVO activityJDCouponPackageCompareVO : couponPackageAfter){
                Integer inventory = activityJDCouponPackageCompareVO.getInventory();
                Integer collectCount = activityJDCacheService.getCacheInventory(activityJDCouponPackageCompareVO.getId(),
                        activityJDReqSaveVO.getId(),
                        RedisKeyConstants.JD_ACTIVITY_CLAIMED_NUM,
                        RedisKeyConstants.JD_COUPON);
                if (inventory != null && inventory < collectCount) {
                    throw exception(JD_INVENTORY_UPDATE_ERROR);
                }
            }
            //删除优惠券库存缓存
            activityJDCacheService.remove(String.valueOf(activityJDReqSaveVO.getId()), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
            //新增优惠券库存缓存
            buildCouponPackage(couponPackageListUpdate);
            activityJDCacheService.cacheActivityCouponPackage(couponPackageListUpdate, activityJDReqSaveVO.getId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);

        }

        //修改活动主表
        ActivityDO activityDO = getActivityDO(activityJDReqSaveVO);
        activityService.updateActivity(activityDO);
        //修改集点活动拓展表
        updateActivityJDDO(activityJDReqSaveVO);
        //修改集点关联门店表 与门店缓存
        updateActivityStoreDO(activityJDReqSaveVO, activityJDFullRespVO);

        //修改活动关联商品表
        updateActivityJDCommodityDO(activityJDReqSaveVO, activityJDFullRespVO);
        //修改活动关联优惠券表
        updateActivityJDCouponDO(activityJDReqSaveVO, activityJDFullRespVO);
        //修改活动关联优惠券包表
        updateActivityJDCouponPackageDO(activityJDReqSaveVO, activityJDFullRespVO);


        //更新缓存
        ActivityJDFullRespVO activityJDFullRespVOAfter = selectFullInfo(activityDO.getId());
        activityJDCacheService.cacheActivity(activityJDFullRespVOAfter, RedisKeyConstants.JD_ACTIVITY);
    }

    private void updateActivityJDCommodityDO(ActivityJDReqSaveVO activityJDReqSaveVO, ActivityJDFullRespVO activityJDFullRespVO) {
        //集点商品范围 （1全部商品，2部分商品）
        Integer collectPointsCommodityType = activityJDReqSaveVO.getCollectPointsCommodityType();
        if (collectPointsCommodityType != null) {
            if (collectPointsCommodityType == 1) {
                //删除集点活动商品
                activityJDCommodityService.deleteByActivityId(activityJDReqSaveVO.getId());
            }
            if (collectPointsCommodityType == 2) {
                //删除集点活动商品
                activityJDCommodityService.deleteByActivityId(activityJDReqSaveVO.getId());
                //新增部分商品数据
                createActivityJDCommodityDO(activityJDReqSaveVO, activityJDReqSaveVO.getId());
            }
        }



    }
    public boolean isContentEqualWithDuplicatesCoupon(List<ActivityJDCouponCompareVO> list1, List<ActivityJDCouponCompareVO> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;

        Multiset<ActivityJDCouponCompareVO> multiset1 = HashMultiset.create(list1);
        Multiset<ActivityJDCouponCompareVO> multiset2 = HashMultiset.create(list2);

        return multiset1.equals(multiset2);
    }

    public boolean isContentEqualWithDuplicatesCouponPackage(List<ActivityJDCouponPackageCompareVO> list1, List<ActivityJDCouponPackageCompareVO> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;

        Multiset<ActivityJDCouponPackageCompareVO> multiset1 = HashMultiset.create(list1);
        Multiset<ActivityJDCouponPackageCompareVO> multiset2 = HashMultiset.create(list2);

        return multiset1.equals(multiset2);
    }
    private void updateActivityJDCouponDO(ActivityJDReqSaveVO activityJDReqSaveVO, ActivityJDFullRespVO activityJDFullRespVO) {
        Long activityId = activityJDReqSaveVO.getId();
        List<ActivityJDCouponReqSaveVO> couponListUpdate = activityJDReqSaveVO.getCouponList();
        activityJDCouponService.deleteByActivityId(activityId);
        if (CollectionUtils.isEmpty(couponListUpdate)){
            return;
        }
        createActivityJDCouponDO(couponListUpdate, activityId);
    }

    private void updateActivityJDCouponPackageDO(ActivityJDReqSaveVO activityJDReqSaveVO, ActivityJDFullRespVO activityJDFullRespVO) {
        Long activityId = activityJDReqSaveVO.getId();
        List<ActivityJDCouponPackageReqSaveVO> couponPackageListUpdate = activityJDReqSaveVO.getCouponPackageList();
        activityJDCouponPackageService.deleteByActivityId(activityId);
        if (CollectionUtils.isEmpty(couponPackageListUpdate)){
            return;
        }
        createActivityJDCouponPackageDO(couponPackageListUpdate, activityId);
    }

    public ActivityJDRespVO selectInfo(Long id) {
        ActivityJDRespVO activityJDRespVO = new ActivityJDRespVO();

        //查询活动主表
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO == null){
            throw new ServiceException(ErrorCodeConstants.JD_NOT_FOUND);
        }
        //将主表信息嵌入返回对象
        convertActivityJDRespVOWithActivityDO(activityJDRespVO,activityDO);
        //将拓展表信息嵌入返回对象
        convertActivityJDRespVOWithThisDO(activityJDRespVO);
        //将门店信息嵌入返回对象 门店与集点活动缓存单独设置
        //将活动商品信息嵌入返回对象
        convertActivityJDRespVOWithCommodityDO(activityJDRespVO);

        //将推广渠道嵌入返回对象
        convertActivityJDRespVOWithChannelDO(activityJDRespVO);

        return activityJDRespVO;
    }

    @Override
    public ActivityJDFullRespVO selectFullInfo(Long id) {
        ActivityJDRespVO activityJDRespVO = selectInfo(id);
        ActivityJDFullRespVO activityJDFullRespVO = new ActivityJDFullRespVO();
        BeanUtils.copyProperties(activityJDRespVO,activityJDFullRespVO);

        //获取配置奖品信息
        List<ActivityJDCouponDO> activityJDCouponDOS = activityJDCouponService.selectByActivityId(id);
        List<ActivityJDCouponRespSaveVO> couponList = BeanUtils.toBean(activityJDCouponDOS, ActivityJDCouponRespSaveVO.class);
        List<ActivityJDCouponPackageDO> activityJDCouponPackageDOS = activityJDCouponPackageService.selectByActivityId(id);
        List<ActivityJDCouponPackageRespSaveVO> couponPackageList = BeanUtils.toBean(activityJDCouponPackageDOS, ActivityJDCouponPackageRespSaveVO.class);

        //获取门店信息
        Integer activityStore = activityJDRespVO.getActivityStore();
        if (activityStore != null && activityStore == 2) {
            List<StoreInfoDTO> storeInfoDTOList = activityStoreService.storesByActivityId(id);
            List<StoreInfoRespVO> stores = BeanUtils.toBean(storeInfoDTOList, StoreInfoRespVO.class);
            activityJDFullRespVO.setStoreIds(stores);
        }

        activityJDFullRespVO.setCouponList(couponList);
        activityJDFullRespVO.setCouponPackageList(couponPackageList);

        return activityJDFullRespVO;
    }

    private void convertActivityJDRespVOWithChannelDO(ActivityJDRespVO activityJDRespVO) {
        Long activityId = activityJDRespVO.getId();
        List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityId(activityId);
        List<ActivityChannelRespVO>  activityChannelRespVOList = new ArrayList<>();
        for (ActivityChannelDO activityChannelDO : activityChannelDOList) {
            ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
            BeanUtils.copyProperties(activityChannelDO,activityChannelRespVO);
            activityChannelRespVOList.add(activityChannelRespVO);
        }
        activityJDRespVO.setActivityChannelRespVOList(activityChannelRespVOList);
    }


/*    private void convertActivitySeckillRespVOWithCouponDO(ActivitySeckillRespVO activitySeckillRespVO) {

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
    }*/



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivityJD(Long id) throws IOException {

        //删除主表
         activityService.deleteActivity(id);
         //删除拓展表
        LambdaQueryWrapper<ActivityJDDO>   queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJDDO::getActivityId, id);
        activityJDMapper.delete(queryWrapper);
        List<StoreInfoDTO> storeInfoDTOList = activityStoreService.storesByActivityId(id);
        //删除门店表
        activityStoreService.deleteByActivityId(id);
        //删除商品表
        activityJDCommodityService.deleteByActivityId(id);
        //删除优惠券
        activityJDCouponService.deleteByActivityId(id);
        //删除推广渠道表
        activityChannelService.deleteByActivityId(id);
        //删除集点活动缓存
        activityJDCacheService.remove(String.valueOf(id), RedisKeyConstants.JD_ACTIVITY);
        if (CollectionUtil.isNotEmpty(storeInfoDTOList)) {
            //删除门店缓存
            activityJDCacheService.batchRemoveActivityStore(storeInfoDTOList, id, RedisKeyConstants.JD_ACTIVITY_STORE);
        }
        //删除优惠券兑换奖品缓存
        activityJDCacheService.remove(String.valueOf(id), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
        //删除优惠券包兑换奖品缓存
        activityJDCacheService.remove(String.valueOf(id), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
        //删除用户集点
        bzOrderApi.delOrderPointsByActivityId(id);
    }


    @Override
    public ActPointRespVO getPointsDetail(Long activityId,Long memberId) {
        ActPointRespVO activityPointRespVO = new ActPointRespVO();
        ActivityJDFullRespVO activity = activityJDCacheService.getActivity(activityId, RedisKeyConstants.JD_ACTIVITY);
        if (ObjectUtil.isEmpty(activity)) {
            throw exception(JD_NOT_FOUND_V2);
        }

        Integer isEnabled = activity.getIsEnabled();
        if (isEnabled == null || isEnabled == 0){
            throw exception(JD_GROUND);
        }
        Date endDate = activity.getEndDate();
        Date startDate = activity.getStartDate();
        if (endDate == null) {
            throw exception(JD_NOT_FOUND_V2);
        }else {
            Calendar end = Calendar.getInstance();
            end.setTime(endDate);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 0);

            Calendar start = Calendar.getInstance();
            start.setTime(startDate);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            Date endDateWithTime = end.getTime();
            Date startDateWithTime = start.getTime();
            int endCompare = new Date().compareTo(endDateWithTime);
            int startCompare = new Date().compareTo(startDateWithTime);
            if (endCompare > 0 || startCompare < 0) {
                throw exception(JD_END);
            }
        }

        // 活动信息
        activityPointRespVO.setActivity(activity);

        Set<Long> memberClaimedCoupons = new HashSet<>();
        // 4. 获取用户已领取的优惠券
        if(ObjectUtil.isNotEmpty(memberId)){
            memberClaimedCoupons = activityJDCacheService.getMemberClaimedCoupons(activityId, memberId);
            activityPointRespVO.setMemberClaimedCoupons(ObjectUtil.isNotEmpty(memberClaimedCoupons) ? memberClaimedCoupons : new HashSet<>());
            // 5. 获取用户点数
            Integer memberPoints = activityJDCacheService.getMemberPoints(activityId, memberId);
            activityPointRespVO.setMemberPoints(ObjectUtil.isNotEmpty(memberPoints) ? memberPoints : 0);
        }else {
            activityPointRespVO.setMemberClaimedCoupons(new HashSet<>());
            activityPointRespVO.setMemberPoints(0);
        }

        // 1. 合并优惠券和券包列表
        List<ActivityJDCouponRespSaveVO> result = mergeCouponAndPackage(activity);
        // 2. 排序
        Map<Long, ActivityJDCouponReqSaveVO> allActivityCoupon = activityJDCacheService.getAllActivityCoupon(activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
        Map<Long, ActivityJDCouponPackageReqSaveVO> allActivityCouponPackages = activityJDCacheService.getAllActivityCouponPackages(activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
        List<ActivityJDCouponRespSaveVO> sortedList = sortCouponList(result,allActivityCoupon,allActivityCouponPackages);
        // 3. 批量处理名称设置和库存扣减
        processCouponInventoryAndName(sortedList, activityId,memberClaimedCoupons);
        activityPointRespVO.setCouponList(sortedList);

        // 5. 获取剩余时间
        Integer validityPeriod = activity.getValidityPeriod();
        if(ObjectUtil.notEqual(validityPeriod, 0)){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            // 计算当前周期的开始时间
            long currentCycleStart = calculateCurrentCycleStart(start, end, validityPeriod);
            activityPointRespVO.setSurplusTime(currentCycleStart);
        }else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date endDateWithTime = calendar.getTime();
            long milliseconds = endDateWithTime.getTime() - new Date().getTime();
            long seconds = milliseconds / 1000;
            activityPointRespVO.setSurplusTime(seconds);
        }

        return activityPointRespVO;
    }

    /**
     * 计算当前周期的开始时间
     */
    private long calculateCurrentCycleStart(LocalDateTime start, LocalDateTime end, int periodDays) {


        // 计算从开始时间到当前时间经过了多少天
        long daysBetween = ChronoUnit.DAYS.between(start, LocalDateTime.now());
        // 计算当前周期的开始日期（基于周期天数）
        long completedCycles = daysBetween / periodDays;
        LocalDateTime currentCycleStart = start.plusDays(completedCycles * periodDays);

        // 计算当前周期的结束时间
        LocalDateTime currentCycleEnd;
        if (periodDays == 1) {
            // 如果是1天周期，结束时间是当天的23:59:59
            currentCycleEnd = currentCycleStart.toLocalDate().atTime(LocalTime.MAX);
        } else {
            // 多天周期的结束时间是下个周期开始时间的前一秒
            currentCycleEnd = currentCycleStart.plusDays(periodDays).minusSeconds(1);
        }

        // 如果周期结束时间超过了总结束时间，则使用总结束时间
        if (currentCycleEnd.isAfter(end)) {
            currentCycleEnd = end;
        }

        // 计算剩余秒数
        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), currentCycleEnd);

        // 如果已经过期，返回0
        return Math.max(0, remainingSeconds);
    }

    /**
     * 合并优惠券和券包列表
     */
    private List<ActivityJDCouponRespSaveVO> mergeCouponAndPackage(ActivityJDFullRespVO activity) {
        List<ActivityJDCouponRespSaveVO> couponList = Optional.ofNullable(activity.getCouponList())
                .orElse(Collections.emptyList());

        List<ActivityJDCouponRespSaveVO> packageList = Optional.ofNullable(activity.getCouponPackageList())
                .map(list -> BeanUtils.toBean(list, ActivityJDCouponRespSaveVO.class))
                .orElse(Collections.emptyList());

        List<ActivityJDCouponRespSaveVO> result = new ArrayList<>(couponList.size() + packageList.size());
        result.addAll(couponList);
        result.addAll(packageList);

        return result;
    }

    /**
     * 排序优惠券列表
     */
    private List<ActivityJDCouponRespSaveVO> sortCouponList(List<ActivityJDCouponRespSaveVO> list,
                                                            Map<Long, ActivityJDCouponReqSaveVO> allActivityCoupon,
                                                            Map<Long, ActivityJDCouponPackageReqSaveVO> allActivityCouponPackages) {
        return list.stream()
                .filter(coupon -> {
                    // 检查是否是优惠券且已上架
                    if (allActivityCoupon.containsKey(coupon.getId())) {
                        return allActivityCoupon.get(coupon.getId()).getGoodCouponRespVO().getIsGround() != 0;
                    }
                    // 检查是否是优惠券包且已上架
                    if (allActivityCouponPackages.containsKey(coupon.getId())) {
                        return allActivityCouponPackages.get(coupon.getId()).getCouponPackageRespVO().getIsGround() != 0;
                    }
                    // 如果都不在映射中，过滤掉
                    return true;
                })
                .sorted(Comparator
                        .comparing(ActivityJDCouponRespSaveVO::getRedeemPoints,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(ActivityJDCouponRespSaveVO::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                )
                .collect(Collectors.toList());
    }

    /**
     * 批量处理名称设置和库存扣减
     */
    private void processCouponInventoryAndName(List<ActivityJDCouponRespSaveVO> couponList, Long activityId,Set<Long> memberClaimedCoupons) {
        // 批量获取已兑换数量
        Map<Long, Integer> allClaimedCoupons = activityJDCacheService.getAllClaimedCoupons(activityId);

        // 使用 Stream 批量处理
        couponList.forEach(coupon -> {
            // 设置名称
            setNameByGoodsType(coupon);
            if (memberClaimedCoupons != null && memberClaimedCoupons.contains(coupon.getId())) {
                coupon.setClaimed(Boolean.TRUE);
            }
            // 扣减库存
            deductInventory(coupon, allClaimedCoupons);
        });
    }

    /**
     * 根据商品类型设置名称
     */
    private void setNameByGoodsType(ActivityJDCouponRespSaveVO coupon) {
        if (ObjectUtil.equal(coupon.getGoodsType(), 1L)) {
            coupon.setName(coupon.getCouponName());
        } else {
            coupon.setName(coupon.getPackageName());
        }
    }

    /**
     * 扣减库存
     */
    private void deductInventory(ActivityJDCouponRespSaveVO coupon, Map<Long, Integer> claimedCoupons) {
        if (ObjectUtil.isEmpty(claimedCoupons)) {
            return;
        }

        Integer claimedCount = claimedCoupons.get(coupon.getId());
        if (claimedCount != null && claimedCount > 0) {
            int remainingInventory = coupon.getInventory() - claimedCount;
            coupon.setInventory(Math.max(remainingInventory, 0)); // 确保库存不为负数
        }
    }

    @Override
    public void incrMemberPointsByMe(Long activityId, Long memberId, Integer points) {
        activityJDCacheService.incrMemberPointsByMe(activityId, memberId, points);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(ActivityJDEnabledUpdateReqVO activityJDEnabledUpdateReqVO) {
        Long activityId = activityJDEnabledUpdateReqVO.getId();
        Integer isEnabled = activityJDEnabledUpdateReqVO.getIsEnabled();

        activityService.updateStatus(activityId,isEnabled);

        ActivityJDFullRespVO activityJDRespVO = selectFullInfo(activityId);
        activityJDCacheService.cacheActivity(activityJDRespVO, RedisKeyConstants.JD_ACTIVITY);
    }

    private void convertActivityJDRespVOWithCommodityDO(ActivityJDRespVO activityJDRespVO) {
        Long activityId = activityJDRespVO.getId();
        List<ActivityJDCommodityDO> activityJDCommodityDOS =  activityJDCommodityService.selectByActivityId(activityId);
        List<ActivityJDCommodityRespVO> activityJDCommodityRespVOS = new ArrayList<>();

        activityJDCommodityDOS.forEach(doObj -> {
            ActivityJDCommodityRespVO voObj = new ActivityJDCommodityRespVO();
            BeanUtils.copyProperties(doObj, voObj);
            activityJDCommodityRespVOS.add(voObj);
        });



        activityJDRespVO.setActivityJDCommodityRespList(activityJDCommodityRespVOS);

    }
    private void convertActivityJDRespVOWithThisDO(ActivityJDRespVO activityJDRespVO) {
        Long activityId = activityJDRespVO.getId();
        LambdaQueryWrapper<ActivityJDDO> activityJDQueryWrapper = new LambdaQueryWrapper<>();
        activityJDQueryWrapper.eq(ActivityJDDO::getActivityId, activityId);
        ActivityJDDO activityJDDO = activityJDMapper.selectOne(activityJDQueryWrapper);
        BeanUtils.copyProperties(activityJDDO,activityJDRespVO);
        //将 id 复原
        activityJDRespVO.setId(activityId);
    }

    private void convertActivityJDRespVOWithActivityDO(ActivityJDRespVO activityJDRespVO, ActivityDO activityDO) {
        BeanUtils.copyProperties(activityDO,activityJDRespVO);

    }


    private void updateActivityStoreDO(ActivityJDReqSaveVO activityJDReqSaveVO, ActivityJDFullRespVO activityJDFullRespVO) {
        //获取更新后活动门店（1全部门店 2部分门店）
        Integer activityStore = activityJDReqSaveVO.getActivityStore();
        //获取更新后的门店信息
        List<Long> storeIdsUpdate = activityJDReqSaveVO.getStoreIds();

        //查询更新前门店信息
        Integer activityStoreBefore = activityJDFullRespVO.getActivityStore();
        List<StoreInfoRespVO> storeList = activityJDFullRespVO.getStoreIds();
        List<StoreInfoDTO> StoreInfoDTOList = BeanUtils.toBean(storeList, StoreInfoDTO.class);

        List<Long> storeIdsBefore = CollectionUtil.isEmpty(storeList)?null:storeList.stream().map(StoreInfoRespVO::getStoreId).toList();

        if (activityStore != null && Objects.equals(activityStore, activityStoreBefore) && activityStore == 1) {
            return;
        }
        if (activityStore != null && Objects.equals(activityStore, activityStoreBefore) && activityStore == 2) {
            //部分门店调整
            //删除原部分门店缓存
            if (CollectionUtil.isNotEmpty(StoreInfoDTOList)) {
                activityJDCacheService.batchRemoveActivityStore(StoreInfoDTOList, activityJDReqSaveVO.getId(), RedisKeyConstants.JD_ACTIVITY_STORE);
            }
            //删除活动门店数据
            activityStoreService.deleteByActivityId(activityJDReqSaveVO.getId());
            //新增门店数据
            if (ObjectUtil.isNotEmpty(activityJDReqSaveVO.getStoreIds()) && activityJDReqSaveVO.getActivityStore()== 2){
                List<Long> storeIds = activityJDReqSaveVO.getStoreIds();
                Long activityId = activityJDReqSaveVO.getId();
                activityStoreService.createBatch(storeIds, activityId);
            }
            //新增活动门店缓存
            setActivityStoreCache(activityJDReqSaveVO, activityJDReqSaveVO.getStoreIds(),activityJDReqSaveVO.getId());

        }
        if (activityStore != null && !Objects.equals(activityStore, activityStoreBefore) && activityStore == 1) {
            //部分门店切换为全部门店
            //删除原门店数据
            activityStoreService.deleteByActivityId(activityJDReqSaveVO.getId());
            //删除原门店缓存
            if (CollectionUtil.isNotEmpty(StoreInfoDTOList)) {
                activityJDCacheService.batchRemoveActivityStore(StoreInfoDTOList, activityJDReqSaveVO.getId(), RedisKeyConstants.JD_ACTIVITY_STORE);
            }
            //刷新门店缓存
            setActivityStoreCache(activityJDReqSaveVO, activityJDReqSaveVO.getStoreIds(),activityJDReqSaveVO.getId());

        }
        if (activityStore != null && !Objects.equals(activityStore, activityStoreBefore) && activityStore == 2) {
            //全部门店切换为部分门店
            if (ObjectUtil.isNotEmpty(activityJDReqSaveVO.getStoreIds()) && activityJDReqSaveVO.getActivityStore()== 2){
                List<Long> storeIds = activityJDReqSaveVO.getStoreIds();
                Long activityId = activityJDReqSaveVO.getId();
                activityStoreService.createBatch(storeIds, activityId);
            }
            //删除全部门店活动缓存
            CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
            if (allStoreList.isSuccess() && ObjectUtil.isNotEmpty(allStoreList.getData())) {
                List<StoreInfoDTO> allStore = allStoreList.getCheckedData();
                activityJDCacheService.batchRemoveActivityStore(allStore, activityJDReqSaveVO.getId(), RedisKeyConstants.JD_ACTIVITY_STORE);
            }
            //刷新门店缓存
            setActivityStoreCache(activityJDReqSaveVO, activityJDReqSaveVO.getStoreIds(),activityJDReqSaveVO.getId());
        }





    }
    private List<Long> createActivityStoreDO(ActivityJDReqSaveVO activityJDReqSaveVO, Long activityId) {
        if (ObjectUtil.isNotEmpty(activityJDReqSaveVO.getStoreIds()) && activityJDReqSaveVO.getActivityStore() == 2){
            List<Long> storeIds = activityJDReqSaveVO.getStoreIds();
            activityStoreService.createBatch(storeIds, activityId);
            return storeIds;
        }
        return null;
    }

    private void createActivityJDCommodityDO(ActivityJDReqSaveVO activityJDReqSaveVO, Long activityId) {
        List<ActivityJDCommoditySaveReqVO> activityJDCommoditySaveReqVOList = activityJDReqSaveVO.getActivityJDCommoditySaveReqVOList();
        if (CollectionUtils.isEmpty(activityJDCommoditySaveReqVOList)){
            return;
        }
        activityJDCommoditySaveReqVOList.forEach(c->c.setId(null));
        List<ActivityJDCommodityDO> activityJDCommodityList = new ArrayList<>();
        for (ActivityJDCommoditySaveReqVO activityJDCommoditySaveReqVO : activityJDCommoditySaveReqVOList) {
            ActivityJDCommodityDO activityJDCommodityDO = new ActivityJDCommodityDO();
            BeanUtils.copyProperties(activityJDCommoditySaveReqVO, activityJDCommodityDO);
            activityJDCommodityDO.setActivityId(activityId);
            activityJDCommodityList.add(activityJDCommodityDO);

        }
        activityJDCommodityService.createBatch(activityJDCommodityList);
    }

    private void createActivityJDDO(ActivityJDReqSaveVO activityJDReqSaveVO, Long activityId) {
        ActivityJDDO activityJDDO = new ActivityJDDO();
        BeanUtils.copyProperties(activityJDReqSaveVO, activityJDDO);
        activityJDDO.setActivityId(activityId);
        activityJDDO.setShareDescription(shareDescription);
        activityJDDO.setShareTitle(shareTitle);
        activityJDDO.setShareImageUrl(shareImageUrl);
        activityJDMapper.insert(activityJDDO);
    }


    private void updateActivityJDDO(ActivityJDReqSaveVO activityJDReqSaveVO) {
        LambdaUpdateWrapper<ActivityJDDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(ActivityJDDO::getActivityId, activityJDReqSaveVO.getId());
        lambdaUpdateWrapper.set(ActivityJDDO::getActivityName, activityJDReqSaveVO.getActivityName());
        lambdaUpdateWrapper.set(ActivityJDDO::getBgImageUrl, activityJDReqSaveVO.getBgImageUrl());
        lambdaUpdateWrapper.set(ActivityJDDO::getBackgroundColor, activityJDReqSaveVO.getBackgroundColor());
        lambdaUpdateWrapper.set(ActivityJDDO::getCollectPointsImageUrl, activityJDReqSaveVO.getCollectPointsImageUrl());
        lambdaUpdateWrapper.set(ActivityJDDO::getCollectedPointsImageUrl, activityJDReqSaveVO.getCollectedPointsImageUrl());
        lambdaUpdateWrapper.set(ActivityJDDO::getCollectPointsType, activityJDReqSaveVO.getCollectPointsType());
        lambdaUpdateWrapper.set(ActivityJDDO::getCollectPointsThreshold, activityJDReqSaveVO.getCollectPointsThreshold());
        lambdaUpdateWrapper.set(ActivityJDDO::getCollectPointsCommodityType, activityJDReqSaveVO.getCollectPointsCommodityType());
        lambdaUpdateWrapper.set(ActivityJDDO::getValidityPeriod, activityJDReqSaveVO.getValidityPeriod());
        lambdaUpdateWrapper.set(ActivityJDDO::getDistributeMode, activityJDReqSaveVO.getDistributeMode());
        lambdaUpdateWrapper.set(ActivityJDDO::getShareSetting, activityJDReqSaveVO.getShareSetting());
        lambdaUpdateWrapper.set(ActivityJDDO::getCommunityFlag, activityJDReqSaveVO.getCommunityFlag());

        activityJDMapper.update(lambdaUpdateWrapper);

    }

    @Override
    public void updateCoupon(Long id) {

        List<ActivityJDCouponDO> activityList = activityJDCouponService.selectActivityByCouponId(id);
        if (CollectionUtil.isEmpty(activityList)) {
            return;
        }
        GoodCouponRespVO coupon = goodCouponService.getCouponById(id);
        if (Objects.isNull(coupon)) {
            return;
        }

        for (ActivityJDCouponDO activityCoupon : activityList) {
            Long activityId = activityCoupon.getActivityId();

            ActivityJDCouponReqSaveVO activityJDCouponReqSaveVO = BeanUtils.toBean(activityCoupon, ActivityJDCouponReqSaveVO.class);
            activityJDCouponReqSaveVO.setGoodCouponRespVO(coupon);

            activityJDCacheService.updateActivityCoupon(activityJDCouponReqSaveVO, activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
        }
    }

    @Override
    public void updateCouponPackage(Long id) {

        List<ActivityJDCouponPackageDO> activityList = activityJDCouponPackageService.selectActivityByCouponId(id);
        if (CollectionUtil.isEmpty(activityList)) {
            return;
        }

        CouponPackageRespVO coupon = couponPackageService.selectById(id);
        if (Objects.isNull(coupon)) {
            return;
        }

        for (ActivityJDCouponPackageDO activityCouponPackage : activityList) {
            Long activityId = activityCouponPackage.getActivityId();

            ActivityJDCouponPackageReqSaveVO activityJDCouponPackageReqSaveVO = BeanUtils.toBean(activityCouponPackage, ActivityJDCouponPackageReqSaveVO.class);
            activityJDCouponPackageReqSaveVO.setCouponPackageRespVO(coupon);

            activityJDCacheService.updateActivityCouponPackage(activityJDCouponPackageReqSaveVO, activityId, RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
        }
    }
}
