package com.htyoudao.youdao.module.promotion.service.activityJk;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.excel.core.service.vo.ActivityLogStatisticsVO;
import com.htyoudao.youdao.framework.excel.core.service.vo.LotteryLogStatisticsVO;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCrowdRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj.ActivityMjDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityCardLog.ActivityCardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJD.ActivityJDMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkCard.ActivityJkCardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrize.ActivityJkPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrizeExchange.ActivityJkPrizeExchangeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJk.ActivityJkMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMj.ActivityMjMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingImage.AdvertisingImageMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.LotteryRedisDAO;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.enums.LotteryStateEnum;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.ActivityJkCard.ActivityJkCardService;
import com.htyoudao.youdao.module.promotion.service.ActivityJkPrize.ActivityJkPrizeService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import com.htyoudao.youdao.module.promotion.service.activityJkCommodity.ActivityJkCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityMj.ActivityMjService;
import com.htyoudao.youdao.module.promotion.service.activityMjCommodity.ActivityMjCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotterySettingServiceImpl;
import com.htyoudao.youdao.module.promotion.util.ConvertUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.mzt.logapi.context.LogRecordContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.JKKeyConstants.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.PRIZE_NUM;

/**
 * @author Yangqinglin
 */
@Service
@Slf4j
@RefreshScope
public class ActivityJkServiceImpl extends ServiceImpl<ActivityJkMapper, ActivityJkDO> implements ActivityJkService {


    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    private ActivityJkMapper activityJkMapper;

    @Resource
    private ActivityJkPrizeMapper activityJkPrizeMapper;

    @Resource
    private ActivityJkCardMapper activityJkCardMapper;

    @Resource
    private ActivityJkCardService activityJkCardService;

    @Resource
    private ActivityJkPrizeService activityJkPrizeService;

    @Resource
    private ActivityCardLogMapper activityCardLogMapper;

    @Resource
    private LotteryRedisDAO lotteryRedisDAO;
    @Resource
    public RedisCache redisCache;

    @Resource
    private ActivityService activityService;
    @Resource
    private CouponPackageService couponPackageService;


    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityJkCommodityService activityJkCommodityService;


    @Resource
    private ActivityChannelService activityChannelService;


    @Resource
    private AdvertisingImageMapper advertisingImageMapper;

    @Resource
    private ActivityJkPrizeExchangeMapper activityJkPrizeExchangeMapper;

    @Resource
    private IEventService eventService;

    @Resource
    private ExcelActionService<ActivityJkLogExportReqVO> exportReqVOExcelActionService;

    // 分享图片
    @Value("${activity.card.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.card.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.card.shareDescription}")
    private String shareDescription;


    @DubboReference
    private StoreApi storeApi;

    @Autowired
    private ActivityLogQueryService activityLogQueryService;

    @Override
    @Transactional
    public CommonResult<Integer> createActivityJk(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {

        verificationData(saveOrUpdateReqVO.getCardReqVOList());
        // 新增主表
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(saveOrUpdateReqVO, activityDO);
        activityDO.setWeekNumbers(convertListToString(saveOrUpdateReqVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(saveOrUpdateReqVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(saveOrUpdateReqVO.getTimeRangeList()));
        activityDO.setActivityType(ActivityTypeEnum.JK.getCode());
        activityDO.setActivityRemark(saveOrUpdateReqVO.getActivityRemark());
        activityDO.setActivityName(saveOrUpdateReqVO.getActivityName());
        activityDO.setActivityRules(saveOrUpdateReqVO.getActivityRules());
        activityDO.setIsEnabled(0);
        Long activityId = activityService.createActivity(activityDO);

        // 新增门店列表
        if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getStoreIds())) {
            activityStoreService.createBatch(saveOrUpdateReqVO.getStoreIds(), activityId);
        }

        //新增商品列表
        if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCommodityIds())) {
            activityJkCommodityService.createBatch(saveOrUpdateReqVO.getCommodityIds(), activityId);
        }

        // 判断卡片存在唯一兜底卡片
        if (!validateGuaranteedPrize(saveOrUpdateReqVO)) {
            return error(JK_GUARANTEED_CARD);
        }

//
//        ActivityJkDO activityJkDO = BeanUtils.toBean(saveOrUpdateReqVO, ActivityJkDO.class);
//        activityJkDO.setActivityId(activityId);
//        activityJkMapper.insert(activityJkDO);

//        int type = switch (saveOrUpdateReqVO.getActivityType()) {
//            case 6 -> ActivityChannelTypeEnum.CARD.getCode();
//            default -> 0;
//        };
//        //创建短链
//        createChannelDO(activityId, type);
        createActivityJDDO(saveOrUpdateReqVO, activityId);
        saveOrUpdateReqVO.setIsEnabled(0);
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.CARD.getCode());

        //添加活动缓存
        lotteryRedisDAO.setCacheObject(JK_SETTING + activityId, saveOrUpdateReqVO);


        if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getPrizeReqVOList())){
            List<ActivityJkPrizeDO> prizeList = saveOrUpdateReqVO.getPrizeReqVOList().stream().map(item -> {
                        ActivityJkPrizeDO activityJkPrizeDO = BeanUtils.toBean(item, ActivityJkPrizeDO.class);
                        activityJkPrizeDO.setActivityId(activityId);
                        return activityJkPrizeDO;
                    })
                    .collect(Collectors.toList());

            activityJkPrizeMapper.insertBatch(prizeList);
            //添加奖品缓存
            lotteryRedisDAO.setCacheObject(JK_PRIZE + activityId, prizeList);
        }

        if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCardReqVOList())){
            List<ActivityJkCardDO> cardList = saveOrUpdateReqVO.getCardReqVOList().stream().map(item -> {
                        ActivityJkCardDO activityJkCardDO = BeanUtils.toBean(item, ActivityJkCardDO.class);
                        activityJkCardDO.setActivityId(activityId);
                        return activityJkCardDO;
                    })
                    .collect(Collectors.toList());

            activityJkCardMapper.insertBatch(cardList);
            //添加卡片缓存
            lotteryRedisDAO.setCacheObject(JK_CARD + activityId, cardList);
        }

        return success(1);
    }

    @Override
    @Transactional
    public CommonResult<Integer> updateActivityJk(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        verificationData(saveOrUpdateReqVO.getCardReqVOList());
        Long activityId = saveOrUpdateReqVO.getActivityId();
        ActivityDO activityDO = activityService.selectById(activityId);

        if(activityDO!=null){
            BeanUtils.copyProperties(saveOrUpdateReqVO,activityDO);
            activityDO.setWeekNumbers(convertListToString(saveOrUpdateReqVO.getWeekNumberList()));
            activityDO.setDayNumbers(convertListToString(saveOrUpdateReqVO.getDayNumberList()));
            activityDO.setTimeRange(convertListToString(saveOrUpdateReqVO.getTimeRangeList()));
            activityDO.setActivityRemark(saveOrUpdateReqVO.getActivityRemark());
            activityDO.setActivityName(saveOrUpdateReqVO.getActivityName());
            activityDO.setActivityRules(saveOrUpdateReqVO.getActivityRules());
            activityDO.setCommunityFlag(saveOrUpdateReqVO.getCommunityFlag());
            activityDO.setId(activityId);
            activityService.updateActivity(activityDO);


            LambdaUpdateWrapper<ActivityJkDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ActivityJkDO::getId, saveOrUpdateReqVO.getId());
            updateWrapper.set(ActivityJkDO::getActivityCoverImage, saveOrUpdateReqVO.getActivityCoverImage());
            updateWrapper.set(ActivityJkDO::getActivityBackgroundImage, saveOrUpdateReqVO.getActivityBackgroundImage());
            updateWrapper.set(ActivityJkDO::getMyBackgroundImage, saveOrUpdateReqVO.getMyBackgroundImage());
            updateWrapper.set(ActivityJkDO::getCardButtonBackgroundImage, saveOrUpdateReqVO.getCardButtonBackgroundImage());
            updateWrapper.set(ActivityJkDO::getActivityDetailsImage, saveOrUpdateReqVO.getActivityDetailsImage());
            updateWrapper.set(ActivityJkDO::getButtonBackgroundColor, saveOrUpdateReqVO.getButtonBackgroundColor());
            updateWrapper.set(ActivityJkDO::getDailyAttendance, saveOrUpdateReqVO.getDailyAttendance());
            updateWrapper.set(ActivityJkDO::getDailyAttenCount, saveOrUpdateReqVO.getDailyAttenCount());
            updateWrapper.set(ActivityJkDO::getPlaceOrderStatus, saveOrUpdateReqVO.getPlaceOrderStatus());
            updateWrapper.set(ActivityJkDO::getPlaceOrderType, saveOrUpdateReqVO.getPlaceOrderType());
            updateWrapper.set(ActivityJkDO::getPlaceOrderProduct, saveOrUpdateReqVO.getPlaceOrderProduct());
            updateWrapper.set(ActivityJkDO::getPaymentThreshold, saveOrUpdateReqVO.getPaymentThreshold());
            updateWrapper.set(ActivityJkDO::getPaymentCount, saveOrUpdateReqVO.getPaymentCount());
            updateWrapper.set(ActivityJkDO::getPlaceOrderCardNumber, saveOrUpdateReqVO.getPlaceOrderCardNumber());
            updateWrapper.set(ActivityJkDO::getShareEvent, saveOrUpdateReqVO.getShareEvent());
            updateWrapper.set(ActivityJkDO::getShareCount, saveOrUpdateReqVO.getShareCount());
            updateWrapper.set(ActivityJkDO::getAwardDistribution, saveOrUpdateReqVO.getAwardDistribution());
            updateWrapper.set(ActivityJkDO::getShareType, saveOrUpdateReqVO.getShareType());
            updateWrapper.set(ActivityJkDO::getPublicButton, saveOrUpdateReqVO.getPublicButton());
            activityJkMapper.update(updateWrapper);






            LambdaQueryWrapper<ActivityJkDO> activityJkLambdaQueryWrapper = new LambdaQueryWrapper<>();
            activityJkLambdaQueryWrapper.eq(ActivityJkDO::getActivityId,activityId);
            ActivityJkDO activityJkDO = activityJkMapper.selectOne(activityJkLambdaQueryWrapper);
            Long activityJkId = activityJkDO.getId();
            BeanUtils.copyProperties(saveOrUpdateReqVO,activityJkDO);
            activityJkDO.setId(activityJkId);
            activityJkMapper.updateById(activityJkDO);
            saveOrUpdateReqVO.setIsEnabled(activityDO.getIsEnabled());
            //删除活动关联商品
            activityJkCommodityService.deleteByActivityId(activityId);

            if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCommodityIds())) {
                activityJkCommodityService.createBatch(saveOrUpdateReqVO.getCommodityIds(), activityId);
            }
            //删除关联的门店
            activityStoreService.deleteByActivityId(activityId);
            if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getStoreIds())) {
                activityStoreService.createBatch(saveOrUpdateReqVO.getStoreIds(), activityId);
            }

//            LambdaQueryWrapper<ActivityJkCardDO> jkCardLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            jkCardLambdaQueryWrapper.eq(ActivityJkCardDO::getActivityId, activityId);
//            activityJkCardMapper.delete(jkCardLambdaQueryWrapper);
//
//
//
//            if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCardReqVOList())){
//                List<ActivityJkCardDO> cardList = saveOrUpdateReqVO.getCardReqVOList().stream().map(item -> {
//                            ActivityJkCardDO activityJkCardDO = BeanUtils.toBean(item, ActivityJkCardDO.class);
//                            activityJkCardDO.setActivityId(activityId);
//                            activityJkCardDO.setId(null);
//                            return activityJkCardDO;
//                        })
//                        .collect(Collectors.toList());
//
//                activityJkCardMapper.insertBatch(cardList);
//                //添加卡片缓存
//                lotteryRedisDAO.setCacheObject(JK_CARD + activityId, cardList);
//            }


            //所有的卡片
            LambdaQueryWrapper<ActivityJkCardDO> doLambdaQueryWrapper = new LambdaQueryWrapper<>();
            doLambdaQueryWrapper.eq(ActivityJkCardDO::getActivityId, activityId);
            List<ActivityJkCardDO> selectList = activityJkCardMapper.selectList(doLambdaQueryWrapper);
            if(ObjectUtil.isNotEmpty(selectList)){
                List<Long> ids = selectList.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                List<Long> collect = saveOrUpdateReqVO.getCardReqVOList().stream()
                        .filter(item -> ObjectUtil.isNotEmpty(item.getId())).map(mm -> mm.getId()).collect(Collectors.toList());
                Set<Long> idsSet = new HashSet<>(ids);
                Set<Long> collectSet = new HashSet<>(collect);

                // 使用Set差集：collectSet - idsSet
                //                Set<Long> missingIdSet = new HashSet<>(collectSet);
                //                missingIdSet.removeAll(idsSet);

                Set<Long> missingIdSet = new HashSet<>(idsSet);
                missingIdSet.removeAll(collectSet);
                List<Long> missingIds = new ArrayList<>(missingIdSet);
                if(ObjectUtil.isNotEmpty(missingIds)){
                    activityJkCardMapper.deleteByIds(missingIds);
                }



                // 取两个集合的交集（需要更新的ID）
                Set<Long> intersectionSet = new HashSet<>(collectSet);
                intersectionSet.retainAll(idsSet);
                List<Long> updateIds = new ArrayList<>(intersectionSet);

                if (ObjectUtil.isNotEmpty(updateIds)) {
                    // 找到需要更新的卡片数据
                    List<ActivityJkCardReqVO> updateCards = saveOrUpdateReqVO.getCardReqVOList().stream()
                            .filter(item -> updateIds.contains(item.getId()))
                            .collect(Collectors.toList());

                    // 构建更新列表
                    List<ActivityJkCardDO> updateList = updateCards.stream()
                            .map(item -> {
                                ActivityJkCardDO cardDO = BeanUtils.toBean(item, ActivityJkCardDO.class);
                                cardDO.setRemainNum(null);
                                return cardDO;
                            })
                            .collect(Collectors.toList());

                    // 批量更新
                    boolean updateSuccess = activityJkCardService.updateBatchById(updateList);
                    log.info("批量更新奖品数据结果: {}, 更新数量: {}", updateSuccess ? "成功" : "失败", updateList.size());
                }



                if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCardReqVOList())){
                    List<ActivityJkCardReqVO> saveOrUpdateReqVOCardReqVOList = saveOrUpdateReqVO.getCardReqVOList();
                    for (ActivityJkCardReqVO activityJkCardReqVO : saveOrUpdateReqVOCardReqVOList) {
                        if(ObjectUtil.isEmpty(activityJkCardReqVO.getId())){
                            ActivityJkCardDO cardDO = BeanUtils.toBean(activityJkCardReqVO, ActivityJkCardDO.class);
                            cardDO.setActivityId(activityId);
                            activityJkCardMapper.insert(cardDO);
                        }

                    }
                }

                LambdaQueryWrapper<ActivityJkCardDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(ActivityJkCardDO::getActivityId,activityId);
                List<ActivityJkCardDO> lotteryPrizeDOS = activityJkCardMapper.selectList(lambdaQueryWrapper);
                lotteryRedisDAO.setCacheObject(JK_CARD + activityId, lotteryPrizeDOS);
            }



//            LambdaQueryWrapper<ActivityJkPrizeDO> jkPrizeLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            jkPrizeLambdaQueryWrapper.eq(ActivityJkPrizeDO::getActivityId, activityId);
//            activityJkPrizeMapper.delete(jkPrizeLambdaQueryWrapper);
//            if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getPrizeReqVOList())){
//                List<ActivityJkPrizeDO> prizeList = saveOrUpdateReqVO.getPrizeReqVOList().stream().map(item -> {
//                            ActivityJkPrizeDO activityJkPrizeDO = BeanUtils.toBean(item, ActivityJkPrizeDO.class);
//                            activityJkPrizeDO.setActivityId(activityId);
//                            activityJkPrizeDO.setId(null);
//                            return activityJkPrizeDO;
//                        })
//                        .collect(Collectors.toList());
//
//                activityJkPrizeMapper.insertBatch(prizeList);
//                //添加奖品缓存
//                lotteryRedisDAO.setCacheObject(JK_PRIZE + activityId, prizeList);
//            }
            //所有的卡片
            LambdaQueryWrapper<ActivityJkPrizeDO> prizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            prizeDOLambdaQueryWrapper.eq(ActivityJkPrizeDO::getActivityId, activityId);
            List<ActivityJkPrizeDO> selectedList = activityJkPrizeMapper.selectList(prizeDOLambdaQueryWrapper);
            if(ObjectUtil.isNotEmpty(selectedList)){
                List<Long> ids = selectedList.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                List<Long> collect = saveOrUpdateReqVO.getPrizeReqVOList().stream()
                        .filter(item -> ObjectUtil.isNotEmpty(item.getId())).map(mm -> mm.getId()).collect(Collectors.toList());
                Set<Long> idsSet = new HashSet<>(ids);
                Set<Long> collectSet = new HashSet<>(collect);

                // 使用Set差集：collectSet - idsSet
                //                Set<Long> missingIdSet = new HashSet<>(collectSet);
                //                missingIdSet.removeAll(idsSet);

                Set<Long> missingIdSet = new HashSet<>(idsSet);
                missingIdSet.removeAll(collectSet);
                List<Long> missingIds = new ArrayList<>(missingIdSet);
                if(ObjectUtil.isNotEmpty(missingIds)){
                    activityJkPrizeMapper.deleteByIds(missingIds);
                }



                // 取两个集合的交集（需要更新的ID）
                Set<Long> intersectionSet = new HashSet<>(collectSet);
                intersectionSet.retainAll(idsSet);
                List<Long> updateIds = new ArrayList<>(intersectionSet);

                if (ObjectUtil.isNotEmpty(updateIds)) {
                    // 找到需要更新的卡片数据
                    List<ActivityJkPrizeReqVO> updatePrizes = saveOrUpdateReqVO.getPrizeReqVOList().stream()
                            .filter(item -> updateIds.contains(item.getId()))
                            .collect(Collectors.toList());

                    // 构建更新列表
                    List<ActivityJkPrizeDO> updateList = updatePrizes.stream()
                            .map(item -> {
                                ActivityJkPrizeDO cardDO = BeanUtils.toBean(item, ActivityJkPrizeDO.class);
                                cardDO.setRemainNum(null);
                                return cardDO;
                            })
                            .collect(Collectors.toList());

                    // 批量更新
                    boolean updateSuccess = activityJkPrizeService.updateBatchById(updateList);
                    log.info("批量更新奖品数据结果: {}, 更新数量: {}", updateSuccess ? "成功" : "失败", updateList.size());
                }






                if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getPrizeReqVOList())){
                    List<ActivityJkPrizeReqVO> saveOrUpdateReqVOCardReqVOList = saveOrUpdateReqVO.getPrizeReqVOList();
                    for (ActivityJkPrizeReqVO activityJkPrizeReqVO : saveOrUpdateReqVOCardReqVOList) {
                        if(ObjectUtil.isEmpty(activityJkPrizeReqVO.getId())){
                            ActivityJkPrizeDO prizeDO = BeanUtils.toBean(activityJkPrizeReqVO, ActivityJkPrizeDO.class);
                            prizeDO.setActivityId(activityId);
                            activityJkPrizeMapper.insert(prizeDO);
                        }

                    }
                }

                LambdaQueryWrapper<ActivityJkPrizeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(ActivityJkPrizeDO::getActivityId,activityId);
                List<ActivityJkPrizeDO> prizeDOList = activityJkPrizeMapper.selectList(lambdaQueryWrapper);
                lotteryRedisDAO.setCacheObject(JK_PRIZE + activityId, prizeDOList);
            }

            //添加活动缓存
            lotteryRedisDAO.setCacheObject(JK_SETTING + activityId, saveOrUpdateReqVO);
        }
        return success(1);

    }

    @Override
    public CommonResult<Integer> deleteActivityJk(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO != null) {
            if (activityDO.getIsEnabled() == 1) {
                return error(LOTTERY_DELETE);
            }
        }
        if (activityDO != null) {
            LambdaQueryWrapper<AdvertisingImageDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdvertisingImageDO::getActivityId, id);
            List<AdvertisingImageDO> imageDOList = advertisingImageMapper.selectList(wrapper);
            //校验是否有广告在使用
            if (ObjectUtil.isNotEmpty(imageDOList)) {
                throw exception(LOTTERY_ADVERTISING_DELETE);
            }else{
                //删除主表数据
                activityService.deleteActivity(id);

                //删除集卡核心表数据
                LambdaQueryWrapper<ActivityJkDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(ActivityJkDO::getActivityId, id);
                activityJkMapper.delete(lambdaQueryWrapper);


                //删除奖品数据
                LambdaQueryWrapper<ActivityJkPrizeDO> prizeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                prizeLambdaQueryWrapper.eq(ActivityJkPrizeDO::getActivityId, id);
                activityJkPrizeMapper.delete(prizeLambdaQueryWrapper);

                //删除卡片数据
                LambdaQueryWrapper<ActivityJkCardDO> cardLambdaQueryWrapper = new LambdaQueryWrapper<>();
                cardLambdaQueryWrapper.eq(ActivityJkCardDO::getActivityId, id);
                activityJkCardMapper.delete(cardLambdaQueryWrapper);

                //删除关联的门店
                LambdaQueryWrapper<ActivityStoreDO> storeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                storeLambdaQueryWrapper.eq(ActivityStoreDO::getActivityId, id);
                activityStoreService.remove(storeLambdaQueryWrapper);

                //删除关联的商品
                LambdaQueryWrapper<ActivityJkCommodityDO> comLambdaQueryWrapper = new LambdaQueryWrapper<>();
                comLambdaQueryWrapper.eq(ActivityJkCommodityDO::getActivityId, id);
                activityJkCommodityService.remove(comLambdaQueryWrapper);

                //清缓存
                lotteryRedisDAO.deleteObject(JK_SETTING + id);
                lotteryRedisDAO.deleteObject(JK_PRIZE + id);
                lotteryRedisDAO.deleteObject(JK_CARD + id);
            }
        }

        return success(1);

    }

    @Override
    public ActivityJkDetailRespVO selectInfo(Long id) {
        ActivityJkDetailRespVO jkDetailRespVO = new ActivityJkDetailRespVO();
        ActivityDO activityDO = activityService.selectById(id);
        if(activityDO!=null){
            BeanUtils.copyProperties(activityDO, jkDetailRespVO);
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            jkDetailRespVO.setStartDate(startDate);
            jkDetailRespVO.setEndDate(endDate);
            //设置门店信息
            if (jkDetailRespVO.getActivityStore().equals(0)) {
                jkDetailRespVO.setStoreInfoDTOList(activityStoreService.storesByActivityId(id));
            } else {
                jkDetailRespVO.setStoreInfoDTOList(new ArrayList<>());
            }
            LambdaQueryWrapper<ActivityJkDO> activityJkLambdaQueryWrapper = new LambdaQueryWrapper<>();
            activityJkLambdaQueryWrapper.eq(ActivityJkDO::getActivityId, id);
            ActivityJkDO activityJkDO = activityJkMapper.selectOne(activityJkLambdaQueryWrapper);
            if (activityJkDO != null) {
                List<CommodityDTO> commodityDTOS = activityJkCommodityService.listByActivityId(id);

                if(ObjectUtil.isNotEmpty(commodityDTOS)){
                    jkDetailRespVO.setCommodityDTOList(commodityDTOS);
                }else{
                    jkDetailRespVO.setCommodityDTOList(new ArrayList<>());
                }


                BeanUtils.copyProperties(activityJkDO, jkDetailRespVO);
                LambdaQueryWrapper<ActivityJkPrizeDO> prizeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                prizeLambdaQueryWrapper.eq(ActivityJkPrizeDO::getActivityId, id);
                List<ActivityJkPrizeDO> prizeList = activityJkPrizeMapper.selectList(prizeLambdaQueryWrapper);
                List<ActivityJkPrizeReqVO> prizeReqVOList = new ArrayList<>();

                if(ObjectUtil.isNotEmpty(prizeList)){
                    for (ActivityJkPrizeDO activityJkPrizeDO : prizeList) {
                        ActivityJkPrizeReqVO activityJkPrizeReqVO = new ActivityJkPrizeReqVO();
                        BeanUtils.copyProperties(activityJkPrizeDO, activityJkPrizeReqVO);
                        prizeReqVOList.add(activityJkPrizeReqVO);
                    }
                    jkDetailRespVO.setPrizeReqVOList(prizeReqVOList);
                }
                LambdaQueryWrapper<ActivityJkCardDO> cardLambdaQueryWrapper = new LambdaQueryWrapper<>();
                cardLambdaQueryWrapper.eq(ActivityJkCardDO::getActivityId, id);
                List<ActivityJkCardDO> cardList = activityJkCardMapper.selectList(cardLambdaQueryWrapper);
                List<ActivityJkCardReqVO> cardReqVOList = new ArrayList<>();
                if(ObjectUtil.isNotEmpty(cardList)){
                    for (ActivityJkCardDO activityJkCardDO : cardList) {
                        ActivityJkCardReqVO activityJkCardReqVO = new ActivityJkCardReqVO();
                        BeanUtils.copyProperties(activityJkCardDO, activityJkCardReqVO);
                        cardReqVOList.add(activityJkCardReqVO);
                    }
                    jkDetailRespVO.setCardReqVOList(cardReqVOList);
                }
            }


        }
        return jkDetailRespVO;
    }

    @Override
    public CommonResult<Integer> copyActivityJk(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        verificationData(saveOrUpdateReqVO.getCardReqVOList());
        // 新增主表
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(saveOrUpdateReqVO, activityDO);
        activityDO.setWeekNumbers(convertListToString(saveOrUpdateReqVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(saveOrUpdateReqVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(saveOrUpdateReqVO.getTimeRangeList()));
        activityDO.setActivityType(ActivityTypeEnum.JK.getCode());
        activityDO.setActivityRemark(saveOrUpdateReqVO.getActivityRemark());
        activityDO.setActivityName(saveOrUpdateReqVO.getActivityName());
        activityDO.setActivityRules(saveOrUpdateReqVO.getActivityRules());
        activityDO.setIsEnabled(0);
        Long activityId = activityService.createActivity(activityDO);

        // 新增门店列表
        if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getStoreIds())) {
            activityStoreService.createBatch(saveOrUpdateReqVO.getStoreIds(), activityId);
        }

        //新增商品列表
        if (ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCommodityIds())) {
            activityJkCommodityService.createBatch(saveOrUpdateReqVO.getCommodityIds(), activityId);
        }

        // 判断卡片存在唯一兜底卡片
        if (!validateGuaranteedPrize(saveOrUpdateReqVO)) {
            return error(JK_GUARANTEED_CARD);
        }

        saveOrUpdateReqVO.setId(null);
//        activityJkDO.setActivityId(activityId);
//        activityJkMapper.insert(activityJkDO);

//        int type = switch (saveOrUpdateReqVO.getActivityType()) {
//            case 8 -> ActivityChannelTypeEnum.CARD.getCode();
//            default -> 0;
//        };
//        //创建短链
//        createChannelDO(activityId, type);

        createActivityJDDO(saveOrUpdateReqVO, activityId);
        saveOrUpdateReqVO.setIsEnabled(0);
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.CARD.getCode());
        //添加活动缓存
        lotteryRedisDAO.setCacheObject(JK_SETTING + activityId, saveOrUpdateReqVO);


        if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getPrizeReqVOList())){
            List<ActivityJkPrizeDO> prizeList = saveOrUpdateReqVO.getPrizeReqVOList().stream().map(item -> {
                        ActivityJkPrizeDO activityJkPrizeDO = BeanUtils.toBean(item, ActivityJkPrizeDO.class);
                        activityJkPrizeDO.setActivityId(activityId);
                        activityJkPrizeDO.setId(null);
                        return activityJkPrizeDO;
                    })
                    .collect(Collectors.toList());

            activityJkPrizeMapper.insertBatch(prizeList);
            //添加奖品缓存
            lotteryRedisDAO.setCacheObject(JK_PRIZE + activityId, prizeList);
        }

        if(ObjectUtil.isNotEmpty(saveOrUpdateReqVO.getCardReqVOList())){
            List<ActivityJkCardDO> cardList = saveOrUpdateReqVO.getCardReqVOList().stream().map(item -> {
                        ActivityJkCardDO activityJkCardDO = BeanUtils.toBean(item, ActivityJkCardDO.class);
                        activityJkCardDO.setActivityId(activityId);
                        activityJkCardDO.setId(null);
                        return activityJkCardDO;
                    })
                    .collect(Collectors.toList());

            activityJkCardMapper.insertBatch(cardList);
            //添加卡片缓存
            lotteryRedisDAO.setCacheObject(JK_CARD + activityId, cardList);
        }

        return success(1);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public Map<String, Object> getActivityJkAnalysis(String id) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Long> result = new HashMap<>();
        long ids = Long.parseLong(id);
        ActivityDO activityDO = activityService.selectById(ids);

        // 获取抽奖活动开始时间
        Date startTime = activityDO.getStartDate();
        Date endTime = activityDO.getEndDate();
        LocalDateTime start = convertDateToLocalDateTime(startTime);
        LocalDateTime end = convertDateToLocalDateTime(endTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }

        LambdaQueryWrapper<ActivityCardLogDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.select(ActivityCardLogDO::getMemberId);
        lambdaQueryWrapper.eq(ActivityCardLogDO::getActivityId, id);
        lambdaQueryWrapper.ge(ActivityCardLogDO::getCreateTime, start);
        lambdaQueryWrapper.le(ActivityCardLogDO ::getCreateTime, end);


        List<Long> sum = new ArrayList<>();
        ResultHandler<Long> resultHandler = new ResultHandler<Long>() {
            @Override
            public void handleResult(ResultContext<? extends Long> resultContext) {
                // 获取当前行的结果（即 memberId）
                Long memberId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(memberId)) {
                    sum.add(memberId);
                }
            }
        };
        activityCardLogMapper.selectObjs(lambdaQueryWrapper, resultHandler);

        Set<Long> distinctSum = new HashSet<>(sum);

        result.put("sum", (long) sum.size());
        result.put("distinctSum", (long) distinctSum.size());



        Long couponCount = 0L;
        // 3. 统计优惠券发放张数（prizeType=1）
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(ActivityJkPrizeExchangeDO::getActivityId, id)
                .ge(ActivityJkPrizeExchangeDO::getCreateTime, start)
                .le(ActivityJkPrizeExchangeDO::getCreateTime, end)
                .eq(ActivityJkPrizeExchangeDO::getPrizeType, 2);

        Long selectCount = activityJkPrizeExchangeMapper.selectCount(couponWrapper);
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> packageWrapper = new LambdaQueryWrapper<>();
        packageWrapper.eq(ActivityJkPrizeExchangeDO::getActivityId, id)
                .ge(ActivityJkPrizeExchangeDO::getCreateTime, start)
                .le(ActivityJkPrizeExchangeDO::getCreateTime, end)
                .eq(ActivityJkPrizeExchangeDO::getPrizeType, 3);
        List<ActivityJkPrizeExchangeDO> activityJkPrizeExchangeDOS = activityJkPrizeExchangeMapper.selectList(packageWrapper);
        Long packageCount = 0L;
        if(ObjectUtil.isNotEmpty(activityJkPrizeExchangeDOS)){
            for (ActivityJkPrizeExchangeDO activityJkPrizeExchangeDO : activityJkPrizeExchangeDOS) {
                Long awardId = activityJkPrizeExchangeDO.getAwardId();
                CouponPackageRespVO couponPackageRespVO = couponPackageService.selectById(awardId);
                if(ObjectUtil.isNotEmpty(couponPackageRespVO)){
                    List<GoodCouponPackageRespVO> goodCouponPackageRespVOS = couponPackageRespVO.getGoodCouponPackageRespVOS();
                    if(ObjectUtil.isNotEmpty(goodCouponPackageRespVOS)){
                        packageCount = Long.valueOf(goodCouponPackageRespVOS.size())+packageCount;
                    }
                }
            }
        }

        couponCount = selectCount + packageCount;
        result.put("couponCount", couponCount);




        //统计积分
        QueryWrapperX<ActivityJkPrizeExchangeDO> pointsWrapper = new QueryWrapperX<>();
        pointsWrapper.select("SUM(prize_value) as totalCash");
        pointsWrapper.eq("activity_id", id);
        pointsWrapper.ge("create_time", start);
        pointsWrapper.le("create_time", end);
        pointsWrapper.eq("prize_type", 1);
        List<Object> pointsObjects = activityJkPrizeExchangeMapper.selectObjs(pointsWrapper);
        BigDecimal pointsCount = new BigDecimal(0);
        if (pointsObjects != null && !pointsObjects.isEmpty() && pointsObjects.get(0) != null) {
            pointsCount = (BigDecimal) pointsObjects.get(0);
        }
        resultObj.put("pointsCount", pointsCount);
        // 4. 统计现金发放总金额（prizeType=5）
        QueryWrapperX<ActivityJkPrizeExchangeDO> cashWrapper = new QueryWrapperX<>();
        cashWrapper.select("SUM(prize_value) as totalCash");
        cashWrapper.eq("activity_id", id);
        cashWrapper.ge("create_time", start);
        cashWrapper.le("create_time", end);
        cashWrapper.eq("claim_status", 2);
        cashWrapper.eq("prize_type", 5);
        cashWrapper.apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
        List<Object> cashObjects = activityJkPrizeExchangeMapper.selectObjs(cashWrapper);
        BigDecimal totalCash = new BigDecimal(0);
        if (cashObjects != null && !cashObjects.isEmpty() && cashObjects.get(0) != null) {
            totalCash = (BigDecimal) cashObjects.get(0);
        }
        // ES统计总浏览量 总访客量
        buildLuckyDrawResult(result, id, startTime, endTime, businessId);
        // ES统计分享人数
        buildShareResult(result, id, startTime, endTime, businessId);
        resultObj.putAll(result);
        resultObj.put("totalCash", totalCash);
        return resultObj;
    }

    @Override
    public Map<String, Object> getActivityJkDailyAnalysis(ActivityJkLogEventReqVO activityJkLogEventReqVO) {
        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Map<String, Long>> result = new HashMap<>();

        // 获取抽奖活动开始时间
        LocalDateTime startTime = activityJkLogEventReqVO.getStartTime();
        LocalDateTime endTime = activityJkLogEventReqVO.getEndTime();
        String id = activityJkLogEventReqVO.getId();

        // 若startTime与endTime均为空 默认最近30天
        if (startTime == null && endTime == null) {
            // 结束时间：当前时间的23:59:59
            endTime = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 忽略纳秒

            // 开始时间：30天前的00:00:00
            startTime = endTime.minusDays(29)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            if (startTime.isAfter(endTime)) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_ERROR);
            }

            // 3. 计算两个时间之间的完整天数差（忽略时分秒，按日期计算）
            // 例如：2025-09-25 23:59:59 到 2025-09-26 00:00:00 算1天
            long daysDiff = ChronoUnit.DAYS.between(startTime, endTime) + 1;
            if (daysDiff > 180) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_OUT_ERROR);
            }
        }

        buildLuckyDrawDailyResult(result, id, startTime, endTime, businessId);

        return convertStatsMapToArrays(result);
    }

    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<ActivityCardLogRespVO> getActivityJkLogList(ActivityJkLogPageReqVO activityJkLogPageReqVO) {
        Long id = activityJkLogPageReqVO.getId();
        ActivityDO activityDO = activityService.selectById(id);
        LambdaQueryWrapper<ActivityCardLogDO> activityCardLogDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(activityDO!=null){
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (start != null) {
                start = start.withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0); // 设置为当天的 00:00:00
            }

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0); // 清除纳秒，确保时间精确到秒
            }


            activityCardLogDOLambdaQueryWrapper.ge(ActivityCardLogDO::getCreateTime, start);
            activityCardLogDOLambdaQueryWrapper.le(ActivityCardLogDO::getCreateTime, end);
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getMemberName())) {
                activityCardLogDOLambdaQueryWrapper
                        .nested(wrapper -> wrapper.eq(ActivityCardLogDO::getMemberMobile, activityJkLogPageReqVO.getMemberName())
                        );
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getId())) {
                activityCardLogDOLambdaQueryWrapper.eq(ActivityCardLogDO::getActivityId, activityJkLogPageReqVO.getId());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getCardTypeList())) {
                activityCardLogDOLambdaQueryWrapper.in(ActivityCardLogDO::getCardType, activityJkLogPageReqVO.getCardTypeList());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStatusList())) {
                activityCardLogDOLambdaQueryWrapper.in(ActivityCardLogDO::getStatus, activityJkLogPageReqVO.getStatusList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStoreIdList())) {
                activityCardLogDOLambdaQueryWrapper.in(ActivityCardLogDO::getStoreId, activityJkLogPageReqVO.getStoreIdList());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStartTime())) {
                activityCardLogDOLambdaQueryWrapper.ge(ActivityCardLogDO::getCreateTime, activityJkLogPageReqVO.getStartTime());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getEndTime())) {
                activityCardLogDOLambdaQueryWrapper.le(ActivityCardLogDO::getCreateTime, activityJkLogPageReqVO.getEndTime());
            }

            activityCardLogDOLambdaQueryWrapper.orderBy(true, false, ActivityCardLogDO::getCreateTime, ActivityCardLogDO::getId);
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(activityJkLogPageReqVO.getPageNo());
            pageParam.setPageSize(activityJkLogPageReqVO.getPageSize());
            PageResult<ActivityCardLogDO> activityCardLogDOPageResult = activityCardLogMapper.selectPage(pageParam, activityCardLogDOLambdaQueryWrapper);
            PageResult<ActivityCardLogRespVO> result = BeanUtils.toBean(activityCardLogDOPageResult, ActivityCardLogRespVO.class);
            List<ActivityCardLogRespVO> list = result.getList();
            if (ObjectUtil.isNotEmpty(list)) {
                list.stream().forEach(log -> {
                    // 2. 根据 storeId 查询门店名称
                    CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(log.getStoreId());
                    StoreDTO data = storeByStoreId.getData();
                    if (data != null) {
                        log.setStoreName(data.getStoreName());
                    }

                });

            }



            return result;

        }
        PageResult<ActivityCardLogRespVO> objectPageResult = new PageResult<>();
        objectPageResult.setList(new ArrayList<>());
        objectPageResult.setTotal(0L);
        return objectPageResult;
    }

    @Override
    public PageResult<ActivityExchangePageRespVO> getExchangeLogList(ActivityJkExchangePageReqVO activityJkExchangePageReqVO) {
        Long id = activityJkExchangePageReqVO.getId();
        ActivityDO activityDO = activityService.selectById(id);
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> exchangeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(activityDO!=null){
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (start != null) {
                start = start.withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0); // 设置为当天的 00:00:00
            }

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0); // 清除纳秒，确保时间精确到秒
            }


            exchangeDOLambdaQueryWrapper.ge(ActivityJkPrizeExchangeDO::getCreateTime, start);
            exchangeDOLambdaQueryWrapper.le(ActivityJkPrizeExchangeDO::getCreateTime, end);
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getMemberName())) {
                exchangeDOLambdaQueryWrapper
                        .nested(wrapper -> wrapper
                                .eq(ActivityJkPrizeExchangeDO::getMemberMobile, activityJkExchangePageReqVO.getMemberName())
                        );
            }


            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getClaimStatus())) {
                Integer claimStatus = activityJkExchangePageReqVO.getClaimStatus();

                // 基础条件：奖品类型为5
                exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, 5);

                if (claimStatus.equals(3)) {
                    // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                    exchangeDOLambdaQueryWrapper.and(w ->
                            w.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 3)
                                    .or(w2 ->
                                            w2.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 2)
                                                    .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                    )
                    );
                } else if (claimStatus.equals(2)) {
                    // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                    exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 2)
                            .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
                } else if (claimStatus.equals(1)) {
                    // 查询状态为1的数据
                    exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 1);
                }
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getId())) {
                exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getActivityId, activityJkExchangePageReqVO.getId());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getPrizeType())) {
                exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, activityJkExchangePageReqVO.getPrizeType());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getReceiveAddressType())) {
                if (activityJkExchangePageReqVO.getReceiveAddressType().equals(0)) {
                    exchangeDOLambdaQueryWrapper.isNull(ActivityJkPrizeExchangeDO::getReceiveAddress);
                } else if (activityJkExchangePageReqVO.getReceiveAddressType().equals(1)) {
                    exchangeDOLambdaQueryWrapper.isNotNull(ActivityJkPrizeExchangeDO::getReceiveAddress);
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getTrackingType())) {
                if (activityJkExchangePageReqVO.getTrackingType().equals(0)) {
                    exchangeDOLambdaQueryWrapper.isNull(ActivityJkPrizeExchangeDO::getTrackingNumber);
                } else if (activityJkExchangePageReqVO.getTrackingType().equals(1)) {
                    exchangeDOLambdaQueryWrapper.isNotNull(ActivityJkPrizeExchangeDO::getTrackingNumber);
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStartTime())) {
                exchangeDOLambdaQueryWrapper.ge(ActivityJkPrizeExchangeDO::getCreateTime, activityJkExchangePageReqVO.getStartTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getEndTime())) {
                exchangeDOLambdaQueryWrapper.le(ActivityJkPrizeExchangeDO::getCreateTime, activityJkExchangePageReqVO.getEndTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStoreId())) {
                exchangeDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getStoreId, activityJkExchangePageReqVO.getStoreId());
            }

            exchangeDOLambdaQueryWrapper.orderBy(true, false, ActivityJkPrizeExchangeDO::getCreateTime, ActivityJkPrizeExchangeDO::getId);
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(activityJkExchangePageReqVO.getPageNo());
            pageParam.setPageSize(activityJkExchangePageReqVO.getPageSize());
            PageResult<ActivityJkPrizeExchangeDO> exchangeDOPageResult = activityJkPrizeExchangeMapper.selectPage(pageParam, exchangeDOLambdaQueryWrapper);
            List<ActivityJkPrizeExchangeDO> list = exchangeDOPageResult.getList();
            if (ObjectUtil.isNotEmpty(list)) {

                list.stream().forEach(log -> {
                    // 2. 根据 storeId 查询门店名称
                    CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(log.getStoreId());
                    StoreDTO data = storeByStoreId.getData();
                    if (data != null) {
                        log.setStoreName(data.getStoreName());
                    }

                });
                list.stream()
                        .filter(log -> log.getPrizeType() == 5 &&
                                log.getClaimStatus() == 2 &&
                                log.getUpdateTime() != null &&  // 确保updateTime不为空
                                Duration.between(log.getCreateTime(), log.getUpdateTime()).toHours() >= 24)
                        .forEach(log -> {
                            // 1. 设置状态为3
                            log.setClaimStatus(3);
                        });


            }
            PageResult<ActivityExchangePageRespVO> result = BeanUtils.toBean(exchangeDOPageResult, ActivityExchangePageRespVO.class);
            return result;

        }
        PageResult<ActivityExchangePageRespVO> objectPageResult = new PageResult<>();
        PageResult<ActivityExchangePageRespVO> pageList = objectPageResult.setList(new ArrayList<>());
        return pageList;
    }

    @Override
    public ActivityJkStatisticsRespVO getExchangeCount(ActivityJkExchangePageReqVO activityJkExchangePageReqVO) {
        ActivityJkStatisticsRespVO vo = new ActivityJkStatisticsRespVO();

        ActivityDO activityDO = activityService.selectById(activityJkExchangePageReqVO.getId());
        if(activityDO!=null){
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0);
            }


            QueryWrapper<ActivityJkPrizeExchangeDO> wrapper = new QueryWrapper<>();
            wrapper.select("COUNT(id) as totalRecords")
                    .eq("activity_id", activityJkExchangePageReqVO.getId())
                    .ge("create_time", start)
                    .le("create_time", end);



            // 添加 memberName 查询条件
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getMemberName())) {
                wrapper.and(queryWrapper -> queryWrapper
                        .like("member_name", activityJkExchangePageReqVO.getMemberName())
                        .or()
                        .like("member_mobile", activityJkExchangePageReqVO.getMemberName())
                );
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStoreId())) {
                wrapper.eq("store_id", activityJkExchangePageReqVO.getStoreId());
            }



            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getClaimStatus())) {
                Integer claimStatus = activityJkExchangePageReqVO.getClaimStatus();

                // 基础条件：奖品类型为5
                wrapper.eq("prize_type", 5);

                if (claimStatus.equals(3)) {
                    // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                    wrapper.and(w ->
                            w.eq("claim_status", 3)
                                    .or(w2 ->
                                            w2.eq("claim_status", 2)
                                                    .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                    )
                    );
                } else if (claimStatus.equals(2)) {
                    // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                    wrapper.eq("claim_status", 2)
                            .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
                } else if (claimStatus.equals(1)) {
                    // 查询状态为1的数据
                    wrapper.eq("claim_status", 1);
                }
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getPrizeType())) {
                wrapper.eq("prize_type", activityJkExchangePageReqVO.getPrizeType());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getReceiveAddressType())) {
                if (activityJkExchangePageReqVO.getReceiveAddressType().equals(0)) {
                    wrapper.isNull("receive_address");
                } else if (activityJkExchangePageReqVO.getReceiveAddressType().equals(1)) {
                    wrapper.isNotNull("receive_address");
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getTrackingType())) {
                if (activityJkExchangePageReqVO.getTrackingType().equals(0)) {
                    wrapper.isNull("tracking_number");
                } else if (activityJkExchangePageReqVO.getTrackingType().equals(1)) {
                    wrapper.isNotNull("tracking_number");
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStartTime())) {
                wrapper.ge("create_time", activityJkExchangePageReqVO.getStartTime());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getEndTime())) {
                wrapper.le("create_time", activityJkExchangePageReqVO.getEndTime());
            }


            // 先查询原始数据验证
            QueryWrapper<ActivityJkPrizeExchangeDO> detailWrapper = new QueryWrapper<>();
            detailWrapper.eq("activity_id", activityJkExchangePageReqVO.getId())
                    .ge("create_time", start)
                    .le("create_time", end)
                    .orderByAsc("member_id");

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStoreId())) {
                detailWrapper.eq("store_id", activityJkExchangePageReqVO.getStoreId());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getMemberName())) {
                detailWrapper.and(lotteryLogDOQueryWrapper -> lotteryLogDOQueryWrapper
                        .like("member_name", activityJkExchangePageReqVO.getMemberName())
                        .or()
                        .like("member_mobile", activityJkExchangePageReqVO.getMemberName())
                );
            }


            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getPrizeType())) {
                detailWrapper.eq("prize_type", activityJkExchangePageReqVO.getPrizeType());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getReceiveAddressType())) {
                if (activityJkExchangePageReqVO.getReceiveAddressType().equals(0)) {
                    detailWrapper.isNull("receive_address");
                } else if (activityJkExchangePageReqVO.getReceiveAddressType().equals(1)) {
                    detailWrapper.isNotNull("receive_address");
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getTrackingType())) {
                if (activityJkExchangePageReqVO.getTrackingType().equals(0)) {
                    detailWrapper.isNull("tracking_number");
                } else if (activityJkExchangePageReqVO.getTrackingType().equals(1)) {
                    detailWrapper.isNotNull("tracking_number");
                }
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getClaimStatus())) {
                Integer claimStatus = activityJkExchangePageReqVO.getClaimStatus();

                // 基础条件：奖品类型为5
                detailWrapper.eq("prize_type", 5);

                if (claimStatus.equals(3)) {
                    // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                    detailWrapper.and(w ->
                            w.eq("claim_status", 3)
                                    .or(w2 ->
                                            w2.eq("claim_status", 2)
                                                    .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                    )
                    );
                } else if (claimStatus.equals(2)) {
                    // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                    detailWrapper.eq("claim_status", 2)
                            .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
                } else if (claimStatus.equals(1)) {
                    // 查询状态为1的数据
                    detailWrapper.eq("claim_status", 1);
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getStartTime())) {
                detailWrapper.ge("create_time", activityJkExchangePageReqVO.getStartTime());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangePageReqVO.getEndTime())) {
                detailWrapper.le("create_time", activityJkExchangePageReqVO.getEndTime());
            }

            List<ActivityJkPrizeExchangeDO> detailList = activityJkPrizeExchangeMapper.selectList(detailWrapper);

            // 手动统计独立用户数
            Set<Long> distinctMembers = detailList.stream()
                    .map(ActivityJkPrizeExchangeDO::getMemberId)
                    .collect(Collectors.toSet());
            Long participationCount = Long.valueOf(distinctMembers.size());

            vo.setParticipation(participationCount);


            // 执行聚合查询
            List<Map<String, Object>> resultMaps = activityJkPrizeExchangeMapper.selectMaps(wrapper);

            resultMaps.stream()
                    .map(map -> {
                        vo.setCount((Long) map.get("totalRecords"));
                        return vo;
                    })
                    .collect(Collectors.toList());

            return vo;
        }


        return null;
    }

    @Override
    public Integer updateExpress(ActivityExchangeReqVO activityExchangeReqVO) {
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getId, activityExchangeReqVO.getId());
        logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getMemberId, activityExchangeReqVO.getMemberId());
        int result = activityJkPrizeExchangeMapper.update(BeanUtils.toBean(activityExchangeReqVO, ActivityJkPrizeExchangeDO.class), logDOLambdaQueryWrapper);
        return result;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void exportActivityJkLog(ActivityJkLogExportReqVO activityJkLogExportReqVO, HttpServletRequest request, HttpServletResponse response) {
        Long businessId = BusinessContextHolder.getBusinessId();


        // 构建查询条件
        LambdaQueryWrapper<ActivityCardLogDO> queryWrapper = buildLogQueryWrapper(activityJkLogExportReqVO, businessId);

        // 设置分页参数，每页1万条
        int pageSize = 10000;
        Page<ActivityCardLogExportRespVO> pageParam = new Page<>(1, pageSize);

        // 生成统计数据（第二个Sheet的内容）
        List<ActivityLogStatisticsVO> statistics = generateEnhancedStatistics(activityJkLogExportReqVO);

        // 使用新的双Sheet导出方法
        exportReqVOExcelActionService.exportAsyncExcel(
                ActivityCardLogExportRespVO.class,
                pageParam,
                (Page<ActivityCardLogExportRespVO> param) -> getCardLogData(param, queryWrapper),
                buildExportFileName(activityJkLogExportReqVO),
                statistics,
                false
        );
    }

    /**
     * 构建导出文件名
     */
    private String buildExportFileName(ActivityJkLogExportReqVO activityJkLogExportReqVO) {
        StringBuilder fileName = new StringBuilder("抽卡记录");

        if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getId())) {
            fileName.append("_活动").append(activityJkLogExportReqVO.getId());
        }

        // 添加时间戳
        fileName.append("_").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return fileName.toString();
    }

    /**
     * 增强的统计方法（添加标题和格式）
     */
    private List<ActivityLogStatisticsVO> generateEnhancedStatistics(ActivityJkLogExportReqVO activityJkLogExportReqVO) {
        List<ActivityLogStatisticsVO> statistics = new ArrayList<>();

        try {
            // 1. 添加报表标题行（第一行：大标题）
            ActivityLogStatisticsVO title = new ActivityLogStatisticsVO();
            title.setStoreName("【集卡活动门店统计报表】");
            statistics.add(title);

            // 2. 添加活动信息行（第二行）
            if (activityJkLogExportReqVO.getId() != null) {
                ActivityDO activityDO = activityService.selectById(activityJkLogExportReqVO.getId());
                if (activityDO != null) {
                    ActivityLogStatisticsVO activityInfo = new ActivityLogStatisticsVO();
                    activityInfo.setStoreName("活动名称: " + activityDO.getActivityName());
                    statistics.add(activityInfo);
                }
            }

            // 3. 添加空行（第三行）
            statistics.add(new ActivityLogStatisticsVO());

            // 4. 添加表头行（第四行）
            ActivityLogStatisticsVO header = new ActivityLogStatisticsVO();
            header.setStoreName("门店名称");
            header.setParticipantsCount("参与人数");  // 表头标识
            header.setCount("集卡次数");       // 表头标识
            statistics.add(header);

            // 5. 获取门店分组统计数据
            List<ActivityStoreStatisticsDTO> storeStats = getStoreStatistics(activityJkLogExportReqVO);

            // 6. 添加门店数据行
            int totalParticipants = 0;
            int totalLotteryCount = 0;
//
            for (ActivityStoreStatisticsDTO stat : storeStats) {
                String storeName = getStoreNameById(stat.getStoreId());

                ActivityLogStatisticsVO vo = new ActivityLogStatisticsVO();
                vo.setStoreName(storeName);
                vo.setParticipantsCount(stat.getParticipantsCount());
                vo.setCount(stat.getCount());
                statistics.add(vo);
                String participantsCount = stat.getParticipantsCount();
                Integer numParticipantsCount = Integer.valueOf(participantsCount);
                String count = stat.getCount();
                Integer numLotteryCount = Integer.valueOf(count);
                totalParticipants += numParticipantsCount;
                totalLotteryCount += numLotteryCount;
            }


            // 10. 添加生成时间
            statistics.add(new ActivityLogStatisticsVO());
            ActivityLogStatisticsVO timeRow = new ActivityLogStatisticsVO();
            timeRow.setStoreName("生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            statistics.add(timeRow);


        } catch (Exception e) {
            log.error("生成统计报表失败", e);
            // 返回简单的错误信息
            ActivityLogStatisticsVO error = new ActivityLogStatisticsVO();
            error.setStoreName("统计生成失败: " + e.getMessage());
            statistics.add(error);
        }

        return statistics;
    }

    // 新增：获取门店名称的方法
    private String getStoreNameById(Long storeId) {
        if (storeId == null || storeId == 0) {
            return "未知门店";
        }

        try {
            CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(storeId);
            StoreDTO data = storeByStoreId.getData();
            if (data != null) {
                return data.getStoreName();
            } else {
                return "未知门店";
            }
        } catch (Exception e) {
            log.warn("查询门店名称失败，storeId: {}", storeId, e);
            return "未知门店";
        }
    }

    private List<ActivityStoreStatisticsDTO> getStoreStatistics(ActivityJkLogExportReqVO activityJkLogExportReqVO) {
        try {
            ActivityDO activityDO = activityService.selectById(activityJkLogExportReqVO.getId());
            LambdaQueryWrapper<ActivityCardLogDO> rawWrapper = new LambdaQueryWrapper<>();
            if (activityDO != null) {
                Date startDate = activityDO.getStartDate();
                Date endDate = activityDO.getEndDate();
                LocalDateTime start = convertDateToLocalDateTime(startDate);
                LocalDateTime end = convertDateToLocalDateTime(endDate);

                if (end != null) {
                    end = end.withHour(23)
                            .withMinute(59)
                            .withSecond(59)
                            .withNano(0); // 清除纳秒，确保时间精确到秒
                }


                rawWrapper.ge(ActivityCardLogDO::getCreateTime, start);
                rawWrapper.le(ActivityCardLogDO::getCreateTime, end);
                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getMemberName())) {
                    rawWrapper
                            .nested(wrapper -> wrapper
                                    .like(ActivityCardLogDO::getMemberName, activityJkLogExportReqVO.getMemberName())
                                    .or()
                                    .like(ActivityCardLogDO::getMemberMobile, activityJkLogExportReqVO.getMemberName())
                            );
                }

                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getId())) {
                    rawWrapper.eq(ActivityCardLogDO::getActivityId, activityJkLogExportReqVO.getId());
                }
                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getCardTypeList())) {
                    rawWrapper.in(ActivityCardLogDO::getCardType, activityJkLogExportReqVO.getCardTypeList());
                }
                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStatusList())) {
                    rawWrapper.in(ActivityCardLogDO::getStatus, activityJkLogExportReqVO.getStatusList());
                }

                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStoreIdList())) {
                    rawWrapper.in(ActivityCardLogDO::getStoreId, activityJkLogExportReqVO.getStoreIdList());
                }
                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStartTime())) {
                    rawWrapper.ge(ActivityCardLogDO::getCreateTime, activityJkLogExportReqVO.getStartTime());
                }
                if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getEndTime())) {
                    rawWrapper.le(ActivityCardLogDO::getCreateTime, activityJkLogExportReqVO.getEndTime());
                }


                List<ActivityCardLogDO> rawData = activityCardLogMapper.selectList(rawWrapper);

                // 手动分组统计
                Map<Long, ActivityStoreStatisticsDTO> resultMap = new HashMap<>();
                Map<Long, Set<Long>> storeMemberMap = new HashMap<>();

                log.info("开始处理{}条原始数据", rawData.size());

                for (ActivityCardLogDO record : rawData) {
                    Long storeId = record.getStoreId();
                    Long memberId = record.getMemberId();

                    if (storeId == null || storeId <= 0) continue;

                    log.debug("处理记录: storeId={}, memberId={}", storeId, memberId);

                    // 统计抽奖次数
                    ActivityStoreStatisticsDTO dto = resultMap.computeIfAbsent(storeId, k -> {
                        ActivityStoreStatisticsDTO newDto = new ActivityStoreStatisticsDTO();
                        newDto.setStoreId(storeId);
                        newDto.setCount("0");
                        newDto.setParticipantsCount("0");
                        return newDto;
                    });

                    // 更新抽奖次数
                    int currentCount = Integer.parseInt(dto.getCount());
                    dto.setCount(String.valueOf(currentCount + 1));

                    // 收集会员ID用于去重统计
                    if (memberId != null) {
                        storeMemberMap.computeIfAbsent(storeId, k -> new HashSet<>())
                                .add(memberId);
                    }
                }

                // 设置参与人数
                storeMemberMap.forEach((storeId, memberSet) -> {
                    ActivityStoreStatisticsDTO dto = resultMap.get(storeId);
                    if (dto != null) {
                        dto.setParticipantsCount(String.valueOf(memberSet.size()));
                    }
                });

                List<ActivityStoreStatisticsDTO> result = new ArrayList<>(resultMap.values());

                log.info("手动分组统计完成，共{}个门店", result.size());
                result.forEach(dto -> {
                    log.info("门店统计: storeId={}, participants={}, lotteryCount={}",
                            dto.getStoreId(), dto.getParticipantsCount(), dto.getCount());
                });

                return result;
            } else {
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("手动统计异常", e);
            return new ArrayList<>();
        }
    }

    private List<ActivityCardLogExportRespVO> getCardLogData(Page<ActivityCardLogExportRespVO> pageParam, LambdaQueryWrapper<ActivityCardLogDO> queryWrapper) {
        // 创建DO的分页参数
        Page<ActivityCardLogDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        // 通过Service方法调用，确保@DS注解生效
        Page<ActivityCardLogDO> resultPage = activityLogQueryService.selectPage(currentPageParam, queryWrapper);
        List<ActivityCardLogDO> logList = resultPage.getRecords();

        // 转换为响应对象
        List<ActivityCardLogExportRespVO> respList = new ArrayList<>(logList.size());
        for (ActivityCardLogDO activityCardLogDO : logList) {
            ActivityCardLogExportRespVO activityCardLogExportRespVO = new ActivityCardLogExportRespVO();
            BeanUtils.copyProperties(activityCardLogDO, activityCardLogExportRespVO);
            String cardTypeToName = convertCardTypeToName(activityCardLogDO.getCardType());
            String statusName = convertStatusToName(activityCardLogDO.getStatus());
            if (ObjectUtil.isNotEmpty(activityCardLogDO.getStoreId())) {
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(activityCardLogDO.getStoreId());
                StoreDTO data = storeByStoreId.getData();
                if (data != null) {
                    activityCardLogExportRespVO.setStoreName(data.getStoreName());
                }
            }
            activityCardLogExportRespVO.setCardTypeName(cardTypeToName);
            activityCardLogExportRespVO.setCardStatus(statusName);
            respList.add(activityCardLogExportRespVO);
        }

        return respList;
    }

    private String convertCardTypeToName(Integer cardType) {
        if (cardType == null) {
            return "";
        }
        switch (cardType) {
            case 1:
                return "兜底卡";
            case 2:
                return "套系卡";
            case 3:
                return "万能卡";
            case 4:
                return "隐藏卡";
            default:
                return "";
        }
    }

    private String convertStatusToName(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "未使用";
            case 1:
                return "已使用";
            default:
                return "";
        }
    }

    private LambdaQueryWrapper<ActivityCardLogDO> buildLogQueryWrapper(ActivityJkLogExportReqVO activityJkLogExportReqVO, Long businessId) {
        LambdaQueryWrapper<ActivityCardLogDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();

        ActivityDO activityDO = activityService.selectById(activityJkLogExportReqVO.getId());
        if (activityDO != null) {

            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (start != null) {
                start = start.withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0); // 设置为当天的 00:00:00
            }

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0); // 清除纳秒，确保时间精确到秒
            }


            logDOLambdaQueryWrapper.ge(ActivityCardLogDO::getCreateTime, start);
            logDOLambdaQueryWrapper.le(ActivityCardLogDO::getCreateTime, end);
            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getMemberName())) {
                logDOLambdaQueryWrapper
                        .nested(wrapper -> wrapper
                                .like(ActivityCardLogDO::getMemberName, activityJkLogExportReqVO.getMemberName())
                                .or()
                                .like(ActivityCardLogDO::getMemberMobile, activityJkLogExportReqVO.getMemberName())
                        );
            }

            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getId())) {
                logDOLambdaQueryWrapper.eq(ActivityCardLogDO::getActivityId, activityJkLogExportReqVO.getId());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getCardTypeList())) {
                logDOLambdaQueryWrapper.in(ActivityCardLogDO::getCardType, activityJkLogExportReqVO.getCardTypeList());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStatusList())) {
                logDOLambdaQueryWrapper.in(ActivityCardLogDO::getStatus, activityJkLogExportReqVO.getStatusList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStoreIdList())) {
                logDOLambdaQueryWrapper.in(ActivityCardLogDO::getStoreId, activityJkLogExportReqVO.getStoreIdList());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getStartTime())) {
                logDOLambdaQueryWrapper.ge(ActivityCardLogDO::getCreateTime, activityJkLogExportReqVO.getStartTime());
            }
            if (ObjectUtil.isNotEmpty(activityJkLogExportReqVO.getEndTime())) {
                logDOLambdaQueryWrapper.le(ActivityCardLogDO::getCreateTime, activityJkLogExportReqVO.getEndTime());
            }

            logDOLambdaQueryWrapper.orderBy(true, false, ActivityCardLogDO::getCreateTime, ActivityCardLogDO::getId);

        }

        return logDOLambdaQueryWrapper;
    }

    @Override
    public void exportExchangeLog(ActivityJkExchangeExchangeReqVO activityJkExchangeExchangeReqVO, HttpServletRequest request, HttpServletResponse response) {
        Long businessId = BusinessContextHolder.getBusinessId();


        // 构建查询条件
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> queryWrapper = buildExchangeWrapper(activityJkExchangeExchangeReqVO, businessId);

        // 设置分页参数，每页1万条
        int pageSize = 10000;
        Page<ActivityJkExchangeExportRespVO> pageParam = new Page<>(1, pageSize);
        

        exportReqVOExcelActionService.exportAsyncExcel(
                ActivityJkExchangeExportRespVO.class,
                pageParam,
                (Page<ActivityJkExchangeExportRespVO> param) -> getExchangeData(param, queryWrapper),
                buildExchangeExportFileName(activityJkExchangeExchangeReqVO)
        );
    }

    private List<ActivityJkExchangeExportRespVO> getExchangeData(Page<ActivityJkExchangeExportRespVO> pageParam, LambdaQueryWrapper<ActivityJkPrizeExchangeDO> queryWrapper) {
        // 创建DO的分页参数
        Page<ActivityJkPrizeExchangeDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        // 通过Service方法调用，确保@DS注解生效
        Page<ActivityJkPrizeExchangeDO> resultPage = activityJkPrizeExchangeMapper.selectPage(currentPageParam, queryWrapper);
        List<ActivityJkPrizeExchangeDO> logList = resultPage.getRecords();

        // 转换为响应对象
        List<ActivityJkExchangeExportRespVO> respList = new ArrayList<>(logList.size());
        for (ActivityJkPrizeExchangeDO activityJkPrizeExchangeDO : logList) {
            ActivityJkExchangeExportRespVO activityJkExchangeExportRespVO = new ActivityJkExchangeExportRespVO();
            BeanUtils.copyProperties(activityJkPrizeExchangeDO, activityJkExchangeExportRespVO);
            String prizeTypeToName = convertPrizeTypeToName(activityJkPrizeExchangeDO.getPrizeType());

            if (ObjectUtil.isNotEmpty(activityJkPrizeExchangeDO.getPrizeType())) {
                if (activityJkPrizeExchangeDO.getPrizeType() == 5) {
                    if (activityJkPrizeExchangeDO.getClaimStatus().equals(1)) {
                        activityJkExchangeExportRespVO.setClaimName("未领取");
                    } else if (activityJkPrizeExchangeDO.getClaimStatus().equals(2)) {
                        // 检查时间字段是否为空
                        if (activityJkPrizeExchangeDO.getCreateTime() != null && activityJkPrizeExchangeDO.getUpdateTime() != null) {
                            // 计算时间差（小时）
                            long hoursBetween = Duration.between(activityJkPrizeExchangeDO.getCreateTime(), activityJkPrizeExchangeDO.getUpdateTime()).toHours();

                            if (hoursBetween >= 24) {
                                activityJkExchangeExportRespVO.setClaimName("已失效");
                            } else {
                                activityJkExchangeExportRespVO.setClaimName("已领取");
                            }
                        } else {
                            // 如果时间为空，按已领取处理或根据业务需求处理
                            activityJkExchangeExportRespVO.setClaimName("已领取");
                        }
                    }else if (activityJkPrizeExchangeDO.getClaimStatus().equals(3)) {
                        activityJkExchangeExportRespVO.setClaimName("已失效");
                    }
                }
            }
            activityJkExchangeExportRespVO.setPrizeTypeName(prizeTypeToName);
            if(activityJkPrizeExchangeDO.getMemberId()!=null){
                activityJkExchangeExportRespVO.setMemberId(activityJkPrizeExchangeDO.getMemberId().toString());
            }

            Long storeId = activityJkPrizeExchangeDO.getStoreId();
            if (storeId != null && storeId != 0L) {
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(storeId);
                StoreDTO data = storeByStoreId.getData();
                if (data != null) {
                    activityJkExchangeExportRespVO.setStoreName(data.getStoreName());
                }
            }
            respList.add(activityJkExchangeExportRespVO);
        }

        return respList;
    }

    private String convertPrizeTypeToName(Integer prizeType) {
        if (prizeType == null) {
            return "";
        }
        switch (prizeType) {
            case 1:
                return "积分";
            case 2:
                return "优惠卷";
            case 3:
                return "优惠卷包";
            case 4:
                return "实物";
            case 5:
                return "现金红包";
            default:
                return "";
        }
    }

    private String buildExchangeExportFileName(ActivityJkExchangeExchangeReqVO activityJkExchangeExchangeReqVO) {
        StringBuilder fileName = new StringBuilder("兑换记录");

        if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getId())) {
            fileName.append("_活动").append(activityJkExchangeExchangeReqVO.getId());
        }

        // 添加时间戳
        fileName.append("_").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return fileName.toString();
    }

    private LambdaQueryWrapper<ActivityJkPrizeExchangeDO> buildExchangeWrapper(ActivityJkExchangeExchangeReqVO activityJkExchangeExchangeReqVO, Long businessId) {
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();

        ActivityDO activityDO = activityService.selectById(activityJkExchangeExchangeReqVO.getId());
        if (activityDO != null) {


            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (start != null) {
                start = start.withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0); // 设置为当天的 00:00:00
            }

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0); // 清除纳秒，确保时间精确到秒
            }


            logDOLambdaQueryWrapper.ge(ActivityJkPrizeExchangeDO::getCreateTime, start);
            logDOLambdaQueryWrapper.le(ActivityJkPrizeExchangeDO::getCreateTime, end);
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getMemberName())) {
                logDOLambdaQueryWrapper
                        .nested(wrapper -> wrapper
                                .like(ActivityJkPrizeExchangeDO::getMemberName, activityJkExchangeExchangeReqVO.getMemberName())
                                .or()
                                .like(ActivityJkPrizeExchangeDO::getMemberMobile, activityJkExchangeExchangeReqVO.getMemberName())
                        );
            }


            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getClaimStatus())) {
                Integer claimStatus = activityJkExchangeExchangeReqVO.getClaimStatus();

                // 基础条件：奖品类型为5
                logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, 5);

                if (claimStatus.equals(3)) {
                    // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                    logDOLambdaQueryWrapper.and(w ->
                            w.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 3)
                                    .or(w2 ->
                                            w2.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 2)
                                                    .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                    )
                    );
                } else if (claimStatus.equals(2)) {
                    // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                    logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 2)
                            .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
                } else if (claimStatus.equals(1)) {
                    // 查询状态为1的数据
                    logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getClaimStatus, 1);
                }
            }

            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getId())) {
                logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getActivityId, activityJkExchangeExchangeReqVO.getId());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getStoreId())) {
                logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getStoreId, activityJkExchangeExchangeReqVO.getStoreId());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getPrizeType())) {
                logDOLambdaQueryWrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, activityJkExchangeExchangeReqVO.getPrizeType());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getReceiveAddressType())) {
                if (activityJkExchangeExchangeReqVO.getReceiveAddressType().equals(0)) {
                    logDOLambdaQueryWrapper.isNull(ActivityJkPrizeExchangeDO::getReceiveAddress);
                } else if (activityJkExchangeExchangeReqVO.getReceiveAddressType().equals(1)) {
                    logDOLambdaQueryWrapper.isNotNull(ActivityJkPrizeExchangeDO::getReceiveAddress);
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getTrackingType())) {
                if (activityJkExchangeExchangeReqVO.getTrackingType().equals(0)) {
                    logDOLambdaQueryWrapper.isNull(ActivityJkPrizeExchangeDO::getTrackingNumber);
                } else if (activityJkExchangeExchangeReqVO.getTrackingType().equals(1)) {
                    logDOLambdaQueryWrapper.isNotNull(ActivityJkPrizeExchangeDO::getTrackingNumber);
                }
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getStartTime())) {
                logDOLambdaQueryWrapper.ge(ActivityJkPrizeExchangeDO::getCreateTime, activityJkExchangeExchangeReqVO.getStartTime());
            }
            if (ObjectUtil.isNotEmpty(activityJkExchangeExchangeReqVO.getEndTime())) {
                logDOLambdaQueryWrapper.le(ActivityJkPrizeExchangeDO::getCreateTime, activityJkExchangeExchangeReqVO.getEndTime());
            }

            return logDOLambdaQueryWrapper;

        }
        return logDOLambdaQueryWrapper;
    }

    @Override
    public ActivityJkSpreadRespVO selectSpread(Long id) {
        ActivityJkSpreadRespVO activityJkSpreadRespVO = new ActivityJkSpreadRespVO();


        LambdaQueryWrapper<ActivityJkDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityJkDO::getActivityId, id);
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(wrapper);

        BeanUtils.copyProperties(activityJkDO, activityJkSpreadRespVO);

        List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityId(id);
        if (!activityChannelDOList.isEmpty()) {
            List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();

            activityChannelDOList.forEach(activityChannelDO -> {
                ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
                BeanUtils.copyProperties(activityChannelDO, activityChannelRespVO);
                activityChannelRespVOS.add(activityChannelRespVO);
            });

            activityJkSpreadRespVO.setActivityChannelRespVOS(activityChannelRespVOS);
        }
        activityJkSpreadRespVO.setId(id);
        return activityJkSpreadRespVO;
    }

    @Override
    public List<ActivityJkReqVO> getActivityJkList() {
        return null;
    }

    @Override
    public void updateSpread(ActivityJkSpreadSaveReqVO activityJkSpreadSaveReqVO) {

        LambdaUpdateWrapper<ActivityJkDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityJkDO::getActivityId, activityJkSpreadSaveReqVO.getId());
        updateWrapper.set(ActivityJkDO::getShareTitle, activityJkSpreadSaveReqVO.getShareTitle());
        updateWrapper.set(ActivityJkDO::getShareImgUrl, activityJkSpreadSaveReqVO.getShareImgUrl());
        updateWrapper.set(ActivityJkDO::getShareNote, activityJkSpreadSaveReqVO.getShareNote());
        activityJkMapper.update(updateWrapper);
        int type = ActivityChannelTypeEnum.CARD.getCode();

        //新建并修改推广链接
//        activityChannelService.createAndUpdateChannel(activityJkSpreadSaveReqVO.getActivityChannelList(), activityJkSpreadSaveReqVO.getId(), type);

        List<ActivityJkDO> activityJkDOList = activityJkMapper.selectList(new LambdaQueryWrapperX<ActivityJkDO>()
                .eq(ActivityJkDO::getDeleted, false).eq(ActivityJkDO::getActivityId, activityJkSpreadSaveReqVO.getId()));


        if (CollectionUtil.isNotEmpty(activityJkDOList)) {
            ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO = addCache(activityJkDOList.get(0).getId());
            lotteryRedisDAO.setCacheObject(JK_SETTING + activityJkDOList.get(0).getId(), saveOrUpdateReqVO);
        }
    }

    @Override
    public Integer updateState(ActivityJkStateReqVO stateReqVO) {
        ActivityDO activityDO = activityService.selectById(stateReqVO.getId());


        LambdaQueryWrapper<ActivityJkDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJkDO::getActivityId, stateReqVO.getId());
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(queryWrapper);
        ActivityJkSaveOrUpdateReqVO activityJkSaveOrUpdateReqVO = new ActivityJkSaveOrUpdateReqVO();
        if (activityDO != null) {
            activityDO.setIsEnabled(stateReqVO.getIsEnabled());
            BeanUtils.copyProperties(activityDO, activityJkSaveOrUpdateReqVO);
            BeanUtils.copyProperties(activityJkDO, activityJkSaveOrUpdateReqVO);
            activityJkSaveOrUpdateReqVO.setIsEnabled(stateReqVO.getIsEnabled());



            if (activityJkDO.getPlaceOrderProduct().equals(2)) {
                LambdaQueryWrapper<ActivityJkCommodityDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ActivityJkCommodityDO::getActivityId, stateReqVO.getId());
                List<ActivityJkCommodityDO> commodityDOS = activityJkCommodityService.list(wrapper);
                if (ObjectUtil.isNotEmpty(commodityDOS)) {
                    List<Long> commodityIds = commodityDOS.stream().map(ma -> ma.getCommodityId()).collect(Collectors.toList());
                    activityJkSaveOrUpdateReqVO.setCommodityIds(commodityIds);
                } else {
                    activityJkSaveOrUpdateReqVO.setCommodityIds(new ArrayList<>());
                }

            } else {
                activityJkSaveOrUpdateReqVO.setCommodityIds(new ArrayList<>());
            }

            //设置门店信息
            if (activityDO.getActivityStore().equals(0)) {
                LambdaQueryWrapper<ActivityStoreDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                storeDOLambdaQueryWrapper.eq(ActivityStoreDO::getActivityId, stateReqVO.getId());
                List<ActivityStoreDO> storeDOS = activityStoreService.list(storeDOLambdaQueryWrapper);

                if (ObjectUtil.isNotEmpty(storeDOS)) {
                    activityJkSaveOrUpdateReqVO.setStoreIds(storeDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList()));
                } else {
                    activityJkSaveOrUpdateReqVO.setStoreIds(new ArrayList<>());
                }


            } else {
                activityJkSaveOrUpdateReqVO.setStoreIds(new ArrayList<>());
            }


        }

        lotteryRedisDAO.setCacheObject(JK_SETTING + stateReqVO.getId(), activityJkSaveOrUpdateReqVO);
        activityService.updateStatus(stateReqVO.getId(), stateReqVO.getIsEnabled());
        return 1;
    }

    //组装缓存
    public ActivityJkSaveOrUpdateReqVO addCache(Long id) {
        ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO = new ActivityJkSaveOrUpdateReqVO();
        // 抽奖设置主体
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(new LambdaQueryWrapperX<ActivityJkDO>()
                .eq(ActivityJkDO::getId, id));

        if (activityJkDO != null) {
            // 复制抽奖设置主体属性到缓存对象
            BeanUtils.copyProperties(activityJkDO, saveOrUpdateReqVO);

            // 活动主体
            ActivityDO activityDO = activityService.selectById(activityJkDO.getActivityId());
            if (activityDO != null) {
                // 复制活动主体属性到缓存对象
                BeanUtils.copyProperties(activityDO, saveOrUpdateReqVO);

                // 处理指定门店
                if (activityDO.getActivityStore() == 0) {
                    List<StoreInfoDTO> storeInfoDTOS = activityStoreService.storesByActivityId(activityDO.getId());
                    if (CollectionUtil.isNotEmpty(storeInfoDTOS)) {
                        List<Long> storeIds = storeInfoDTOS.stream()
                                .map(StoreInfoDTO::getStoreId)
                                .collect(Collectors.toList());
                        saveOrUpdateReqVO.setStoreIds(storeIds);
                    }
                }

                // 处理指定商品
                if (activityJkDO.getPlaceOrderProduct() == 2) {
                    List<CommodityDTO> activityProductDTOS = activityJkCommodityService.listByActivityId(activityDO.getId());
                    if (CollectionUtil.isNotEmpty(activityProductDTOS)) {
                        List<Long> commodityIds = activityProductDTOS.stream()
                                .map(CommodityDTO::getCommodityId)
                                .collect(Collectors.toList());
                        saveOrUpdateReqVO.setCommodityIds(commodityIds);
                    }
                }

            }
        }

        return saveOrUpdateReqVO;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public ActivityJkStatisticsRespVO getActivityJkLogCount(ActivityJkLogPageReqVO activityJkLogPageReqVO) {
        ActivityJkStatisticsRespVO vo = new ActivityJkStatisticsRespVO();

        ActivityDO activityDO = activityService.selectById(activityJkLogPageReqVO.getId());
        if(activityDO!=null){
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            LocalDateTime start = convertDateToLocalDateTime(startDate);
            LocalDateTime end = convertDateToLocalDateTime(endDate);

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0);
            }


            QueryWrapper<ActivityCardLogDO> wrapper = new QueryWrapper<>();
            wrapper.select("COUNT(id) as totalRecords")
                    .eq("activity_id", activityJkLogPageReqVO.getId())
                    .ge("create_time", start)
                    .le("create_time", end);



            // 添加 memberName 查询条件
            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getMemberName())) {
                wrapper.and(queryWrapper -> queryWrapper
                        .like("member_name", activityJkLogPageReqVO.getMemberName())
                        .or()
                        .like("member_mobile", activityJkLogPageReqVO.getMemberName())
                );
            }


            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getCardTypeList())) {
                wrapper.in("card_type", activityJkLogPageReqVO.getCardTypeList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStartTime())) {
                wrapper.ge("create_time", activityJkLogPageReqVO.getStartTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getEndTime())) {
                wrapper.le("create_time", activityJkLogPageReqVO.getEndTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStatusList())) {
                wrapper.in("status", activityJkLogPageReqVO.getStatusList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStoreIdList())) {
                wrapper.in("store_id", activityJkLogPageReqVO.getStoreIdList());
            }


            // 先查询原始数据验证
            QueryWrapper<ActivityCardLogDO> detailWrapper = new QueryWrapper<>();
            detailWrapper.eq("activity_id", activityJkLogPageReqVO.getId())
                    .ge("create_time", start)
                    .le("create_time", end)
                    .orderByAsc("member_id");


            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getMemberName())) {
                detailWrapper.and(lotteryLogDOQueryWrapper -> lotteryLogDOQueryWrapper
                        .like("member_name", activityJkLogPageReqVO.getMemberName())
                        .or()
                        .like("member_mobile", activityJkLogPageReqVO.getMemberName())
                );
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getCardTypeList())) {
                detailWrapper.in("card_type", activityJkLogPageReqVO.getCardTypeList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStartTime())) {
                detailWrapper.ge("create_time", activityJkLogPageReqVO.getStartTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getEndTime())) {
                detailWrapper.le("create_time", activityJkLogPageReqVO.getEndTime());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStatusList())) {
                detailWrapper.in("status", activityJkLogPageReqVO.getStatusList());
            }

            if (ObjectUtil.isNotEmpty(activityJkLogPageReqVO.getStoreIdList())) {
                detailWrapper.in("store_id", activityJkLogPageReqVO.getStoreIdList());
            }

            List<ActivityCardLogDO> detailList = activityCardLogMapper.selectList(detailWrapper);

            // 手动统计独立用户数
            Set<Long> distinctMembers = detailList.stream()
                    .map(ActivityCardLogDO::getMemberId)
                    .collect(Collectors.toSet());
            Long participationCount = Long.valueOf(distinctMembers.size());

            vo.setParticipation(participationCount);


            // 执行聚合查询
            List<Map<String, Object>> resultMaps = activityCardLogMapper.selectMaps(wrapper);
            System.out.println("聚合查询结果: " + resultMaps);

            resultMaps.stream()
                    .map(map -> {
                        vo.setCount((Long) map.get("totalRecords"));
                        return vo;
                    })
                    .collect(Collectors.toList());

            return vo;
        }


        return vo;
    }


    /**
     * 逗号分隔字符串
     * @param list
     * @return
     */
    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }


    /**
     * 检查保底卡片数量
     * @param saveOrUpdateReqVO
     * @return
     */
    private boolean validateGuaranteedPrize(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        long count = saveOrUpdateReqVO.getCardReqVOList().stream()
                .filter(item -> item.getIsGuarantees() == 1)
                .count();
        return count == 1;
    }

    /**
     * 检查总概率
     * @param saveOrUpdateReqVO
     * @return
     */
    private boolean validateTotalProbability(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO) {
        BigDecimal totalProbability = saveOrUpdateReqVO.getCardReqVOList().stream()
                .map(ActivityJkCardReqVO::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalProbability.compareTo(new BigDecimal("100")) == 0;
    }


    /**
     * 新增时添加默认短链
     */
    private void createChannelDO(Long activityId, int type) {
        activityChannelService.createChannelDO(activityId, type);
    }

    private void createActivityJDDO(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO, Long activityId) {
        ActivityJkDO activityJkDO = new ActivityJkDO();
        BeanUtils.copyProperties(saveOrUpdateReqVO, activityJkDO);
        activityJkDO.setActivityId(activityId);
        activityJkDO.setShareNote(shareDescription);
        activityJkDO.setShareTitle(shareTitle);
        activityJkDO.setShareImgUrl(shareImageUrl);
        activityJkMapper.insert(activityJkDO);
    }

    /**
     * Date 转 LocalDateTime（指定业务时区，避免时区误差）
     *
     * @param date 待转换的 Date 对象（null 时返回 null）
     * @return LocalDateTime 转换后的本地时间
     */
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        if (date == null) {
            return null; // 处理 null，避免空指针
        }
        // 步骤：Date -> Instant -> ZonedDateTime（指定时区）-> LocalDateTime
        return Instant.ofEpochMilli(date.getTime())
                .atZone(BUSINESS_TIME_ZONE)
                .toLocalDateTime();
    }

    /**
     * 转换统计数据为包含数组的Map
     *
     * @param statsMap 原始统计数据，结构为:
     *                 外层Key: 日期(yyyy-MM-dd)
     *                 内层Map: Key为指标名("pv"或"uv")，Value为指标值
     * @return 转换后的Map，包含三个键:
     * - "times": 日期数组(String[])
     * - "pvValues": PV数值数组(Long[])
     * - "uvValues": UV数值数组(Long[])
     * @throws IllegalArgumentException 当原始数据为空或格式错误时抛出
     */
    public Map<String, Object> convertStatsMapToArrays(Map<String, Map<String, Long>> statsMap) {
        // 校验原始数据非空
        if (statsMap == null || statsMap.isEmpty()) {
            throw new IllegalArgumentException("原始统计数据 Map 不能为空");
        }

        // 提取外层日期并排序
        List<String> dateList = new ArrayList<>(statsMap.keySet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期顺序排序
        dateList.sort((dateStr1, dateStr2) -> {
            try {
                LocalDate date1 = LocalDate.parse(dateStr1, dateFormatter);
                LocalDate date2 = LocalDate.parse(dateStr2, dateFormatter);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "日期格式错误，需为 yyyy-MM-dd：" + dateStr1 + " / " + dateStr2, e);
            }
        });

        // 初始化目标数组
        int dataSize = dateList.size();
        String[] dateArray = new String[dataSize];
        Long[] uvArray = new Long[dataSize];
        Long[] pvArray = new Long[dataSize];

        // 填充数组数据
        for (int i = 0; i < dataSize; i++) {
            String currentDate = dateList.get(i);
            Map<String, Long> innerMap = statsMap.get(currentDate);

            // 校验内层Map非空
            if (innerMap == null) {
                throw new IllegalArgumentException(
                        "日期 " + currentDate + " 对应的内层指标 Map 不能为空");
            }

            // 填充日期数组
            dateArray[i] = currentDate;

            // 填充UV数组，缺失时补0
            uvArray[i] = innerMap.getOrDefault("uv", 0L);

            // 填充PV数组，缺失时补0
            pvArray[i] = innerMap.getOrDefault("pv", 0L);
        }

        // 构建结果Map
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("times", dateArray);
        resultMap.put("pvValues", pvArray);
        resultMap.put("uvValues", uvArray);

        return resultMap;
    }

    private void buildLuckyDrawResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        // 抽奖设置主体
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(new LambdaQueryWrapperX<ActivityJkDO>()
                .and(wrapper -> wrapper
                        .eq(ActivityJkDO::getId, id)
                        .or()
                        .eq(ActivityJkDO::getActivityId, id)
                ));
        // 初始化时直接用空HashMap兜底，避免后续赋值null
        Map<String, Long> stringLongActivityMap = new HashMap<>();
        Map<String, Long> stringLongMap = new HashMap<>(); // 先初始化为空Map，防止null

        if (eventService != null) {
            stringLongMap = eventService.statPvUv(EventType.POINT_DRAW, id, start, end, businessId);
            if (stringLongMap == null) {
                stringLongMap = new HashMap<>();
            }
            if (activityJkDO != null && activityJkDO.getActivityId() != null) {
                String activityId = activityJkDO.getActivityId().toString();
                Map<String, Long> tempActivityMap = eventService.statPvUv(EventType.POINT_DRAW, activityId, start, end, businessId);
                stringLongActivityMap = tempActivityMap != null ? tempActivityMap : new HashMap<>();
            }
        }
        Map<String, Long> totalMap = getStringLongMap(stringLongMap, stringLongActivityMap);
        result.putAll(totalMap);
    }

    private static Map<String, Long> getStringLongMap(Map<String, Long> stringLongMap, Map<String, Long> stringLongActivityMap) {

        Map<String, Long> map1 = stringLongMap == null ? new HashMap<>() : stringLongMap;
        Map<String, Long> map2 = stringLongActivityMap == null ? new HashMap<>() : stringLongActivityMap;

        Map<String, Long> totalMap = new HashMap<>();
        // 遍历第一个Map，值为null则替换为0L
        for (Map.Entry<String, Long> entry : map1.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue() == null ? 0L : entry.getValue();
            totalMap.put(key, value);
        }

        // 遍历第二个Map，累加数值（Key不存在则取0L）
        for (Map.Entry<String, Long> entry : map2.entrySet()) {
            String key = entry.getKey();
            Long activityValue = entry.getValue() == null ? 0L : entry.getValue();
            Long totalValue = totalMap.getOrDefault(key, 0L) + activityValue;
            totalMap.put(key, totalValue);
        }
        return totalMap;
    }


    private void buildLuckyDrawDailyResult(Map<String, Map<String, Long>> result, String id, LocalDateTime start, LocalDateTime end, Long businessId) {
        // 抽奖设置主体
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(new LambdaQueryWrapperX<ActivityJkDO>()
                .and(wrapper -> wrapper
                        .eq(ActivityJkDO::getId, id)
                        .or()
                        .eq(ActivityJkDO::getActivityId, id)
                ));
        Map<String, Map<String, Long>> stringLongMap = eventService.statDailyPvUv(EventType.POINT_DRAW, id, start, end, businessId);
        if (stringLongMap == null) {
            stringLongMap = new HashMap<>();
        }
        if (activityJkDO != null && activityJkDO.getActivityId() != null) {
            // 关键修正：第二个查询应该用activityId而非原id，否则逻辑无意义
            Map<String, Map<String, Long>> stringActivityLongMap = eventService.statDailyPvUv(
                    EventType.POINT_DRAW,
                    activityJkDO.getActivityId().toString(),
                    start,
                    end,
                    businessId
            );

            if (stringActivityLongMap != null && !stringActivityLongMap.isEmpty()) {
                mergeNestedMap(stringLongMap, stringActivityLongMap);
            }
        }

        if (result == null) {
            result = new HashMap<>();
        }
        result.putAll(stringLongMap);
    }
    private void mergeNestedMap(Map<String, Map<String, Long>> targetMap, Map<String, Map<String, Long>> sourceMap) {
        for (Map.Entry<String, Map<String, Long>> sourceEntry : sourceMap.entrySet()) {
            String outerKey = sourceEntry.getKey();
            Map<String, Long> sourceInnerMap = sourceEntry.getValue();

            if (!targetMap.containsKey(outerKey)) {
                targetMap.put(outerKey, new HashMap<>(sourceInnerMap));
                continue;
            }

            Map<String, Long> targetInnerMap = targetMap.get(outerKey);
            if (targetInnerMap == null) {
                targetInnerMap = new HashMap<>();
                targetMap.put(outerKey, targetInnerMap);
            }

            for (Map.Entry<String, Long> innerEntry : sourceInnerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                Long sourceValue = innerEntry.getValue() == null ? 0L : innerEntry.getValue();

                targetInnerMap.put(
                        innerKey,
                        targetInnerMap.getOrDefault(innerKey, 0L) + sourceValue
                );
            }
        }
    }

    private void buildShareResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {

        // 抽奖设置主体
        ActivityJkDO activityJkDO = activityJkMapper.selectOne(new LambdaQueryWrapperX<ActivityJkDO>()
                .and(wrapper -> wrapper
                        .eq(ActivityJkDO::getId, id)
                        .or()
                        .eq(ActivityJkDO::getActivityId, id)
                ));

        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        Long activeCount = 0L;
        Long count = eventService.statUv(EventType.POINT_SHARE, id, start, end, businessId);
        if (activityJkDO != null) {
            activeCount = eventService.statUv(EventType.POINT_SHARE, activityJkDO.getActivityId().toString(), start, end, businessId);
        }


        result.put("shareCount", count + activeCount);
    }

    /**
     * 校验只有一个万能卡和一个隐藏卡方法
     */
    private void verificationData(List<ActivityJkCardReqVO> cardReqVOList){
        if (ObjectUtil.isEmpty(cardReqVOList)) {
            return;
        }

        // 万能卡类型
        long count = cardReqVOList.stream()
                .filter(vo -> vo.getCardType() != null && vo.getCardType().equals(3))
                .count();

        // 隐藏卡数量
        long cardCount = cardReqVOList.stream()
                .filter(vo -> vo.getCardType() != null && vo.getCardType().equals(4))
                .count();

        if (count > 1) {
            throw exception(JK_ALL_CARD);
        }

        if (cardCount > 1) {
            throw exception(JK_HIDE_CARD);
        }
    }



}
