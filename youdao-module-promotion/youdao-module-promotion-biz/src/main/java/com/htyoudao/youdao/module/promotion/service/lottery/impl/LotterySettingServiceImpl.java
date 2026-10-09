package com.htyoudao.youdao.module.promotion.service.lottery.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCrowdRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertisingImage.AdvertisingImageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.LotteryRedisDAO;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.LotteryStateEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityLotteryCommodity.ActivityLotteryCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryPrizeService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotterySettingService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryCachePublisher;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryLedger;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryRuntimeLifecycle;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryScopeService;
import com.htyoudao.youdao.module.promotion.util.ConvertUtil;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.TagValueDTO;
import com.htyoudao.youdao.module.system.api.tag.TagApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Service
@Slf4j
public class LotterySettingServiceImpl implements LotterySettingService {
    @Resource
    private LotteryRuntimeLifecycle lotteryRuntime;
    @Resource
    private LotteryLedger lotteryLedger;
    @Resource
    private LotteryScopeService lotteryScopeService;
    @Resource
    private LotteryCachePublisher lotteryCachePublisher;

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;

    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;

    @Resource
    private LotteryLogMapper lotteryLogMapper;

    @Resource
    private LotteryPrizeService lotteryPrizeService;





    @Resource
    private LotteryRedisDAO lotteryRedisDAO;
    @Resource
    public RedisCache redisCache;

    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private ActivityStoreTagMapper activityStoreTagMapper;

    @Resource
    private ActivityLotteryCommodityService activityLotteryCommodityService;


    @Resource
    RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    public static String APPLET_AD = "APPLET_AD";
    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityMapper activityMapper;


    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private TagApi tagApi;


    @DubboReference
    private CrowdApi crowdApi;


    @Resource
    private ActivityChannelService activityChannelService;

    @Resource
    private AdvertisingImageMapper advertisingImageMapper;

    @Override
    @DSTransactional
    public PageResult<LotterySettingsRespVO> getLotterySettingsListPage(Integer pageNum, Integer pageSize, LotterySettingsReqVO lotterySettingsVO) {
        LambdaQueryWrapper<LotterySettingsDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        logDOLambdaQueryWrapper.orderBy(true, false, LotterySettingsDO::getCreateTime, LotterySettingsDO::getId);
        if (lotterySettingsVO.getId() != null) {
            logDOLambdaQueryWrapper.like(LotterySettingsDO::getId, lotterySettingsVO.getId());
        }
        if (StringUtils.isNotBlank(lotterySettingsVO.getLotteryTitle())) {
            logDOLambdaQueryWrapper.like(LotterySettingsDO::getLotteryTitle, lotterySettingsVO.getLotteryTitle());
        }

        if (lotterySettingsVO.getState() != null) {
            logDOLambdaQueryWrapper.eq(LotterySettingsDO::getState, lotterySettingsVO.getState());
        }

        if (lotterySettingsVO.getLotteryType() != null) {
            logDOLambdaQueryWrapper.eq(LotterySettingsDO::getLotteryType, lotterySettingsVO.getLotteryType());
        }

        if (!ObjectUtil.isEmpty(lotterySettingsVO.getLotteryStartTime())) {
            logDOLambdaQueryWrapper.ge(LotterySettingsDO::getLotteryStartTime, lotterySettingsVO.getLotteryStartTime());
        }
        if (!ObjectUtil.isEmpty(lotterySettingsVO.getLotteryEndTime())) {
            logDOLambdaQueryWrapper.le(LotterySettingsDO::getLotteryEndTime, lotterySettingsVO.getLotteryEndTime());
        }
        PageParam page = new PageParam();
        page.setPageNo(pageNum);
        page.setPageSize(pageSize);
        PageResult<LotterySettingsDO> settingsPage = lotterySettingsMapper.selectPage(page, logDOLambdaQueryWrapper);
        PageResult<LotterySettingsRespVO> result = BeanUtils.toBean(settingsPage, LotterySettingsRespVO.class);
        fillLotteryScope(result.getList());
        return result;
    }

    /** 分页批量回填范围、标签名称及当前命中的门店。 */
    private void fillLotteryScope(List<LotterySettingsRespVO> rows) {
        if (rows == null || rows.isEmpty()) return;
        List<Long> activityIds = rows.stream().map(LotterySettingsRespVO::getActivityId)
                .filter(Objects::nonNull).distinct().toList();
        if (activityIds.isEmpty()) return;
        long businessId = BusinessContextHolder.getRequiredBusinessId();
        Map<Long, ActivityDO> activities = activityMapper.selectList(new LambdaQueryWrapper<ActivityDO>()
                        .eq(ActivityDO::getBusinessId, businessId).in(ActivityDO::getId, activityIds))
                .stream().collect(Collectors.toMap(ActivityDO::getId, Function.identity()));
        Map<Long, List<Long>> tagsByActivity = activityStoreTagMapper.selectList(
                        new LambdaQueryWrapper<ActivityStoreTagDO>().eq(ActivityStoreTagDO::getBusinessId, businessId)
                                .in(ActivityStoreTagDO::getActivityId, activityIds))
                .stream().collect(Collectors.groupingBy(ActivityStoreTagDO::getActivityId,
                        Collectors.mapping(ActivityStoreTagDO::getTagId, Collectors.toList())));
        Map<Long, List<Long>> storesByActivity = activityStoreService.list(
                        new LambdaQueryWrapper<ActivityStoreDO>().eq(ActivityStoreDO::getBusinessId, businessId)
                                .in(ActivityStoreDO::getActivityId, activityIds))
                .stream().collect(Collectors.groupingBy(ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())));
        List<Long> tagIds = tagsByActivity.values().stream().flatMap(List::stream).distinct().toList();
        Map<Long, String> tagNames = tagNames(tagIds);
        List<Long> storeIds = storesByActivity.values().stream().flatMap(List::stream).distinct().toList();
        Map<Long, StoreInfoDTO> storesById = storeInfo(storeIds);
        for (LotterySettingsRespVO row : rows) {
            ActivityDO activity = activities.get(row.getActivityId());
            if (activity == null) continue;
            row.setAppScope(Optional.ofNullable(activity.getAppScope()).orElse(0));
            row.setActivityStore(activity.getActivityStore());
            List<Long> selectedTags = tagsByActivity.getOrDefault(activity.getId(), List.of()).stream()
                    .distinct().sorted().toList();
            row.setTagIds(selectedTags);
            row.setTagInfoDTOS(tagInfo(selectedTags, tagNames));
            if (Objects.equals(activity.getActivityStore(), 0)) {
                List<Long> selectedStores = storesByActivity.getOrDefault(activity.getId(), List.of()).stream()
                        .distinct().sorted().toList();
                row.setStoreIds(selectedStores);
                row.setStoreInfoDTOS(selectedStores.stream().map(storesById::get)
                        .filter(Objects::nonNull).toList());
            }
        }
    }

    private List<TagValueDTO> tagInfo(List<Long> ids, Map<Long, String> names) {
        return ids.stream().map(id -> {
            TagValueDTO tag = new TagValueDTO();
            tag.setId(id);
            tag.setName(names.get(id));
            return tag;
        }).toList();
    }

    /** 名称服务暂不可用时仍返回标签 ID，避免管理端列表和详情整体失败。 */
    private Map<Long, String> tagNames(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        try {
            Map<Long, String> names = tagApi.getNamesByIds(ids);
            return names == null ? Map.of() : names;
        } catch (RuntimeException e) {
            log.warn("抽奖适用标签名称查询失败，标签ID={}", ids, e);
            return Map.of();
        }
    }

    /** 门店资料暂不可用时保留关系表中的门店 ID。 */
    private Map<Long, StoreInfoDTO> storeInfo(List<Long> ids) {
        Map<Long, StoreInfoDTO> result = new HashMap<>();
        for (Long id : ids) {
            StoreInfoDTO store = new StoreInfoDTO();
            store.setStoreId(id);
            result.put(id, store);
        }
        if (ids.isEmpty()) return result;
        try {
            List<StoreInfoDTO> stores = storeApi.getStoresByStoreIds(ids).getCheckedData();
            if (stores != null) for (StoreInfoDTO store : stores) {
                if (store != null && store.getStoreId() != null) result.put(store.getStoreId(), store);
            }
        } catch (RuntimeException e) {
            log.warn("抽奖适用门店名称查询失败，门店数量={}", ids.size(), e);
        }
        return result;
    }

    @Override
    @LogRecord(type = SYSTEM_LOTTERY_SETTING_TYPE, subType = SYSTEM_LOTTERY_SETTING_ADD_TYPE, bizNo = "1", success = SYSTEM_LOTTERY_SETTING_ADD_SUCCESS)
    public CommonResult<Integer> addLotteryPrize(LotterySettingsReqVO lotterySettingsVO) {
        // 判断奖品存在唯一兜底商品
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsVO)) {
            return error(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
        if (!validateTotalProbability(lotterySettingsVO)) {
            return error(LOTTERY_WINNING_PROBABILITY);
        }
        LotterySettingsDO lotterySettings = BeanUtils.toBean(lotterySettingsVO, LotterySettingsDO.class);

        lotterySettings.setCreateTime(LocalDateTime.now());
        lotterySettings.setUpdateTime(LocalDateTime.now());
        String userName = SecurityFrameworkUtils.getLoginUsername();
        lotterySettings.setCreateUserName(userName);
        lotterySettings.setUpdateUserName(userName);
        lotterySettings.setUpdateTime(LocalDateTime.now());
//        lotterySettings.setIsDelete(0);
        lotterySettingsMapper.insert(lotterySettings);

        int type = switch (lotterySettings.getLotteryType()) {
            case 1 -> ActivityChannelTypeEnum.TURNTABLE.getCode();
            case 3 -> ActivityChannelTypeEnum.FORTUNE.getCode();
            default -> 0; // 原代码默认值
        };

        createChannelDO(lotterySettings.getId(), type);

        List<LotteryPrizeDO> list = lotterySettingsVO.getPrizes().stream().map(item -> {
                    LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                    prize.setCreateUserName(userName);
                    prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                    // todo 获取用户userName
                    prize.setUpdateUserName(userName);
                    prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                    prize.setIsDelete(0);
                    prize.setLotteryId(lotterySettings.getId());
                    return prize;
                })
                .collect(Collectors.toList());
        //添加活动缓存
        lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + lotterySettings.getId(), lotterySettings);

        lotteryPrizeMapper.insertBatch(list);
        //添加奖品缓存
        syncPrizeRecordAndInventoryByLogs(lotterySettings.getId(), lotterySettings.getActivityId(), lotterySettings.getPrizePoolRules());
        // 记录操作日志上下文
        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsVO);
        return success(null);
    }


    @Override
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE, bizNo = "{{#lotterySettingsVO.id}}", success = SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE_SUCCESS)
    public CommonResult<Integer> updateLotteryState(LotterySettingsReqVO lotterySettingsVO) {
        LotterySettingsDO settings = lotterySettingsMapper.selectById(lotterySettingsVO.getId());
        if (settings == null) throw exception(LOTTERY_NOT_NULL);
        if (settings.getActivityId() == null) {
            // 旧添加接口可创建未关联活动主表的配置，仍沿用原状态修改方式。
            settings.setState(lotterySettingsVO.getState());
            settings.setUpdateTime(LocalDateTime.now());
            settings.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
            lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + settings.getId(), settings);
            lotterySettingsMapper.updateById(settings);
        } else if (!lotteryRuntime.changeState(settings.getActivityId(), lotterySettingsVO.getState())) {
            throw exception(LOTTERY_NOT_NULL);
        }
        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsVO);
        return success(null);
    }

    @Override
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_DELETE_TYPE, bizNo = "{{#deleteID}}", success = SYSTEM_LOTTERY_LOG_DELETE_TYPE_SUCCESS)
    public CommonResult<Integer> deleteLottery(long id) {
        lotteryRuntime.rejectLegacyWrite(id);
        LambdaQueryWrapper<LotterySettingsDO> lotterySettingsDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotterySettingsDOLambdaQueryWrapper.eq(LotterySettingsDO::getId, id);
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(lotterySettingsDOLambdaQueryWrapper);
        if (lotterySettings.getState() == 1) {
            return error(LOTTERY_DELETE);
        }

        lotterySettingsMapper.delete(lotterySettingsDOLambdaQueryWrapper);
        LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, id);
        lotteryPrizeMapper.delete(lotteryPrizeDOLambdaQueryWrapper);
        lotteryRedisDAO.deleteObject(LOTTERY_SETTING + lotterySettings.getId());
        lotteryRedisDAO.deleteObject(LOTTERY_NUM);
        lotteryRedisDAO.deleteObject(LOTTERY_PRIZE + lotterySettings.getId());
        lotteryRedisDAO.deleteObject(PRIZE_NUM);
        // 记录操作日志上下文
        LogRecordContext.putVariable("deleteID", id);
        return success(null);
    }

    @Override
    public LotterySettingsDetailRespVO getLotteryDetails(long id) {
        LambdaQueryWrapper<LotterySettingsDO> lotterySettingsDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotterySettingsDOLambdaQueryWrapper.eq(LotterySettingsDO::getId, id);
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(lotterySettingsDOLambdaQueryWrapper);
        LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, id);
        List<LotteryPrizeDO> list = lotteryPrizeMapper.selectList(lotteryPrizeDOLambdaQueryWrapper);
        if (Objects.equals(lotterySettings.getRuntimeVersion(), 2))
            lotteryLedger.fillDisplayStock(BusinessContextHolder.getRequiredBusinessId(),
                    lotterySettings.getActivityId(), lotterySettings.getStockEpoch(), list);
        LotterySettingsDetailRespVO lotterySettingsRespVO = BeanUtils.toBean(lotterySettings, LotterySettingsDetailRespVO.class);
        fillLotteryScope(List.of(lotterySettingsRespVO));
        List<LotteryPrizeRespVO> prizeRespVOList = list.stream().map(LotteryPrizeRespVO::new).collect(Collectors.toList());
        lotterySettingsRespVO.setPrizes(prizeRespVOList);
        return lotterySettingsRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_UPDATE_TYPE, bizNo = "{{#lotterySettingsVO.id}}", success = SYSTEM_LOTTERY_LOG_UPDATE_TYPE_SUCCESS)
    public CommonResult<Integer> updateLottery(LotterySettingsReqVO lotterySettingsVO) {
        lotteryRuntime.rejectLegacyWrite(lotterySettingsVO.getId());
        // 判断奖品存在唯一兜底商品
        LambdaQueryWrapper<LotterySettingsDO> lotterySettingsDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotterySettingsDOLambdaQueryWrapper.eq(LotterySettingsDO::getId, lotterySettingsVO.getId());
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(lotterySettingsDOLambdaQueryWrapper);
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsVO)) {
            return error(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
        if (!validateTotalProbability(lotterySettingsVO)) {
            return error(LOTTERY_WINNING_PROBABILITY);
        }
        BeanUtils.copyProperties(lotterySettingsVO, lotterySettings);
        lotterySettings.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
        lotterySettings.setUpdateTime(null);
        lotterySettingsMapper.updateById(lotterySettings);
        lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + lotterySettings.getId(), lotterySettings);
        List<LotteryPrizeDO> list = lotterySettingsVO.getPrizes().stream().map(item -> {
                    LotteryPrizeDO prize = new LotteryPrizeDO();
                    BeanUtils.copyProperties(item, prize);
                    prize.setLotteryId(lotterySettings.getId());
                    prize.setCreateUserName(SecurityFrameworkUtils.getLoginUsername());
                    prize.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
                    prize.setLotteryId(lotterySettings.getId());
                    return prize;
                })
                .collect(Collectors.toList());
        LambdaQueryWrapper<LotteryPrizeDO> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettings.getId());
        lotteryPrizeMapper.delete(delWrapper);
        lotteryRedisDAO.deleteObject(LOTTERY_PRIZE + lotterySettings.getId());
        lotteryPrizeMapper.insertBatch(list);
        syncPrizeRecordAndInventoryByLogs(lotterySettings.getId(), lotterySettings.getActivityId(), lotterySettings.getPrizePoolRules());
        // 记录操作日志上下文
        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsVO);
        return success(null);
    }

//    @Override
//    @Transactional
//    @LogRecord(type = SYSTEM_LOTTERY_SETTING_TYPE, subType = SYSTEM_LOTTERY_SETTING_ADD_TYPE, bizNo = "1", success = SYSTEM_LOTTERY_SETTING_ADD_SUCCESS)
//    public CommonResult<Integer> saveLottery(LotterySettingsAddReqVO lotterySettingsAddVO) {
//        //新增主表
//        ActivityDO activityDO = new ActivityDO();
//        BeanUtils.copyProperties(lotterySettingsAddVO, activityDO);
//        activityDO.setWeekNumbers(convertListToString(lotterySettingsAddVO.getWeekNumberList()));
//        activityDO.setDayNumbers(convertListToString(lotterySettingsAddVO.getDayNumberList()));
//        activityDO.setTimeRange(convertListToString(lotterySettingsAddVO.getTimeRangeList()));
//        activityDO.setSelectedGroups(convertListToString(lotterySettingsAddVO.getSelectedGroupList()));
//        activityDO.setActivityType(ActivityTypeEnum.LOTTERY.getCode());
//        activityDO.setActivityRemark(lotterySettingsAddVO.getActivityRemark());
//        activityDO.setActivityName(lotterySettingsAddVO.getActivityName());
//        activityDO.setIsEnabled(0);
//        Long activityId = activityService.createActivity(activityDO);
//
//
//        //新增门店列表
//        if (ObjectUtil.isNotEmpty(lotterySettingsAddVO.getStoreIds())) {
//            activityStoreService.createBatch(lotterySettingsAddVO.getStoreIds(), activityId);
//        }
//
//        if (ObjectUtil.isNotEmpty(lotterySettingsAddVO.getCommodityIds())) {
//            activityLotteryCommodityService.createBatch(lotterySettingsAddVO.getCommodityIds(), activityId);
//        }
//
//        // 判断奖品存在唯一兜底商品
//        // 检查保底商品数量
//        if (!validateGuaranteedPrize(lotterySettingsAddVO)) {
//            throw exception(LOTTERY_GUARANTEED_PRODUCTS);
//        }
//        // 检查总概率
//        if (!validateTotalProbability(lotterySettingsAddVO)) {
//            return error(LOTTERY_WINNING_PROBABILITY);
//        }
//
//        if (lotterySettingsAddVO.getParticipantGroup().equals(5)) {
//            if (lotterySettingsAddVO.getSelectedGroupList().size() <= 0) {
//                throw exception(LOTTERY_CROWD_NOT_NULL);
//            }
//        }
//        LotterySettingsDO lotterySettings = BeanUtils.toBean(lotterySettingsAddVO, LotterySettingsDO.class);
//        lotterySettings.setLotteryStartTime(lotterySettingsAddVO.getStartDate());
//        lotterySettings.setLotteryEndTime(lotterySettingsAddVO.getEndDate());
//        lotterySettings.setLotteryTitle(lotterySettingsAddVO.getActivityName());
//        lotterySettings.setActivityId(activityId);
//        lotterySettings.setLotteryRule(lotterySettingsAddVO.getActivityRules());
//        lotterySettings.setState(0);
//        lotterySettings.setCreateTime(LocalDateTime.now());
//        lotterySettings.setUpdateTime(LocalDateTime.now());
//        String userName = SecurityFrameworkUtils.getLoginUsername();
//        lotterySettings.setCreateUserName(userName);
//        lotterySettings.setUpdateUserName(userName);
//        lotterySettings.setUpdateTime(LocalDateTime.now());
//        lotterySettingsMapper.insert(lotterySettings);
//
//        int type = switch (lotterySettings.getLotteryType()) {
//            case 1 -> ActivityChannelTypeEnum.TURNTABLE.getCode();
//            case 3 -> ActivityChannelTypeEnum.FORTUNE.getCode();
//            default -> 0; // 原代码默认值
//        };
//
//        createChannelDO(activityId, type);
//        if(lotterySettingsAddVO.getPrizePoolRules().equals(2)){
//            if(lotterySettingsAddVO.getActivityStore().equals(0)){
//                if(ObjectUtil.isNotEmpty(lotterySettingsAddVO.getStoreIds())){
//                    for (Long storeId : lotterySettingsAddVO.getStoreIds()) {
//                        List<LotteryPrizeDO> list = new ArrayList<>();
//                        List<LotteryPrizeReqVO> prizes = lotterySettingsAddVO.getPrizes();
//
//                        for (int i = 0; i < prizes.size(); i++) {
//                            LotteryPrizeReqVO item = prizes.get(i);
//                            LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
//
//                            // 生成唯一code: 原code + 门店ID + 索引
//                            String uniqueCode = storeId + "_" + i;
//                            prize.setCode(uniqueCode);
//
//                            if(ObjectUtil.isNotEmpty(item.getWinningCitys())){
//                                prize.setWinningCitys(convertListToString(item.getWinningCitys()));
//                            }else{
//                                prize.setWinningCitys(null);
//                            }
//                            prize.setStoreId(storeId);
//                            prize.setCreateUserName(userName);
//                            prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                            prize.setUpdateUserName(userName);
//                            prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                            prize.setLotteryId(lotterySettings.getId());
//                            list.add(prize);
//                        }
//
//                        lotteryPrizeMapper.insertBatch(list);
//                        lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotterySettings.getId() + ":store:" + storeId, list);
//                    }
//                }
//            }else if(lotterySettingsAddVO.getActivityStore().equals(1)){
//                CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
//                List<StoreInfoDTO> data = allStoreList.getData();
//                if(ObjectUtil.isNotEmpty(data)){
//                    for (StoreInfoDTO storeInfoDTO : data) {
//                        List<LotteryPrizeDO> list = new ArrayList<>();
//                        List<LotteryPrizeReqVO> prizes = lotterySettingsAddVO.getPrizes();
//
//                        for (int i = 0; i < prizes.size(); i++) {
//                            LotteryPrizeReqVO item = prizes.get(i);
//                            LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
//
//                            // 生成唯一code: 原code + 门店ID + 索引
//                            String uniqueCode = storeInfoDTO.getStoreId() + "_" + i;
//                            prize.setCode(uniqueCode);
//
//                            if(ObjectUtil.isNotEmpty(item.getWinningCitys())){
//                                prize.setWinningCitys(convertListToString(item.getWinningCitys()));
//                            }else{
//                                prize.setWinningCitys(null);
//                            }
//                            prize.setStoreId(storeInfoDTO.getStoreId());
//                            prize.setCreateUserName(userName);
//                            prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                            prize.setUpdateUserName(userName);
//                            prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                            prize.setLotteryId(lotterySettings.getId());
//                            list.add(prize);
//                        }
//
//                        lotteryPrizeMapper.insertBatch(list);
//                        lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotterySettings.getId() + ":store:" + storeInfoDTO.getStoreId(), list);
//                    }
//                }
//            }
//        }else{
//            // 非门店模式的逻辑保持不变
//            List<LotteryPrizeDO> list = lotterySettingsAddVO.getPrizes().stream().map(item -> {
//                        LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
//                        if(ObjectUtil.isNotEmpty(item.getWinningCitys())){
//                            prize.setWinningCitys(convertListToString(item.getWinningCitys()));
//                        }else{
//                            prize.setWinningCitys(null);
//                        }
//                        prize.setCreateUserName(userName);
//                        prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                        prize.setUpdateUserName(userName);
//                        prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                        prize.setLotteryId(lotterySettings.getId());
//                        return prize;
//                    })
//                    .collect(Collectors.toList());
//
//            lotteryPrizeMapper.insertBatch(list);
//            lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotterySettings.getId(), list);
//        }
//        lotterySettingsAddVO.setId(lotterySettings.getId());
//        lotterySettingsAddVO.setActivityId(activityId);
//        LotterySettingsCacheDataVO lotterySettingsCacheDataVO = new LotterySettingsCacheDataVO();
//        BeanUtils.copyProperties(lotterySettingsAddVO,lotterySettingsCacheDataVO);
//        //添加活动缓存
//        lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + lotterySettings.getId(), lotterySettingsCacheDataVO);
//
//
//        // 记录操作日志上下文
//        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsAddVO);
//        return success(1);
//    }



    @Override
    @Transactional
    @LogRecord(type = SYSTEM_LOTTERY_SETTING_TYPE, subType = SYSTEM_LOTTERY_SETTING_ADD_TYPE, bizNo = "1", success = SYSTEM_LOTTERY_SETTING_ADD_SUCCESS)
    public CommonResult<Integer> saveLottery(LotterySettingsAddReqVO lotterySettingsAddVO) {
        if(ObjectUtil.isNotEmpty(lotterySettingsAddVO.getTimeRangeList())){
            boolean b = validateTimeRanges(lotterySettingsAddVO.getTimeRangeList());
            if(!b){
                throw exception(LOTTERY_SESSION_ERROR);
            }
        }
        if (lotterySettingsAddVO.getParticipantGroup().equals(5)) {
            if (lotterySettingsAddVO.getSelectedGroupList().size() <= 0) {
                throw exception(LOTTERY_CROWD_NOT_NULL);
            }
        }
        // 新增主表
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(lotterySettingsAddVO, activityDO);
        activityDO.setAppScope(lotterySettingsAddVO.getAppScope());
        activityDO.setWeekNumbers(convertListToString(lotterySettingsAddVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(lotterySettingsAddVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(lotterySettingsAddVO.getTimeRangeList()));
        activityDO.setSelectedGroups(convertListToString(lotterySettingsAddVO.getSelectedGroupList()));
        activityDO.setActivityType(ActivityTypeEnum.LOTTERY.getCode());
        activityDO.setActivityRemark(lotterySettingsAddVO.getActivityRemark());
        activityDO.setActivityName(lotterySettingsAddVO.getActivityName());
        activityDO.setIsEnabled(0);
        Long activityId = activityService.createActivity(activityDO);

        List<Long> scopeStoreIds = lotteryScopeService.replace(activityDO,
                lotterySettingsAddVO.getTagIds(), lotterySettingsAddVO.getStoreIds());
        activityService.updateActivity(activityDO);

        if (ObjectUtil.isNotEmpty(lotterySettingsAddVO.getCommodityIds())) {
            activityLotteryCommodityService.createBatch(lotterySettingsAddVO.getCommodityIds(), activityId);
        }

        // 判断奖品存在唯一兜底商品
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsAddVO)) {
            throw exception(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
//        if (!validateTotalProbability(lotterySettingsAddVO)) {
//            return error(LOTTERY_WINNING_PROBABILITY);
//        }



        LotterySettingsDO lotterySettings = BeanUtils.toBean(lotterySettingsAddVO, LotterySettingsDO.class);
        lotterySettings.setLotteryStartTime(lotterySettingsAddVO.getStartDate());
        lotterySettings.setLotteryEndTime(lotterySettingsAddVO.getEndDate());
        lotterySettings.setLotteryTitle(lotterySettingsAddVO.getActivityName());
        lotterySettings.setActivityId(activityId);
        lotterySettings.setLotteryRule(lotterySettingsAddVO.getActivityRules());
        // 新活动启用持久化账本，旧活动保留迁移标记直到重新启用。
        lotterySettings.setRuntimeVersion(2);
        lotterySettings.setStockEpoch(1L);
        lotterySettings.setConfigVersion(1L);
        lotterySettings.setState(0);
        lotterySettings.setCreateTime(LocalDateTime.now());
        lotterySettings.setUpdateTime(LocalDateTime.now());
        String userName = SecurityFrameworkUtils.getLoginUsername();
        lotterySettings.setCreateUserName(userName);
        lotterySettings.setUpdateUserName(userName);
        lotterySettings.setUpdateTime(LocalDateTime.now());
        lotterySettingsMapper.insert(lotterySettings);

        int type = switch (lotterySettings.getLotteryType()) {
            case 1 -> ActivityChannelTypeEnum.TURNTABLE.getCode();
            case 3 -> ActivityChannelTypeEnum.FORTUNE.getCode();
            default -> 0; // 原代码默认值
        };

        createChannelDO(activityId, type);

        lotteryScopeService.syncPrizes(lotterySettings, lotterySettingsAddVO.getPrizes(), scopeStoreIds, null);

        // 记录操作日志上下文
        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsAddVO);
        return success(1);
    }

        /**
         * 校验时间段列表，只有超过1个时间段时才检查重叠
         */
        public static boolean validateTimeRanges(List<String> timeRangeStrings) {
            if (timeRangeStrings == null || timeRangeStrings.size() < 2) {
                return true;
            }

            List<TimeRange> timeRanges = parseTimeRanges(timeRangeStrings);

            for (int i = 0; i < timeRanges.size() - 1; i++) {
                TimeRange current = timeRanges.get(i);
                TimeRange next = timeRanges.get(i + 1);

                if (current.overlapsWith(next)) {
                    return false;
                }
            }

            return true;
        }



        private static List<TimeRange> parseTimeRanges(List<String> timeRangeStrings) {
            List<TimeRange> timeRanges = new ArrayList<>();
            for (String rangeStr : timeRangeStrings) {
                timeRanges.add(parseTimeRange(rangeStr));
            }
            return timeRanges;
        }

    private static TimeRange parseTimeRange(String rangeStr) {
        try {
            String[] parts = rangeStr.split("-");
            if (parts.length != 2) {
                return null;
            }
            LocalTime start = parseTime(parts[0]);
            LocalTime end = parseTime(parts[1]);
            return new TimeRange(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime parseTime(String timeStr) {
        if ("24:00".equals(timeStr) || "24:00:00".equals(timeStr)) {
            // 将 24:00 视为第二天的 00:00
            return LocalTime.MIDNIGHT;
        }
        return LocalTime.parse(timeStr);
    }

        // 内部时间范围类
        private static class TimeRange {
            private final LocalTime start;
            private final LocalTime end;

            public TimeRange(LocalTime start, LocalTime end) {
                this.start = start;
                this.end = end;
            }

            public boolean overlapsWith(TimeRange other) {
                // 当前结束时间 >= 下一个开始时间，表示有重叠
                return !this.end.isBefore(other.start);
            }
        }






    /**
     * 处理奖品数据 - 提取的公共方法
     */
    private void handlePrizeData(LotterySettingsAddReqVO lotterySettingsAddVO,
                                 LotterySettingsDO lotterySettings,
                                 String userName) {
        if (lotterySettingsAddVO.getPrizePoolRules().equals(2)) {
            // 门店独立奖池模式
            handleStorePrizePool(lotterySettingsAddVO, lotterySettings, userName);
        } else {
            // 全局奖池模式
            handleGlobalPrizePool(lotterySettingsAddVO, lotterySettings, userName);
        }
    }

    /**
     * 处理门店独立奖池
     */
    private void handleStorePrizePool(LotterySettingsAddReqVO lotterySettingsAddVO,
                                      LotterySettingsDO lotterySettings,
                                      String userName) {
        List<Long> storeIds = getStoreIds(lotterySettingsAddVO);

        // 添加调试日志
        log.info("门店ID列表: {}", storeIds);
        log.info("奖品列表大小: {}", lotterySettingsAddVO.getPrizes().size());

        if (ObjectUtil.isEmpty(storeIds)) {
            log.warn("门店ID列表为空，跳过奖品处理");
            return;
        }

        // 预先生成所有奖品的唯一code（不包含门店信息）
        Map<String, String> prizeUniqueCodeMap = generatePrizeUniqueCodeMap(lotterySettingsAddVO.getPrizes());

        log.info("生成的奖品code映射: {}", prizeUniqueCodeMap);

        // 验证映射是否正确
        validatePrizeCodeMapping(lotterySettingsAddVO.getPrizes(), prizeUniqueCodeMap);

        for (Long storeId : storeIds) {
            List<LotteryPrizeDO> prizeList = buildStorePrizeList(
                    lotterySettingsAddVO.getPrizes(), storeId, lotterySettings.getId(), userName, prizeUniqueCodeMap);

            log.info("门店 {} 的奖品数量: {}", storeId, prizeList.size());

            // 验证该门店下奖品的code一致性
            validateStorePrizeCodes(storeId, prizeList);

            if (ObjectUtil.isNotEmpty(prizeList)) {
                lotteryPrizeMapper.insertBatch(prizeList);
                lotteryRedisDAO.setCacheObject(
                        LOTTERY_PRIZE + lotterySettings.getActivityId() + ":store:" + storeId, prizeList);
            }
        }
    }

    /**
     * 处理全局奖池
     */
    private void handleGlobalPrizePool(LotterySettingsAddReqVO lotterySettingsAddVO,
                                       LotterySettingsDO lotterySettings,
                                       String userName) {
        // 预先生成所有奖品的唯一code
        Map<String, String> prizeUniqueCodeMap = generatePrizeUniqueCodeMap(lotterySettingsAddVO.getPrizes());

        List<LotteryPrizeDO> prizeList = buildStorePrizeList(
                lotterySettingsAddVO.getPrizes(), null, lotterySettings.getId(), userName, prizeUniqueCodeMap);

        if (ObjectUtil.isNotEmpty(prizeList)) {
            lotteryPrizeMapper.insertBatch(prizeList);
            lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotterySettings.getActivityId(), prizeList);
        }
    }

    /**
     * 生成奖品唯一code映射
     * 相同的奖品会生成相同的唯一code（不包含门店信息）
     */
    private Map<String, String> generatePrizeUniqueCodeMap(List<LotteryPrizeReqVO> prizes) {
        Map<String, String> uniqueCodeMap = new HashMap<>();

        if (ObjectUtil.isEmpty(prizes)) {
            log.warn("奖品列表为空，无法生成code映射");
            return uniqueCodeMap;
        }

        // 首先，打印所有奖品的原始code，用于调试
        log.info("=== 开始生成奖品唯一code映射 ===");
        for (LotteryPrizeReqVO prize : prizes) {
            log.info("奖品: 名称={}, 原始code={}, 类型={}", prize.getPrizeName(), prize.getCode(), prize.getPrizeType());
        }

        for (LotteryPrizeReqVO prize : prizes) {
            prize.setRemainNum(0);
            // 为每个奖品生成唯一的code（不包含门店信息）
            // 使用原code作为key，确保相同奖品生成相同唯一code
            if (ObjectUtil.isEmpty(prize.getCode())) {
                log.warn("奖品code为空，跳过生成: {}", prize.getPrizeName());
                continue;
            }

            // 关键修改：检查是否已经为这个原始code生成了唯一code
            if (!uniqueCodeMap.containsKey(prize.getCode())) {
                // 为每个不同的奖品生成不同的唯一code
                String uniqueCode = generateStablePrizeCode(prize);
                uniqueCodeMap.put(prize.getCode(), uniqueCode);
                log.info("为奖品 {} (原code: {}) 生成唯一code: {}", prize.getPrizeName(), prize.getCode(), uniqueCode);
            } else {
                log.info("奖品 {} 使用已存在的唯一code: {}", prize.getPrizeName(), uniqueCodeMap.get(prize.getCode()));
            }
        }

        log.info("=== 最终生成的奖品code映射: {} ===", uniqueCodeMap);
        return uniqueCodeMap;
    }

    /**
     * 生成稳定的奖品code（基于奖品属性生成，确保相同奖品生成相同code）
     */
    private String generateStablePrizeCode(LotteryPrizeReqVO prize) {
        // 使用关键属性生成稳定的哈希值，确保相同奖品在不同运行中生成相同code
        String keyProperties = prize.getPrizeName() + "_" + prize.getPrizeType() + "_" + prize.getProbability();
        int propertiesHash = Math.abs(keyProperties.hashCode()) % 1000000;

        // 为了确保唯一性，加上UUID的前8位（但相同奖品的UUID部分应该相同）
        // 使用固定的UUID种子，基于关键属性生成
        String uuidSeed = "PRIZE_" + keyProperties;
        String stableUuid = UUID.nameUUIDFromBytes(uuidSeed.getBytes()).toString().replace("-", "").substring(0, 8);

        return "PRIZE_" + propertiesHash + "_" + stableUuid;
    }

    /**
     * 构建奖品DO对象
     */
    private LotteryPrizeDO buildPrizeDO(LotteryPrizeReqVO item,
                                        Long storeId,
                                        Long lotteryId,
                                        String userName,
                                        Map<String, String> prizeUniqueCodeMap) {
        if (item == null) {
            return null;
        }

        LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
        prize.setId(null);
        prize.setRemainNum(0);
        // 设置唯一code - 关键修改：确保使用映射中的code
        if (prizeUniqueCodeMap != null && ObjectUtil.isNotEmpty(item.getCode())) {
            // 使用预生成的唯一code
            String uniqueCode = prizeUniqueCodeMap.get(item.getCode());
            if (uniqueCode == null) {
                log.error("严重错误：未找到奖品 {} (原始code: {}) 的code映射", item.getPrizeName(), item.getCode());
                // 作为fallback，生成稳定的code
                uniqueCode = generateStablePrizeCode(item);
                log.warn("为奖品 {} 生成fallback code: {}", item.getPrizeName(), uniqueCode);
            }
            prize.setCode(uniqueCode);
            log.debug("设置奖品 {} (原始code: {}) 的code为: {}", item.getPrizeName(), item.getCode(), uniqueCode);
        } else {
            // 如果没有预生成映射或奖品code为空，则生成稳定的code
            String uniqueCode = generateStablePrizeCode(item);
            prize.setCode(uniqueCode);
            log.warn("奖品 {} 没有预生成映射，生成新的code: {}", item.getPrizeName(), uniqueCode);
        }

        if (ObjectUtil.isNotEmpty(item.getWinningCitys())) {
            prize.setWinningCitys(convertListToString(item.getWinningCitys()));
        } else {
            prize.setWinningCitys(null);
        }

        prize.setStoreId(storeId);
        prize.setCreateUserName(userName);
        prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
        prize.setUpdateUserName(userName);
        prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
        prize.setLotteryId(lotteryId);

        return prize;
    }

    /**
     * 验证奖品code映射是否正确
     */
    private void validatePrizeCodeMapping(List<LotteryPrizeReqVO> prizes, Map<String, String> codeMapping) {
        log.info("=== 开始验证奖品code映射 ===");

        // 按原始code分组，检查相同原始code的奖品
        Map<String, List<LotteryPrizeReqVO>> groupByOriginalCode = prizes.stream()
                .filter(prize -> ObjectUtil.isNotEmpty(prize.getCode()))
                .collect(Collectors.groupingBy(LotteryPrizeReqVO::getCode));

        for (Map.Entry<String, List<LotteryPrizeReqVO>> entry : groupByOriginalCode.entrySet()) {
            String originalCode = entry.getKey();
            List<LotteryPrizeReqVO> sameCodePrizes = entry.getValue();

            if (sameCodePrizes.size() > 1) {
                log.info("原始code {} 对应 {} 个奖品: {}",
                        originalCode, sameCodePrizes.size(),
                        sameCodePrizes.stream().map(LotteryPrizeReqVO::getPrizeName).collect(Collectors.toList()));
            }

            // 检查这些相同原始code的奖品是否映射到相同的唯一code
            String uniqueCode = codeMapping.get(originalCode);
            if (uniqueCode != null) {
                log.info("原始code {} 映射到唯一code: {}", originalCode, uniqueCode);
            } else {
                log.warn("原始code {} 没有对应的唯一code映射", originalCode);
            }
        }
        log.info("=== 奖品code映射验证完成 ===");
    }

    /**
     * 验证门店下奖品的code一致性
     */
    private void validateStorePrizeCodes(Long storeId, List<LotteryPrizeDO> prizeList) {
        log.info("=== 验证门店 {} 的奖品code一致性 ===", storeId);

        // 按code分组
        Map<String, List<LotteryPrizeDO>> groupByCode = prizeList.stream()
                .collect(Collectors.groupingBy(LotteryPrizeDO::getCode));

        for (Map.Entry<String, List<LotteryPrizeDO>> entry : groupByCode.entrySet()) {
            String code = entry.getKey();
            List<LotteryPrizeDO> sameCodePrizes = entry.getValue();

            if (sameCodePrizes.size() > 1) {
                log.info("门店 {} - code {} 对应 {} 个奖品: {}",
                        storeId, code, sameCodePrizes.size(),
                        sameCodePrizes.stream().map(LotteryPrizeDO::getPrizeName).collect(Collectors.toList()));
            }
        }

        // 按奖品名称分组，检查相同奖品是否有相同code
        Map<String, List<LotteryPrizeDO>> groupByName = prizeList.stream()
                .collect(Collectors.groupingBy(LotteryPrizeDO::getPrizeName));

        for (Map.Entry<String, List<LotteryPrizeDO>> entry : groupByName.entrySet()) {
            String prizeName = entry.getKey();
            List<LotteryPrizeDO> sameNamePrizes = entry.getValue();

            if (sameNamePrizes.size() > 1) {
                Set<String> codes = sameNamePrizes.stream()
                        .map(LotteryPrizeDO::getCode)
                        .collect(Collectors.toSet());

                if (codes.size() == 1) {
                    log.info("✅ 门店 {} - 奖品 {} 在所有实例中使用相同code: {}", storeId, prizeName, codes.iterator().next());
                } else {
                    log.error("❌ 门店 {} - 奖品 {} 使用不同的code: {}", storeId, prizeName, codes);
                }
            }
        }
        log.info("=== 门店 {} 的奖品code验证完成 ===", storeId);
    }

// 其他方法保持不变...






    /**
     * 构建门店奖品列表
     */
    private List<LotteryPrizeDO> buildStorePrizeList(List<LotteryPrizeReqVO> prizes,
                                                     Long storeId,
                                                     Long lotteryId,
                                                     String userName,
                                                     Map<String, String> prizeUniqueCodeMap) {
        List<LotteryPrizeDO> prizeList = new ArrayList<>();

        if (ObjectUtil.isEmpty(prizes)) {
            return prizeList;
        }

        for (LotteryPrizeReqVO item : prizes) {
            LotteryPrizeDO prize = buildPrizeDO(item, storeId, lotteryId, userName, prizeUniqueCodeMap);
            if (prize != null) {
                prizeList.add(prize);
            }
        }

        return prizeList;
    }



    /**
     * 获取门店ID列表
     */
    private List<Long> getStoreIds(LotterySettingsAddReqVO lotterySettingsAddVO) {
        List<Long> storeIds = Collections.emptyList();

        if (lotterySettingsAddVO.getActivityStore().equals(0)) {
            // 指定门店
            storeIds = lotterySettingsAddVO.getStoreIds();
            log.info("使用指定门店: {}", storeIds);
        } else if (lotterySettingsAddVO.getActivityStore().equals(1)) {
            // 所有门店
            CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
            if (allStoreList.isSuccess() && ObjectUtil.isNotEmpty(allStoreList.getData())) {
                storeIds = allStoreList.getData().stream()
                        .map(StoreInfoDTO::getStoreId)
                        .collect(Collectors.toList());
                log.info("使用所有门店: {}", storeIds);
            } else {
                log.warn("获取所有门店列表失败或为空");
            }
        } else {
            log.warn("未知的门店活动类型: {}", lotterySettingsAddVO.getActivityStore());
        }

        return storeIds;
    }


    /**
     * 生成奖品的唯一code（不包含门店信息）
     */
    private String generateUniquePrizeCode() {
        // 使用UUID保证唯一性 - 每次调用都生成不同的UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "PRIZE_" + uuid.substring(0, 16);
    }


    /**
     * 雪花ID生成器
     */
    private static class SnowflakeIdGenerator {
        private static final long START_TIMESTAMP = 1609459200000L; // 2021-01-01 00:00:00
        private static final long WORKER_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 5L;
        private static final long SEQUENCE_BITS = 12L;

        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
        private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
        private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

        private static long workerId = 1;
        private static long datacenterId = 1;
        private static long sequence = 0L;
        private static long lastTimestamp = -1L;

        public static synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards. Refusing to generate id");
            }

            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                    | (datacenterId << DATACENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT)
                    | sequence;
        }

        private static long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_UPDATE_TYPE, bizNo = "{{#lotterySettingsVO.id}}", success = SYSTEM_LOTTERY_LOG_UPDATE_TYPE_SUCCESS)
    public CommonResult<Integer> updateLotteryData(LotterySettingsUpdateReqVO lotterySettingsUpdateReqVO) {
        LotterySettingsDO existingSettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                .eq(LotterySettingsDO::getActivityId, lotterySettingsUpdateReqVO.getActivityId()));
        ActivityDO existingActivity = activityService.selectById(lotterySettingsUpdateReqVO.getActivityId());
        boolean enablingLegacy = existingSettings != null && existingActivity != null
                && !Objects.equals(existingSettings.getRuntimeVersion(), 2)
                && Objects.equals(lotterySettingsUpdateReqVO.getIsEnabled(), 1)
                && (!Objects.equals(existingSettings.getState(), 1)
                || !Objects.equals(existingActivity.getIsEnabled(), 1));
        // 历史活动重启切换前，仍按原有奖品和库存保存逻辑处理。
        if (!enablingLegacy && (existingSettings == null || !Objects.equals(existingSettings.getRuntimeVersion(), 2))) {
            return updateLegacyLotteryData(lotterySettingsUpdateReqVO);
        }
        LambdaUpdateWrapper<LotterySettingsDO> updateWrapper = new LambdaUpdateWrapper<>();
        if(ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getTimeRangeList())){
            boolean b = validateTimeRanges(lotterySettingsUpdateReqVO.getTimeRangeList());
            if(!b){
                throw exception(LOTTERY_SESSION_ERROR);
            }
        }
        Long activityId = lotterySettingsUpdateReqVO.getActivityId();
        ActivityDO activityDO = activityService.selectById(activityId);
        LotterySettingsDO lotterySettingsDO = new LotterySettingsDO();
        List<Long> originalTagIds = null;
        List<Long> originalStoreIds = null;
        if (activityDO != null) {
            boolean activityWasEnabled = Objects.equals(activityDO.getIsEnabled(), 1);
            Integer originalAppScope = activityDO.getAppScope();
            originalTagIds = lotterySettingsUpdateReqVO.getTagIds() == null
                    ? lotteryScopeService.tagIds(activityId) : null;
            originalStoreIds = lotterySettingsUpdateReqVO.getStoreIds() == null
                    ? activityStoreService.selectByActivityId(activityId).stream()
                    .map(ActivityStoreDO::getStoreId).toList() : null;
            BeanUtils.copyProperties(lotterySettingsUpdateReqVO, activityDO);
            activityDO.setAppScope(lotterySettingsUpdateReqVO.getAppScope() == null
                    ? originalAppScope : lotterySettingsUpdateReqVO.getAppScope());
            activityDO.setWeekNumbers(convertListToString(lotterySettingsUpdateReqVO.getWeekNumberList()));
            activityDO.setDayNumbers(convertListToString(lotterySettingsUpdateReqVO.getDayNumberList()));
            activityDO.setTimeRange(convertListToString(lotterySettingsUpdateReqVO.getTimeRangeList()));
            activityDO.setSelectedGroups(convertListToString(lotterySettingsUpdateReqVO.getSelectedGroupList()));
            activityDO.setActivityRemark(lotterySettingsUpdateReqVO.getActivityRemark());
            activityDO.setActivityName(lotterySettingsUpdateReqVO.getActivityName());
            activityDO.setId(activityId);


            LambdaQueryWrapper<LotterySettingsDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LotterySettingsDO::getActivityId, activityId).last("FOR UPDATE");



            lotterySettingsDO = lotterySettingsMapper.selectOne(wrapper);
            if (lotterySettingsDO == null) throw exception(LOTTERY_NOT_NULL);
            if (Objects.equals(lotterySettingsUpdateReqVO.getIsEnabled(), 1)
                    && (!Objects.equals(lotterySettingsDO.getState(), 1) || !activityWasEnabled)) {
                lotteryRuntime.initializeLegacy(lotterySettingsDO);
            }
            updateWrapper.eq(LotterySettingsDO::getId,lotterySettingsDO.getId());
            updateWrapper.set(LotterySettingsDO::getLotteryType, lotterySettingsUpdateReqVO.getLotteryType());
            updateWrapper.set(LotterySettingsDO::getState, lotterySettingsUpdateReqVO.getIsEnabled());
            updateWrapper.set(LotterySettingsDO::getLotteryTitle, lotterySettingsUpdateReqVO.getActivityName());
            updateWrapper.set(LotterySettingsDO::getLotteryRule, lotterySettingsUpdateReqVO.getActivityRules());
            updateWrapper.set(LotterySettingsDO::getShareType, lotterySettingsUpdateReqVO.getShareType());
            updateWrapper.set(LotterySettingsDO::getLotteryStartTime, lotterySettingsUpdateReqVO.getStartDate());
            updateWrapper.set(LotterySettingsDO::getLotteryEndTime, lotterySettingsUpdateReqVO.getEndDate());
            updateWrapper.set(LotterySettingsDO::getLotteryLimit, lotterySettingsUpdateReqVO.getLotteryLimit());
            updateWrapper.set(LotterySettingsDO::getActivityProduct, lotterySettingsUpdateReqVO.getActivityProduct());
            updateWrapper.set(LotterySettingsDO::getPrice, lotterySettingsUpdateReqVO.getPrice());
            updateWrapper.set(LotterySettingsDO::getShareTitle, lotterySettingsUpdateReqVO.getShareTitle());
            updateWrapper.set(LotterySettingsDO::getShareNote, lotterySettingsUpdateReqVO.getShareNote());
            updateWrapper.set(LotterySettingsDO::getShareImgUrl, lotterySettingsUpdateReqVO.getShareImgUrl());
            updateWrapper.set(LotterySettingsDO::getStoreId, lotterySettingsUpdateReqVO.getStoreId());
            updateWrapper.set(LotterySettingsDO::getActivityImgUrl, lotterySettingsUpdateReqVO.getActivityImgUrl());
            updateWrapper.set(LotterySettingsDO::getPaymentThreshold, lotterySettingsUpdateReqVO.getPaymentThreshold());
            updateWrapper.set(LotterySettingsDO::getActivityBackground, lotterySettingsUpdateReqVO.getActivityBackground());
            updateWrapper.set(LotterySettingsDO::getButtonImgUrl, lotterySettingsUpdateReqVO.getButtonImgUrl());
            updateWrapper.set(LotterySettingsDO::getBackgroundColor, lotterySettingsUpdateReqVO.getBackgroundColor());
            updateWrapper.set(LotterySettingsDO::getLotteryMethod, lotterySettingsUpdateReqVO.getLotteryMethod());
            updateWrapper.set(LotterySettingsDO::getLotteryTotalNumber, lotterySettingsUpdateReqVO.getLotteryTotalNumber());
            updateWrapper.set(LotterySettingsDO::getIsFree, lotterySettingsUpdateReqVO.getIsFree());
            updateWrapper.set(LotterySettingsDO::getCalculationRules, lotterySettingsUpdateReqVO.getCalculationRules());
            updateWrapper.set(LotterySettingsDO::getPrizePoolRules, lotterySettingsUpdateReqVO.getPrizePoolRules());
            updateWrapper.set(LotterySettingsDO::getLotteryRulesReset, lotterySettingsUpdateReqVO.getLotteryRulesReset());
            updateWrapper.set(LotterySettingsDO::getPublicButton, lotterySettingsUpdateReqVO.getPublicButton());
            updateWrapper.set(LotterySettingsDO::getCommunityFlag, lotterySettingsUpdateReqVO.getCommunityFlag());
            updateWrapper.set(LotterySettingsDO::getGuideImage, lotterySettingsUpdateReqVO.getGuideImage());
            updateWrapper.set(LotterySettingsDO::getBrowseType, lotterySettingsUpdateReqVO.getBrowseType());
            updateWrapper.set(LotterySettingsDO::getBrowseCount, lotterySettingsUpdateReqVO.getBrowseCount());
            updateWrapper.set(LotterySettingsDO::getFreeStatus, lotterySettingsUpdateReqVO.getFreeStatus());
            updateWrapper.set(LotterySettingsDO::getFreeCount, lotterySettingsUpdateReqVO.getFreeCount());
            updateWrapper.set(LotterySettingsDO::getPointsStatus, lotterySettingsUpdateReqVO.getPointsStatus());
            updateWrapper.set(LotterySettingsDO::getPointsType, lotterySettingsUpdateReqVO.getPointsType());
            updateWrapper.set(LotterySettingsDO::getPointsCount, lotterySettingsUpdateReqVO.getPointsCount());
            updateWrapper.set(LotterySettingsDO::getOrderStatus, lotterySettingsUpdateReqVO.getOrderStatus());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderType, lotterySettingsUpdateReqVO.getPlaceOrderType());
            updateWrapper.set(LotterySettingsDO::getCategoryType, lotterySettingsUpdateReqVO.getCategoryType());
            updateWrapper.set(LotterySettingsDO::getPaymentType, lotterySettingsUpdateReqVO.getPaymentType());
            updateWrapper.set(LotterySettingsDO::getPaymentCount, lotterySettingsUpdateReqVO.getPaymentCount());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderLottery, lotterySettingsUpdateReqVO.getPlaceOrderLottery());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderLotteryNumber, lotterySettingsUpdateReqVO.getPlaceOrderLotteryNumber());
            updateWrapper.set(LotterySettingsDO::getShareEvent, lotterySettingsUpdateReqVO.getShareEvent());
            updateWrapper.set(LotterySettingsDO::getShareCount, lotterySettingsUpdateReqVO.getShareCount());

//            BeanUtils.copyProperties(lotterySettingsUpdateReqVO, lotterySettingsDO);
//            lotterySettingsDO.setLotteryRule(lotterySettingsUpdateReqVO.getActivityRules());
//            lotterySettingsDO.setLotteryStartTime(lotterySettingsUpdateReqVO.getStartDate());
//            lotterySettingsDO.setLotteryEndTime(lotterySettingsUpdateReqVO.getEndDate());
//            lotterySettingsDO.setLotteryTitle(lotterySettingsUpdateReqVO.getActivityName());
//            lotterySettingsDO.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
//            lotterySettingsDO.setUpdateTime(LocalDateTime.now());

            lotterySettingsUpdateReqVO.setId(activityDO.getId());
        }

        // 判断奖品存在唯一兜底商品
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsUpdateReqVO)) {
            throw exception(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
//        if (!validateTotalProbability(lotterySettingsUpdateReqVO)) {
//            return error(LOTTERY_WINNING_PROBABILITY);
//        }
        if (lotterySettingsUpdateReqVO.getParticipantGroup().equals(5)) {
            if (lotterySettingsUpdateReqVO.getSelectedGroupList().size() <= 0) {
                throw exception(LOTTERY_CROWD_NOT_NULL);
            }
        }



        //删除活动关联商品
        activityLotteryCommodityService.deleteByActivityId(activityId);

        if (ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getCommodityIds())) {
            activityLotteryCommodityService.createBatch(lotterySettingsUpdateReqVO.getCommodityIds(), activityId);
        }
        if (lotterySettingsDO != null) {
            List<Long> scopeStoreIds = lotteryScopeService.replace(activityDO,
                    lotterySettingsUpdateReqVO.getTagIds() == null ? originalTagIds : lotterySettingsUpdateReqVO.getTagIds(),
                    lotterySettingsUpdateReqVO.getStoreIds() == null ? originalStoreIds : lotterySettingsUpdateReqVO.getStoreIds());
            Integer previousPool = lotterySettingsDO.getPrizePoolRules();
            lotterySettingsMapper.update(updateWrapper);
            LotterySettingsDO current = lotterySettingsMapper.selectById(lotterySettingsDO.getId());
            lotteryScopeService.syncPrizes(current, lotterySettingsUpdateReqVO.getPrizes(), scopeStoreIds, previousPool);
            activityService.updateActivity(activityDO);
            // 记录操作日志上下文
            LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsUpdateReqVO);
            return success(1);
        } else {
            return null;
        }


    }

    private CommonResult<Integer> updateLegacyLotteryData(LotterySettingsUpdateReqVO lotterySettingsUpdateReqVO) {
        LambdaUpdateWrapper<LotterySettingsDO> updateWrapper = new LambdaUpdateWrapper<>();
        if(ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getTimeRangeList())){
            boolean b = validateTimeRanges(lotterySettingsUpdateReqVO.getTimeRangeList());
            if(!b){
                throw exception(LOTTERY_SESSION_ERROR);
            }
        }
        Long activityId = lotterySettingsUpdateReqVO.getActivityId();
        ActivityDO activityDO = activityService.selectById(activityId);
        LotterySettingsDO lotterySettingsDO = new LotterySettingsDO();
        List<Long> effectiveStoreIds = lotterySettingsUpdateReqVO.getStoreIds();
        if (activityDO != null) {
            Integer originalAppScope = activityDO.getAppScope();
            boolean updateTagScope = Objects.equals(lotterySettingsUpdateReqVO.getAppScope(), 1);
            boolean rebuildScope = updateTagScope || Objects.equals(originalAppScope, 1);

            //证明有改变
            if (!rebuildScope && !lotterySettingsUpdateReqVO.getActivityStore().equals(activityDO.getActivityStore())){
                //删除活动关联门店
                activityStoreService.deleteByActivityId(activityId);
                if(lotterySettingsUpdateReqVO.getActivityStore().equals(0)){
                    if (ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getStoreIds())) {
                        activityStoreService.createBatch(lotterySettingsUpdateReqVO.getStoreIds(), activityId);
                    }
                }
            }else if(!rebuildScope && lotterySettingsUpdateReqVO.getActivityStore().equals(0)){

                List<ActivityStoreDO> activityStoreDOS = activityStoreService.selectByActivityId(activityId);
                if(ObjectUtil.isNotEmpty(activityStoreDOS)){
                    List<Long> collect = activityStoreDOS.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                    List<Long> storeIds = lotterySettingsUpdateReqVO.getStoreIds();
                    // 使用HashSet判断是否相同
                    Set<Long> set1 = new HashSet<>(collect);
                    Set<Long> set2 = new HashSet<>(storeIds);

                    boolean isSame = set1.equals(set2);

                    if (isSame) {

                    } else {
// 获取需要删除的storeId（在collect中但不在storeIds中）
                        Set<Long> toDeleteStoreIds = new HashSet<>(set1);
                        toDeleteStoreIds.removeAll(set2);

                        // 获取需要新增的storeId（在storeIds中但不在collect中）
                        Set<Long> toAddStoreIds = new HashSet<>(set2);
                        toAddStoreIds.removeAll(set1);

                        log.info("需要删除的门店ID: {}", toDeleteStoreIds);
                        log.info("需要新增的门店ID: {}", toAddStoreIds);

                        // 执行相应的业务逻辑
                        if (!toDeleteStoreIds.isEmpty()) {
                            // 删除多余的门店
                            List<Long> longs = new ArrayList<>(toDeleteStoreIds);
                            for (Long aLong : longs) {
                                LambdaQueryWrapper<ActivityStoreDO> storeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                                storeDOLambdaQueryWrapper.eq(ActivityStoreDO::getActivityId,activityId);
                                storeDOLambdaQueryWrapper.eq(ActivityStoreDO::getStoreId,aLong);

                                activityStoreService.remove(storeDOLambdaQueryWrapper);
                            }

                        }

                        if (!toAddStoreIds.isEmpty()) {
                            // 新增缺少的门店
                            activityStoreService.createBatch(new ArrayList<>(toAddStoreIds), activityId);
                        }
                    }
                }

            }
            BeanUtils.copyProperties(lotterySettingsUpdateReqVO, activityDO);
            activityDO.setAppScope(lotterySettingsUpdateReqVO.getAppScope() == null
                    ? originalAppScope : lotterySettingsUpdateReqVO.getAppScope());
            activityDO.setWeekNumbers(convertListToString(lotterySettingsUpdateReqVO.getWeekNumberList()));
            activityDO.setDayNumbers(convertListToString(lotterySettingsUpdateReqVO.getDayNumberList()));
            activityDO.setTimeRange(convertListToString(lotterySettingsUpdateReqVO.getTimeRangeList()));
            activityDO.setSelectedGroups(convertListToString(lotterySettingsUpdateReqVO.getSelectedGroupList()));
            activityDO.setActivityRemark(lotterySettingsUpdateReqVO.getActivityRemark());
            activityDO.setActivityName(lotterySettingsUpdateReqVO.getActivityName());
            activityDO.setId(activityId);
            if (rebuildScope) {
                // 旧活动切换标签范围时，门店和标签关系都由同一范围服务重建。
                effectiveStoreIds = lotteryScopeService.replace(activityDO,
                        lotterySettingsUpdateReqVO.getTagIds() == null
                                ? lotteryScopeService.tagIds(activityId) : lotterySettingsUpdateReqVO.getTagIds(),
                        lotterySettingsUpdateReqVO.getStoreIds() == null
                                ? activityStoreService.selectByActivityId(activityId).stream()
                                .map(ActivityStoreDO::getStoreId).toList() : lotterySettingsUpdateReqVO.getStoreIds());
            }


            LambdaQueryWrapper<LotterySettingsDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LotterySettingsDO::getActivityId, activityId);



            lotterySettingsDO = lotterySettingsMapper.selectOne(wrapper);
            updateWrapper.eq(LotterySettingsDO::getId,lotterySettingsUpdateReqVO.getId());
            updateWrapper.set(LotterySettingsDO::getLotteryType, lotterySettingsUpdateReqVO.getLotteryType());
            updateWrapper.set(LotterySettingsDO::getState, lotterySettingsUpdateReqVO.getState());
            updateWrapper.set(LotterySettingsDO::getLotteryTitle, lotterySettingsUpdateReqVO.getActivityName());
            updateWrapper.set(LotterySettingsDO::getLotteryRule, lotterySettingsUpdateReqVO.getActivityRules());
            updateWrapper.set(LotterySettingsDO::getShareType, lotterySettingsUpdateReqVO.getShareType());
            updateWrapper.set(LotterySettingsDO::getLotteryStartTime, lotterySettingsUpdateReqVO.getStartDate());
            updateWrapper.set(LotterySettingsDO::getLotteryEndTime, lotterySettingsUpdateReqVO.getEndDate());
            updateWrapper.set(LotterySettingsDO::getLotteryLimit, lotterySettingsUpdateReqVO.getLotteryLimit());
            updateWrapper.set(LotterySettingsDO::getActivityProduct, lotterySettingsUpdateReqVO.getActivityProduct());
            updateWrapper.set(LotterySettingsDO::getPrice, lotterySettingsUpdateReqVO.getPrice());
            updateWrapper.set(LotterySettingsDO::getShareTitle, lotterySettingsUpdateReqVO.getShareTitle());
            updateWrapper.set(LotterySettingsDO::getShareNote, lotterySettingsUpdateReqVO.getShareNote());
            updateWrapper.set(LotterySettingsDO::getShareImgUrl, lotterySettingsUpdateReqVO.getShareImgUrl());
            updateWrapper.set(LotterySettingsDO::getStoreId, lotterySettingsUpdateReqVO.getStoreId());
            updateWrapper.set(LotterySettingsDO::getActivityImgUrl, lotterySettingsUpdateReqVO.getActivityImgUrl());
            updateWrapper.set(LotterySettingsDO::getPaymentThreshold, lotterySettingsUpdateReqVO.getPaymentThreshold());
            updateWrapper.set(LotterySettingsDO::getActivityBackground, lotterySettingsUpdateReqVO.getActivityBackground());
            updateWrapper.set(LotterySettingsDO::getButtonImgUrl, lotterySettingsUpdateReqVO.getButtonImgUrl());
            updateWrapper.set(LotterySettingsDO::getBackgroundColor, lotterySettingsUpdateReqVO.getBackgroundColor());
            updateWrapper.set(LotterySettingsDO::getLotteryMethod, lotterySettingsUpdateReqVO.getLotteryMethod());
            updateWrapper.set(LotterySettingsDO::getLotteryTotalNumber, lotterySettingsUpdateReqVO.getLotteryTotalNumber());
            updateWrapper.set(LotterySettingsDO::getIsFree, lotterySettingsUpdateReqVO.getIsFree());
            updateWrapper.set(LotterySettingsDO::getCalculationRules, lotterySettingsUpdateReqVO.getCalculationRules());
            updateWrapper.set(LotterySettingsDO::getPrizePoolRules, lotterySettingsUpdateReqVO.getPrizePoolRules());
            updateWrapper.set(LotterySettingsDO::getLotteryRulesReset, lotterySettingsUpdateReqVO.getLotteryRulesReset());
            updateWrapper.set(LotterySettingsDO::getPublicButton, lotterySettingsUpdateReqVO.getPublicButton());
            updateWrapper.set(LotterySettingsDO::getCommunityFlag, lotterySettingsUpdateReqVO.getCommunityFlag());
            updateWrapper.set(LotterySettingsDO::getGuideImage, lotterySettingsUpdateReqVO.getGuideImage());
            updateWrapper.set(LotterySettingsDO::getBrowseType, lotterySettingsUpdateReqVO.getBrowseType());
            updateWrapper.set(LotterySettingsDO::getBrowseCount, lotterySettingsUpdateReqVO.getBrowseCount());
            updateWrapper.set(LotterySettingsDO::getFreeStatus, lotterySettingsUpdateReqVO.getFreeStatus());
            updateWrapper.set(LotterySettingsDO::getFreeCount, lotterySettingsUpdateReqVO.getFreeCount());
            updateWrapper.set(LotterySettingsDO::getPointsStatus, lotterySettingsUpdateReqVO.getPointsStatus());
            updateWrapper.set(LotterySettingsDO::getPointsType, lotterySettingsUpdateReqVO.getPointsType());
            updateWrapper.set(LotterySettingsDO::getPointsCount, lotterySettingsUpdateReqVO.getPointsCount());
            updateWrapper.set(LotterySettingsDO::getOrderStatus, lotterySettingsUpdateReqVO.getOrderStatus());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderType, lotterySettingsUpdateReqVO.getPlaceOrderType());
            updateWrapper.set(LotterySettingsDO::getCategoryType, lotterySettingsUpdateReqVO.getCategoryType());
            updateWrapper.set(LotterySettingsDO::getPaymentType, lotterySettingsUpdateReqVO.getPaymentType());
            updateWrapper.set(LotterySettingsDO::getPaymentCount, lotterySettingsUpdateReqVO.getPaymentCount());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderLottery, lotterySettingsUpdateReqVO.getPlaceOrderLottery());
            updateWrapper.set(LotterySettingsDO::getPlaceOrderLotteryNumber, lotterySettingsUpdateReqVO.getPlaceOrderLotteryNumber());
            updateWrapper.set(LotterySettingsDO::getShareEvent, lotterySettingsUpdateReqVO.getShareEvent());
            updateWrapper.set(LotterySettingsDO::getShareCount, lotterySettingsUpdateReqVO.getShareCount());

//            BeanUtils.copyProperties(lotterySettingsUpdateReqVO, lotterySettingsDO);
//            lotterySettingsDO.setLotteryRule(lotterySettingsUpdateReqVO.getActivityRules());
//            lotterySettingsDO.setLotteryStartTime(lotterySettingsUpdateReqVO.getStartDate());
//            lotterySettingsDO.setLotteryEndTime(lotterySettingsUpdateReqVO.getEndDate());
//            lotterySettingsDO.setLotteryTitle(lotterySettingsUpdateReqVO.getActivityName());
//            lotterySettingsDO.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
//            lotterySettingsDO.setUpdateTime(LocalDateTime.now());

            lotterySettingsUpdateReqVO.setId(activityDO.getId());
        }

        // 判断奖品存在唯一兜底商品
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsUpdateReqVO)) {
            return error(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
//        if (!validateTotalProbability(lotterySettingsUpdateReqVO)) {
//            return error(LOTTERY_WINNING_PROBABILITY);
//        }
        if (lotterySettingsUpdateReqVO.getParticipantGroup().equals(5)) {
            if (lotterySettingsUpdateReqVO.getSelectedGroupList().size() <= 0) {
                throw exception(LOTTERY_CROWD_NOT_NULL);
            }
        }



        //删除活动关联商品
        activityLotteryCommodityService.deleteByActivityId(activityId);

        if (ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getCommodityIds())) {
            activityLotteryCommodityService.createBatch(lotterySettingsUpdateReqVO.getCommodityIds(), activityId);
        }
        if (lotterySettingsDO != null) {
            lotterySettingsUpdateReqVO.setActivityId(activityId);

            LotterySettingsCacheDataVO lotterySettingsCacheDataVO = new LotterySettingsCacheDataVO();
            lotterySettingsCacheDataVO.setId(activityId);
            BeanUtils.copyProperties(lotterySettingsUpdateReqVO, lotterySettingsCacheDataVO);
            lotterySettingsCacheDataVO.setLotteryStartTime(lotterySettingsUpdateReqVO.getStartDate());
            lotterySettingsCacheDataVO.setLotteryEndTime(lotterySettingsUpdateReqVO.getEndDate());
            lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + activityDO.getId(), lotterySettingsCacheDataVO);
            Long id = lotterySettingsDO.getId();
            delAllStore(activityDO.getId());




            if(lotterySettingsUpdateReqVO.getPrizePoolRules().equals(2)){
                //证明有改变
                if(!lotterySettingsUpdateReqVO.getActivityStore().equals(activityDO.getActivityStore())){
                    if(lotterySettingsUpdateReqVO.getActivityStore().equals(1)){
                        CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
                        List<StoreInfoDTO> data = allStoreList.getData();
                        if(ObjectUtil.isNotEmpty(data)){
                            //获取所有门店的id
                            List<Long> collect = data.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            LambdaQueryWrapper<LotteryPrizeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                            lambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettingsDO.getId())
                                    .groupBy(LotteryPrizeDO::getStoreId)  // 按storeId分组
                                    .select(LotteryPrizeDO::getStoreId);  // 只查询storeId字段

                            List<LotteryPrizeDO> distinctStoreList = lotteryPrizeMapper.selectList(lambdaQueryWrapper);
                            if (ObjectUtil.isNotEmpty(distinctStoreList)) {
                                // 提取distinctStoreList中的storeId
                                Set<Long> distinctStoreIds = distinctStoreList.stream()
                                        .map(LotteryPrizeDO::getStoreId)
                                        .collect(Collectors.toSet());

                                // 找出collect中有但distinctStoreIds中没有的门店ID
                                List<Long> missingStoreIds = collect.stream()
                                        .filter(storeId -> !distinctStoreIds.contains(storeId))
                                        .collect(Collectors.toList());

                                if (ObjectUtil.isNotEmpty(missingStoreIds)) {
                                    for (Long missingStoreId : missingStoreIds) {
                                        List<LotteryPrizeDO> list = lotterySettingsUpdateReqVO.getPrizes().stream().map(item -> {
                                                    LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                                                    if(ObjectUtil.isNotEmpty(item.getWinningCitys())){
                                                        prize.setWinningCitys(convertListToString(item.getWinningCitys()));
                                                    }else{
                                                        prize.setWinningCitys(null);
                                                    }
                                                    prize.setCode(item.getCode());
                                                    prize.setStoreId(missingStoreId);
//                                        prize.setCreateUserName(userName);
                                                    prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                                        prize.setUpdateUserName(userName);
                                                    prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                                    prize.setLotteryId(id);
                                                    return prize;
                                                })
                                                .collect(Collectors.toList());

                                        lotteryPrizeMapper.insertBatch(list);
                                        //添加奖品缓存
//                                    lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + id + ":store:" + missingStoreId, list);
                                    }
                                    // 这里可以处理这些缺失的门店ID
                                } else {
                                    log.info("collect中的所有门店ID都在distinctStoreList中存在");
                                }
                            } else {
                                log.info("distinctStoreList为空，collect中的所有门店ID都是缺失的: {}", collect);
                            }

                        }
                        //说明是全部门店转为部分门店
                    }else if(lotterySettingsUpdateReqVO.getActivityStore().equals(0)) {
                        CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
                        List<StoreInfoDTO> data = allStoreList.getData();
                        if (ObjectUtil.isNotEmpty(data)) {
                            List<Long> collect = data.stream().map(mm -> mm.getStoreId()).collect(Collectors.toList());
                            List<Long> storeIds = effectiveStoreIds;
                            // 使用HashSet判断是否相同
                            Set<Long> set1 = new HashSet<>(collect);
                            Set<Long> set2 = new HashSet<>(storeIds);

                            boolean isSame = set1.equals(set2);

                            if (isSame) {

                            } else {
                                // 获取需要删除的storeId（在collect中但不在storeIds中）
                                Set<Long> toDeleteStoreIds = new HashSet<>(set1);
                                toDeleteStoreIds.removeAll(set2);


                                log.info("需要删除的门店ID: {}", toDeleteStoreIds);
//                            log.info("需要新增的门店ID: {}", toAddStoreIds);

                                // 执行相应的业务逻辑
                                if (!toDeleteStoreIds.isEmpty()) {
                                    // 删除多余的门店
                                    List<Long> longs = new ArrayList<>(toDeleteStoreIds);
                                    LambdaQueryWrapper<LotteryPrizeDO> wrapper = new LambdaQueryWrapper<>();
                                    wrapper.in(LotteryPrizeDO::getStoreId, longs);
                                    wrapper.eq(LotteryPrizeDO::getLotteryId,lotterySettingsDO.getId());
                                    lotteryPrizeMapper.delete(wrapper);
                                }

                            }

                        }
                    }


                }else{
                    if(lotterySettingsUpdateReqVO.getActivityStore().equals(0)){
//                        List<ActivityStoreDO> activityStoreDOS = activityStoreService.selectByActivityId(activityId);
                        Set<Long> collect = this.getDistinctStoreIds(id);
                        if (ObjectUtil.isNotEmpty(collect)) {
                            List<Long> storeIds = effectiveStoreIds;
                            Set<Long> set1 = new HashSet<>(collect);
                            Set<Long> set2 = new HashSet<>(storeIds);

                            boolean isSame = set1.equals(set2);

                            if (!isSame) {
                                // 获取需要删除的storeId（在collect中但不在storeIds中）
                                Set<Long> toDeleteStoreIds = new HashSet<>(set1);
                                toDeleteStoreIds.removeAll(set2);

                                // 获取需要新增的storeId（在storeIds中但不在collect中）
                                Set<Long> toAddStoreIds = new HashSet<>(set2);
                                toAddStoreIds.removeAll(set1);

                                log.info("需要删除的门店ID: {}", toDeleteStoreIds);
                                log.info("需要新增的门店ID: {}", toAddStoreIds);

                                // 执行删除逻辑
                                if (!toDeleteStoreIds.isEmpty()) {
                                    LambdaQueryWrapper<LotteryPrizeDO> wrapper = new LambdaQueryWrapper<>();
                                    wrapper.in(LotteryPrizeDO::getStoreId, toDeleteStoreIds);
                                    wrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettingsDO.getId());
                                    lotteryPrizeMapper.delete(wrapper);
                                }

                                // 执行新增逻辑 - 修复版本
                                if (!toAddStoreIds.isEmpty()) {
                                    List<LotteryPrizeDO> prizesToInsert = new ArrayList<>();

                                    for (Long toAddStoreId : toAddStoreIds) {
                                        List<LotteryPrizeDO> storePrizes = lotterySettingsUpdateReqVO.getPrizes().stream()
                                                .map(item -> {
                                                    LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                                                    if (ObjectUtil.isNotEmpty(item.getWinningCitys())) {
                                                        prize.setWinningCitys(convertListToString(item.getWinningCitys()));
                                                    } else {
                                                        prize.setWinningCitys(null);
                                                    }
                                                    prize.setId(null);
                                                    prize.setCreateTime(LocalDateTime.now());
                                                    prize.setUpdateTime(LocalDateTime.now());
                                                    // 确保其他必要字段
                                                    prize.setCode(item.getCode());
                                                    prize.setStoreId(toAddStoreId); // 设置正确的门店ID
                                                    prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                                    prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                                    prize.setLotteryId(id);
                                                    return prize;
                                                })
                                                .collect(Collectors.toList());

                                        prizesToInsert.addAll(storePrizes);
                                    }

                                    // 批量插入所有需要新增的数据
                                    if (!prizesToInsert.isEmpty()) {
                                        boolean success = lotteryPrizeService.saveBatch(prizesToInsert);
                                        log.info("批量插入执行结果，影响行数: {}", success);

                                        // 立即验证
                                        LambdaQueryWrapper<LotteryPrizeDO> queryWrapper = new LambdaQueryWrapper<>();
                                        queryWrapper.eq(LotteryPrizeDO::getLotteryId, id);
//                                        queryWrapper.in(LotteryPrizeDO::getStoreId, toAddStoreIds);
                                        Long aLong = lotteryPrizeMapper.selectCount(queryWrapper);
                                        log.info("插入后数据条数: {}", aLong);
                                    }
                                }

                            }
                        }
                    }

                }

            }else{

                //所有的奖品
                LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettingsDO.getId());
                List<LotteryPrizeDO> selectList = lotteryPrizeMapper.selectList(lotteryPrizeDOLambdaQueryWrapper);
                List<Long> ids = selectList.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                List<Long> collect = lotterySettingsUpdateReqVO.getPrizes().stream()
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
                    lotteryPrizeMapper.deleteByIds(missingIds);
                }



              // 取两个集合的交集（需要更新的ID）
                Set<Long> intersectionSet = new HashSet<>(collectSet);
                intersectionSet.retainAll(idsSet);
                List<Long> updateIds = new ArrayList<>(intersectionSet);

                if (ObjectUtil.isNotEmpty(updateIds)) {
                    // 找到需要更新的奖品数据
                    List<LotteryPrizeReqVO> updatePrizes = lotterySettingsUpdateReqVO.getPrizes().stream()
                            .filter(item -> updateIds.contains(item.getId()))
                            .collect(Collectors.toList());

                    // 构建更新列表
                    List<LotteryPrizeDO> updateList = updatePrizes.stream()
                            .map(item -> {
                                LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                                if (ObjectUtil.isNotEmpty(item.getWinningCitys())) {
                                    prize.setWinningCitys(convertListToString(item.getWinningCitys()));
                                } else {
                                    prize.setWinningCitys(null);
                                }
                                prize.setRemainNum(null);
                                prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                prize.setUpdateTime(LocalDateTime.now());
                                return prize;
                            })
                            .collect(Collectors.toList());

                    // 批量更新
                    boolean updateSuccess = lotteryPrizeService.updateBatchById(updateList);
                    log.info("批量更新奖品数据结果: {}, 更新数量: {}", updateSuccess ? "成功" : "失败", updateList.size());
                }






                if(ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getPrizes())){
                    List<LotteryPrizeReqVO> lotterySettingsUpdateReqVOPrizes = lotterySettingsUpdateReqVO.getPrizes();
                    for (LotteryPrizeReqVO lotteryPrizeReqVO : lotterySettingsUpdateReqVOPrizes) {
                        if(ObjectUtil.isEmpty(lotteryPrizeReqVO.getId())){
                            LotteryPrizeDO prize = BeanUtils.toBean(lotteryPrizeReqVO, LotteryPrizeDO.class);
                            if(ObjectUtil.isNotEmpty(lotteryPrizeReqVO.getWinningCitys())){
                                prize.setWinningCitys(convertListToString(lotteryPrizeReqVO.getWinningCitys()));
                            }else{
                                prize.setWinningCitys(null);
                            }
                            prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                            prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                            prize.setLotteryId(id);
                            lotteryPrizeMapper.insert(prize);
                        }

                    }
                }

                LambdaQueryWrapper<LotteryPrizeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId,lotterySettingsDO.getId());
                List<LotteryPrizeDO> lotteryPrizeDOS = lotteryPrizeMapper.selectList(lambdaQueryWrapper);
                lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + activityDO.getId(), lotteryPrizeDOS);

            }


            if(lotterySettingsUpdateReqVO.getPrizePoolRules().equals(2)) {
                //新增奖品逻辑
                if(ObjectUtil.isNotEmpty(lotterySettingsUpdateReqVO.getPrizes())){
                    LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettingsDO.getId());
                    List<LotteryPrizeDO> selectList = lotteryPrizeMapper.selectList(lotteryPrizeDOLambdaQueryWrapper);

                    Map<Long, List<LotteryPrizeDO>> groupedByStoreId = selectList.stream()
                            .collect(Collectors.groupingBy(LotteryPrizeDO::getStoreId));
                    List<LotteryPrizeDO> lotteryPrizeDOList = new ArrayList<>();
                    for (Long aLong : groupedByStoreId.keySet()) {
                        lotteryPrizeDOList = groupedByStoreId.get(aLong);

                        break;
                    }

                    //取对应的一组数据
                    if(ObjectUtil.isNotEmpty(lotteryPrizeDOList)){
                        List<String> stringList = lotteryPrizeDOList.stream().map(mm -> mm.getCode()).collect(Collectors.toList());
                        List<String> ss = lotterySettingsUpdateReqVO.getPrizes().stream().map(mm -> mm.getCode()).collect(Collectors.toList());

                        // 使用HashSet提高contains查询性能
                        Set<String> ssSet = new HashSet<>(ss);

                        List<String> missingCodes = stringList.stream()
                                .filter(code -> !ssSet.contains(code))
                                .collect(Collectors.toList());
                        if(ObjectUtil.isNotEmpty(missingCodes)){
                            LambdaQueryWrapper<LotteryPrizeDO> deleteWrapper = new LambdaQueryWrapper<>();
                            deleteWrapper.in(LotteryPrizeDO::getCode, missingCodes)
                                    .eq(LotteryPrizeDO::getLotteryId, lotterySettingsDO.getId()); // 可选：加上lotteryId条件

                            int deleteCount = lotteryPrizeMapper.delete(deleteWrapper);
                        }
                    }

                    for (LotteryPrizeReqVO pp : lotterySettingsUpdateReqVO.getPrizes()) {
                        if(ObjectUtil.isEmpty(pp.getCode())){
                            List<LotteryPrizeDO> list = new ArrayList<>();
                            if(lotterySettingsUpdateReqVO.getActivityStore().equals(0)){
                                String idString = generateStablePrizeCode(pp);
                                for (Long storeId : effectiveStoreIds) {
                                    LotteryPrizeDO prize = BeanUtils.toBean(pp, LotteryPrizeDO.class);
                                    if(ObjectUtil.isNotEmpty(pp.getWinningCitys())){
                                        prize.setWinningCitys(convertListToString(pp.getWinningCitys()));
                                    }else{
                                        prize.setWinningCitys(null);
                                    }
                                    prize.setCode(idString);
                                    prize.setStoreId(storeId);
//                                        prize.setCreateUserName(userName);
                                    prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                                        prize.setUpdateUserName(userName);
                                    prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                    prize.setLotteryId(id);
                                    list.add(prize);
                                }
                            }else if(lotterySettingsUpdateReqVO.getActivityStore().equals(1)){
                                CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
                                List<StoreInfoDTO> data = allStoreList.getData();
                                if(ObjectUtil.isNotEmpty(data)){
                                    for (StoreInfoDTO datum : data) {
                                        LotteryPrizeDO prize = BeanUtils.toBean(pp, LotteryPrizeDO.class);
                                        if(ObjectUtil.isNotEmpty(pp.getWinningCitys())){
                                            prize.setWinningCitys(convertListToString(pp.getWinningCitys()));
                                        }else{
                                            prize.setWinningCitys(null);
                                        }
                                        Long snowflakeId = generateSnowflakeId();
                                        prize.setCode(snowflakeId.toString());
                                        prize.setStoreId(datum.getStoreId());
//                                        prize.setCreateUserName(userName);
                                        prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                                        prize.setUpdateUserName(userName);
                                        prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                        prize.setLotteryId(id);
                                        list.add(prize);

                                    }
                                }
                            }
                            lotteryPrizeMapper.insertBatch(list);


                        }

                    }
                }
                // 批量更新所有关联的奖品数据
                List<LotteryPrizeReqVO> collect = lotterySettingsUpdateReqVO.getPrizes().stream()
                        .filter(item -> ObjectUtil.isNotEmpty(item.getCode())).collect(Collectors.toList());

                if(ObjectUtil.isNotEmpty(collect)){

                    List<LotteryPrizeDO> lotteryPrizeDOS = lotterySettingsUpdateReqVO.getPrizes().stream()
                            .filter(item -> ObjectUtil.isNotEmpty(item.getCode()))  // 过滤掉code为空的数据
                            .map(item -> {
                                LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                                if(ObjectUtil.isNotEmpty(item.getWinningCitys())){
                                    prize.setWinningCitys(convertListToString(item.getWinningCitys()));
                                }else{
                                    prize.setWinningCitys(null);
                                }
                                prize.setRemainNum(null);
                                prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                                prize.setId(null);
                                return prize;
                            })
                            .collect(Collectors.toList());


                    lotteryPrizeMapper.updateBatchByCode(lotteryPrizeDOS);
                    updateAllStorePrizeCache(lotterySettingsDO.getId(),activityDO.getId());
                }


            }
            lotterySettingsMapper.update(updateWrapper);
            activityService.updateActivity(activityDO);
            syncPrizeRecordAndInventoryByLogs(lotterySettingsDO.getId(), activityDO.getId(), lotterySettingsUpdateReqVO.getPrizePoolRules());
            // 记录操作日志上下文
            LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsUpdateReqVO);
            return success(1);
        } else {
            return null;
        }


    }

    public Set<Long> getDistinctStoreIds(Long id) {
        LambdaQueryWrapper<LotteryPrizeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryPrizeDO::getLotteryId,id);
        List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectList(wrapper);

        return prizes.stream()
                .map(LotteryPrizeDO::getStoreId)
                .collect(Collectors.toSet());
    }




    /**
     * 批量更新抽奖活动下的所有奖品数据
     */
//    private void batchUpdateAllPrizes(Long lotteryId, LotterySettingsUpdateReqVO updateReqVO) {
//        try {
//            // 1. 查询当前抽奖活动下的所有奖品
//            LambdaQueryWrapper<LotteryPrizeDO> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(LotteryPrizeDO::getLotteryId, lotteryId);
//            List<LotteryPrizeDO> allPrizes = lotteryPrizeMapper.selectList(queryWrapper);
//
//            if (ObjectUtil.isEmpty(allPrizes)) {
//                log.info("抽奖活动 {} 下没有奖品数据需要更新", lotteryId);
//                return;
//            }
//
//            // 2. 构建更新列表
//            List<LotteryPrizeDO> updateList = buildPrizeUpdateList(allPrizes, updateReqVO);
//
//            // 3. 执行批量更新
//            if (ObjectUtil.isNotEmpty(updateList)) {
//                boolean success = lotteryPrizeService.updateBatchById(updateList);
//                log.info("批量更新奖品数据完成: lotteryId={}, 更新数量={}, 结果={}",
//                        lotteryId, updateList.size(), success ? "成功" : "失败");
//
//                // 4. 更新缓存
//                updateAllStorePrizeCache(lotteryId);
//            }
//
//        } catch (Exception e) {
//            log.error("批量更新奖品数据失败: lotteryId={}", lotteryId, e);
//            throw new RuntimeException("批量更新奖品数据失败", e);
//        }
//    }

    /**
     * 构建奖品更新列表
     */
    private List<LotteryPrizeDO> buildPrizeUpdateList(List<LotteryPrizeDO> existingPrizes,
                                                      LotterySettingsUpdateReqVO updateReqVO) {
        List<LotteryPrizeDO> updateList = new ArrayList<>();
        String updater = String.valueOf(SecurityFrameworkUtils.getLoginUserId());

        for (LotteryPrizeDO prize : existingPrizes) {
            LotteryPrizeDO updatePrize = new LotteryPrizeDO();
            updatePrize.setId(prize.getId());

            // 设置通用更新字段
            updatePrize.setUpdater(updater);
            updatePrize.setUpdateTime(LocalDateTime.now());

            // 根据业务需求设置其他需要更新的字段
            // 例如：如果请求参数中有需要同步到所有奖品的字段
            // updatePrize.setSomeField(updateReqVO.getSomeField());

            updateList.add(updatePrize);
        }

        return updateList;
    }

    /**
     * 生成雪花ID
     */
    private long generateSnowflakeId() {
        // 使用自定义的雪花算法
        return SnowflakeIdGenerator.nextId();
    }

    /**
     * 更新所有门店的奖品缓存
     */
    private void updateAllStorePrizeCache(Long lotteryId,Long id) {
        try {
            // 查询所有门店ID
            QueryWrapper<LotteryPrizeDO> wrapper = new QueryWrapper<>();
            wrapper.eq("lottery_id", lotteryId)
                    .select("DISTINCT store_id");

            List<Object> storeIdObjects = lotteryPrizeMapper.selectObjs(wrapper);

            for (Object storeIdObj : storeIdObjects) {
                Long storeId = (Long) storeIdObj;

                // 查询该门店下的所有奖品
                LambdaQueryWrapper<LotteryPrizeDO> prizeWrapper = new LambdaQueryWrapper<>();
                prizeWrapper.eq(LotteryPrizeDO::getLotteryId, lotteryId)
                        .eq(LotteryPrizeDO::getStoreId, storeId);
                List<LotteryPrizeDO> storePrizes = lotteryPrizeMapper.selectList(prizeWrapper);

                // 更新缓存
                if (ObjectUtil.isNotEmpty(storePrizes)) {
                    String cacheKey = LOTTERY_PRIZE + id + ":store:" + storeId;
                    lotteryRedisDAO.setCacheObject(cacheKey, storePrizes);
                }
            }

            log.info("更新所有门店奖品缓存完成: lotteryId={}, 门店数量={}", lotteryId, storeIdObjects.size());

        } catch (Exception e) {
            log.error("更新奖品缓存失败: lotteryId={}", lotteryId, e);
        }
    }


















    public void delAllStore(Long id) {
        LotterySettingsDO settings = lotterySettingsMapper.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                .eq(LotterySettingsDO::getActivityId, id));
        if (settings != null) lotteryCachePublisher.enqueue(settings);
    }


    @Override
    @Transactional
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_DELETE_TYPE, bizNo = "{{#deleteID}}", success = SYSTEM_LOTTERY_LOG_DELETE_TYPE_SUCCESS)
    public CommonResult<Integer> removeLottery(long id) {

        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO != null) {
            if (activityDO.getIsEnabled() == 1) {
                return error(LOTTERY_DELETE);
            }
        }
        activityService.deleteActivity(id);

        LambdaQueryWrapper<LotterySettingsDO> lotterySettingsDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lotterySettingsDOLambdaQueryWrapper.eq(LotterySettingsDO::getActivityId, id);
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(lotterySettingsDOLambdaQueryWrapper);

        if (lotterySettings != null) {
            LambdaQueryWrapper<AdvertisingImageDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdvertisingImageDO::getActivityId, lotterySettings.getId());
            List<AdvertisingImageDO> advertisingImageDOS = advertisingImageMapper.selectList(wrapper);
            if (ObjectUtil.isNotEmpty(advertisingImageDOS)) {
                throw exception(LOTTERY_ADVERTISING_DELETE);
            }else{
                lotterySettings.setConfigVersion(lotterySettings.getConfigVersion() + 1);
                lotteryCachePublisher.enqueue(lotterySettings);
                lotterySettingsMapper.deleteById(lotterySettings.getId());
            }
            LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettings.getId());
            lotteryPrizeMapper.delete(lotteryPrizeDOLambdaQueryWrapper);
            delAllStore(lotterySettings.getActivityId());
            lotteryRedisDAO.deleteObject(LOTTERY_SETTING + activityDO.getId());
        }

        lotteryRedisDAO.deleteObject(LOTTERY_NUM);
//        lotteryRedisDAO.deleteObject(LOTTERY_PRIZE + lotterySettings.getId());
        lotteryRedisDAO.deleteObject(PRIZE_NUM);
        // 记录操作日志上下文
        LogRecordContext.putVariable("deleteID", id);
        return success(null);
    }

    @Override
    public LotterySettingsDetailDataRespVO getByDetail(long id) {
        LotterySettingsDetailDataRespVO detailDataRespVO = new LotterySettingsDetailDataRespVO();
        ActivityDO activityDO = activityService.selectById(id);
        // 列表主键是抽奖配置 ID；详情同时接受活动 ID 和配置 ID。
        Long activityId = id;
        if (activityDO == null) {
            LotterySettingsDO bySettingsId = lotterySettingsMapper.selectById(id);
            if (bySettingsId != null && bySettingsId.getActivityId() != null) {
                activityId = bySettingsId.getActivityId();
                activityDO = activityService.selectById(activityId);
            }
        }
        if(activityDO!=null){
            BeanUtils.copyProperties(activityDO, detailDataRespVO);
            detailDataRespVO.setAppScope(Optional.ofNullable(activityDO.getAppScope()).orElse(0));
            List<Long> selectedTags = lotteryScopeService.tagIds(activityId);
            detailDataRespVO.setTagIds(selectedTags);
            detailDataRespVO.setTagInfoDTOS(tagInfo(selectedTags, tagNames(selectedTags)));
            Date startDate = activityDO.getStartDate();
            Date endDate = activityDO.getEndDate();
            detailDataRespVO.setStartDate(startDate);
            detailDataRespVO.setEndDate(endDate);
            //设置门店信息
            if (Objects.equals(detailDataRespVO.getActivityStore(), 0)) {
                List<StoreInfoDTO> currentStores = activityStoreService.storesByActivityId(activityId);
                detailDataRespVO.setStoreInfoDTOS(currentStores == null ? new ArrayList<>() : currentStores);
                detailDataRespVO.setStoreIds(detailDataRespVO.getStoreInfoDTOS().stream()
                        .map(StoreInfoDTO::getStoreId).toList());
            } else {
                detailDataRespVO.setStoreInfoDTOS(new ArrayList<>());
                detailDataRespVO.setStoreIds(new ArrayList<>());
            }
            //将人群参数加入
            if (activityDO.getSelectedGroups() != null) {

                List<Long> longs = ConvertUtil.convertStringToList(activityDO.getSelectedGroups());
                List<ActivitySeckillCrowdRespVO> activitySeckillCrowdRespVOS = new ArrayList<>();

                List<CrowdNameDTO> byIds = crowdApi.getByIds(longs);
                if (ObjectUtil.isNotEmpty(byIds)) {
                    for (CrowdNameDTO byId : byIds) {
                        ActivitySeckillCrowdRespVO activitySeckillCrowdRespVO = new ActivitySeckillCrowdRespVO();
                        BeanUtils.copyProperties(byId, activitySeckillCrowdRespVO);
                        activitySeckillCrowdRespVOS.add(activitySeckillCrowdRespVO);
                    }
                    detailDataRespVO.setSelectedGroupList(byIds.stream().map(mm -> mm.getId()).collect(Collectors.toList()));
                } else {
                    detailDataRespVO.setSelectedGroupList(new ArrayList<>());
                }
                detailDataRespVO.setActivitySeckillCrowdRespVOList(activitySeckillCrowdRespVOS);
            } else {
                detailDataRespVO.setSelectedGroupList(new ArrayList<>());
            }


            LambdaQueryWrapper<LotterySettingsDO> lotterySettingsDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            lotterySettingsDOLambdaQueryWrapper.eq(LotterySettingsDO::getActivityId, activityId);
            LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(lotterySettingsDOLambdaQueryWrapper);
            if (lotterySettings != null) {

                if (lotterySettings.getActivityProduct().equals(2)) {
                    detailDataRespVO.setCommodityDTOList(activityLotteryCommodityService.listByActivityId(activityId));
                } else {
                    detailDataRespVO.setCommodityDTOList(new ArrayList<>());
                }

                BeanUtils.copyProperties(lotterySettings, detailDataRespVO);
                LambdaQueryWrapper<LotteryPrizeDO> lotteryPrizeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
                lotteryPrizeDOLambdaQueryWrapper.eq(LotteryPrizeDO::getLotteryId, lotterySettings.getId());
                List<LotteryPrizeDO> list = lotteryPrizeMapper.selectList(lotteryPrizeDOLambdaQueryWrapper);
                if (Objects.equals(lotterySettings.getRuntimeVersion(), 2))
                    lotteryLedger.fillDisplayStock(BusinessContextHolder.getRequiredBusinessId(), activityId, lotterySettings.getStockEpoch(), list);

                List<LotteryPrizeReqVO> lotteryPrizeReqVOList = new ArrayList<>();
                if(lotterySettings.getPrizePoolRules().equals(2)){

                    Map<Long, List<LotteryPrizeDO>> groupedByStoreId = list.stream()
                            .collect(Collectors.groupingBy(LotteryPrizeDO::getStoreId));
                    List<LotteryPrizeDO> lotteryPrizeDOList = new ArrayList<>();
                    for (Long aLong : groupedByStoreId.keySet()) {
                        lotteryPrizeDOList = groupedByStoreId.get(aLong);

                        break;
                    }

                    Map<String, Integer> codeToSumMap = list.stream()
                            .collect(Collectors.groupingBy(
                                    LotteryPrizeDO::getCode,
                                    Collectors.summingInt(LotteryPrizeDO::getRemainNum)
                            ));

                    if(ObjectUtil.isNotEmpty(lotteryPrizeDOList)){
                        for (LotteryPrizeDO lotteryPrizeDO : lotteryPrizeDOList) {
                            LotteryPrizeReqVO lotteryPrizeReqVO = new LotteryPrizeReqVO();
                            BeanUtils.copyProperties(lotteryPrizeDO, lotteryPrizeReqVO);
                            if (ObjectUtil.isNotEmpty(lotteryPrizeDO.getWinningCitys())) {
                                lotteryPrizeReqVO.setWinningCitys(setWinning(lotteryPrizeDO.getWinningCitys()));
                            }
                            String code = lotteryPrizeDO.getCode();
                            Integer totalRemainNum = codeToSumMap.get(code);
                            if (totalRemainNum != null) {
                                lotteryPrizeReqVO.setRemainNum(totalRemainNum);
                            } else {
                                // 如果map中没有对应的code，使用原来的值或0
                                lotteryPrizeReqVO.setRemainNum(lotteryPrizeDO.getRemainNum());
                            }
                            lotteryPrizeReqVOList.add(lotteryPrizeReqVO);
                        }
                    }

                }else{
                    for (LotteryPrizeDO lotteryPrizeDO : list) {
                        LotteryPrizeReqVO lotteryPrizeReqVO = new LotteryPrizeReqVO();
                        BeanUtils.copyProperties(lotteryPrizeDO, lotteryPrizeReqVO);
                        if (ObjectUtil.isNotEmpty(lotteryPrizeDO.getWinningCitys())) {
                            lotteryPrizeReqVO.setWinningCitys(setWinning(lotteryPrizeDO.getWinningCitys()));
                        }
                        lotteryPrizeReqVOList.add(lotteryPrizeReqVO);
                    }
                }

                if (lotteryPrizeReqVOList.isEmpty()) lotteryPrizeReqVOList = lotteryScopeService.template(lotterySettings);
                detailDataRespVO.setPrizes(lotteryPrizeReqVOList);
            }

        }



        return detailDataRespVO;
    }

    public List<String> setWinning(String winning) {
        List<String> stringList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(winning)) {

            String[] array = winning.split(",");
            for (int i = 0; i < array.length; i++) {
                stringList.add(array[i]);
            }
            return stringList;
        } else {
            return stringList;
        }
    }

    @Override
    public CommonResult<Integer> copyLotteryPrize(LotterySettingsReqVO lotterySettingsVO) {
        lotteryRuntime.rejectLegacyWrite(lotterySettingsVO.getId());

        //check name
        checkNameReplace(null, lotterySettingsVO.getLotteryTitle());

        // 判断奖品存在唯一兜底商品
        // 检查保底商品数量
        if (!validateGuaranteedPrize(lotterySettingsVO)) {
            throw exception(LOTTERY_GUARANTEED_PRODUCTS);
        }
        // 检查总概率
        if (!validateTotalProbability(lotterySettingsVO)) {
            return error(LOTTERY_WINNING_PROBABILITY);
        }
        LotterySettingsDO lotterySettings = BeanUtils.toBean(lotterySettingsVO, LotterySettingsDO.class);

        lotterySettings.setCreateTime(LocalDateTime.now());
        lotterySettings.setUpdateTime(LocalDateTime.now());
        String userName = SecurityFrameworkUtils.getLoginUsername();
        lotterySettings.setCreateUserName(userName);
        lotterySettings.setUpdateUserName(userName);
        lotterySettings.setUpdateTime(LocalDateTime.now());
//        lotterySettings.setIsDelete(0);
        lotterySettings.setId(null);
        lotterySettingsMapper.insert(lotterySettings);
        List<LotteryPrizeDO> list = lotterySettingsVO.getPrizes().stream().map(item -> {
                    LotteryPrizeDO prize = BeanUtils.toBean(item, LotteryPrizeDO.class);
                    prize.setCreateUserName(userName);
                    prize.setCreator(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
                    // todo 获取用户userName
                    prize.setUpdateUserName(userName);
                    prize.setUpdater(String.valueOf(SecurityFrameworkUtils.getLoginUserId()));
//                    prize.setIsDelete(0);
                    prize.setLotteryId(lotterySettings.getId());
                    return prize;
                })
                .collect(Collectors.toList());
        //添加活动缓存
        lotteryRedisDAO.setCacheObject(LOTTERY_SETTING + lotterySettings.getId(), lotterySettings);

        lotteryPrizeMapper.insertBatch(list);
        //添加奖品缓存
        lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotterySettings.getId(), list);
        // 记录操作日志上下文
        LogRecordContext.putVariable("lotterySettingsVO", lotterySettingsVO);
        return success(null);
    }

    private void checkNameReplace(Object o, String lotteryTitle) {
        LambdaQueryWrapperX<LotterySettingsDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(LotterySettingsDO::getLotteryTitle, lotteryTitle);

        if (lotterySettingsMapper.exists(queryWrapperX)) {
            throw exception(ErrorCodeConstants.LOTTERY_TITLE_HAVING);
        }
    }

    // 检查总概率

    private boolean validateTotalProbability(LotterySettingsReqVO lotterySettingsVO) {
        BigDecimal totalProbability = lotterySettingsVO.getPrizes().stream()
                .map(LotteryPrizeReqVO::getProbability) // 获取 BigDecimal 类型的概率
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加概率

        return totalProbability.compareTo(new BigDecimal("100")) == 0;
    }


    // 检查总概率

    private boolean validateTotalProbability(LotterySettingsAddReqVO lotterySettingsVO) {
        BigDecimal totalProbability = lotterySettingsVO.getPrizes().stream()
                .map(LotteryPrizeReqVO::getProbability) // 获取 BigDecimal 类型的概率
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加概率

        return totalProbability.compareTo(new BigDecimal("100")) == 0;
    }

    // 检查保底商品数量
    private boolean validateGuaranteedPrize(LotterySettingsReqVO lotterySettingsVO) {
        long count = lotterySettingsVO.getPrizes().stream()
                .filter(item -> item.getIsGuarantees() == 1)
                .count();
        return count == 1;
    }


    // 检查保底商品数量
    private boolean validateGuaranteedPrize(LotterySettingsAddReqVO lotterySettingsVO) {
        long count = lotterySettingsVO.getPrizes().stream()
                .filter(item -> item.getIsGuarantees() == 1)
                .count();
        return count == 1;
    }


    private boolean validateTotalProbability(LotterySettingsUpdateReqVO lotterySettingsVO) {
        BigDecimal totalProbability = lotterySettingsVO.getPrizes().stream()
                .map(LotteryPrizeReqVO::getProbability) // 获取 BigDecimal 类型的概率
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加概率

        return totalProbability.compareTo(new BigDecimal("100")) == 0;
    }

    // 检查保底商品数量
    private boolean validateGuaranteedPrize(LotterySettingsUpdateReqVO lotterySettingsVO) {
        long count = lotterySettingsVO.getPrizes().stream()
                .filter(item -> item.getIsGuarantees() == 1)
                .count();
        return count == 1;
    }


    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * 新增时添加默认短链
     */
    private void createChannelDO(Long activityId, int type) {
        activityChannelService.createChannelDO(activityId, type);
    }


    /**
     * 查询推广
     *
     * @param id
     * @return
     */
    @Override
    public LotterySpreadRespVO selectSpread(Long id) {
        LotterySpreadRespVO lotterySpreadRespVO = new LotterySpreadRespVO();


        LambdaQueryWrapper<LotterySettingsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotterySettingsDO::getActivityId, id);
        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(wrapper);

        BeanUtils.copyProperties(lotterySettingsDO, lotterySpreadRespVO);

        //List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityId(id);
        //List<ActivityChannelDO> activityChannelDOList = activityChannelService.selectByActivityIdWithIsEnable(id);
        /*if (!activityChannelDOList.isEmpty()) {
            List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();

            activityChannelDOList.forEach(activityChannelDO -> {
                ActivityChannelRespVO activityChannelRespVO = new ActivityChannelRespVO();
                BeanUtils.copyProperties(activityChannelDO, activityChannelRespVO);
                activityChannelRespVOS.add(activityChannelRespVO);
            });

            lotterySpreadRespVO.setActivityChannelRespVOS(activityChannelRespVOS);
        }*/
        lotterySpreadRespVO.setId(id);

        return lotterySpreadRespVO;
    }


    /**
     * 修改或新增推广
     */

    @Override
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_UPDATE_SPREED, bizNo = "{{#lotterySpreadSaveReqVO.id}}", success = SYSTEM_LOTTERY_LOG_UPDATE_SPREED_SUCCESS)
    public void updateSpread(LotterySpreadSaveReqVO lotterySpreadSaveReqVO) {
        LambdaUpdateWrapper<LotterySettingsDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LotterySettingsDO::getActivityId, lotterySpreadSaveReqVO.getId());
        updateWrapper.set(LotterySettingsDO::getShareTitle, lotterySpreadSaveReqVO.getShareTitle());
        updateWrapper.set(LotterySettingsDO::getShareImgUrl, lotterySpreadSaveReqVO.getShareImgUrl());
        updateWrapper.set(LotterySettingsDO::getShareNote, lotterySpreadSaveReqVO.getShareNote());
        lotterySettingsMapper.update(updateWrapper);

        int type = switch (lotterySpreadSaveReqVO.getLotteryType()) {
            case 1 -> ActivityChannelTypeEnum.TURNTABLE.getCode();
            case 3 -> ActivityChannelTypeEnum.FORTUNE.getCode();
            default -> 0; // 原代码默认值
        };

        //新建并修改推广链接
//        activityChannelService.createAndUpdateChannel(lotterySpreadSaveReqVO.getActivityChannelList(), lotterySpreadSaveReqVO.getId(), type);

        List<LotterySettingsDO> lotterySettingsList = lotterySettingsMapper.selectList(new LambdaQueryWrapperX<LotterySettingsDO>()
                .eq(LotterySettingsDO::getDeleted, false).eq(LotterySettingsDO::getActivityId, lotterySpreadSaveReqVO.getId()));


        if (CollectionUtil.isNotEmpty(lotterySettingsList)) {
            LotterySettingsDO config = lotterySettingsList.get(0);
            config.setConfigVersion(config.getConfigVersion() + 1);
            lotterySettingsMapper.updateById(config);
            lotteryCachePublisher.enqueue(config);

        }
        LogRecordContext.putVariable("lotterySpreadSaveReqVO", lotterySpreadSaveReqVO);

    }

    @Override
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE, bizNo = "{{#lotterySettingsVO.id}}", success = SYSTEM_LOTTERY_LOG_UPDATE_STATE_TYPE_SUCCESS)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> updateState(LotterySettingsReqVO lotterySettingsVO) {
        if (lotterySettingsVO.getId() == null || !lotteryRuntime.changeState(lotterySettingsVO.getId(), lotterySettingsVO.getIsEnabled()))
            throw exception(LOTTERY_NOT_NULL);
        return success(1);
    }

    @Override
    public void lotteryPoolReset() {
        log.info("====== 开始执行奖池规则重置定时任务 ======");
        LocalDate today = LocalDate.now();
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalTime currentTime = currentDateTime.toLocalTime().withSecond(0).withNano(0);

        // 1. 筛选基础符合条件的活动（开启重置、启用状态、在活动日期内）
        List<LotterySettingsDO> resetActivities = lotterySettingsMapper.selectList(
                new LambdaQueryWrapperX<LotterySettingsDO>()
                        .eq(LotterySettingsDO::getRuntimeVersion, 2) // 历史活动重启后才进入新版场次重置
                        .eq(LotterySettingsDO::getLotteryRulesReset, 1) // 开启奖池重置
                        .eq(LotterySettingsDO::getState, 1) // 活动状态启用
                        .le(LotterySettingsDO::getLotteryStartTime, today) // 开始日期≤今天
                        .ge(LotterySettingsDO::getLotteryEndTime, today) // 结束日期≥今天
                        .orderByAsc(LotterySettingsDO::getLotteryType)
        );

        if (CollectionUtils.isEmpty(resetActivities)) {
            log.info("无符合基础条件的活动，任务结束");
            return;
        }

        // 2. 查询关联的ActivityDO和门店信息
        List<Long> activityIds = resetActivities.stream()
                .map(LotterySettingsDO::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, ActivityDO> activityMap = activityIds.isEmpty() ? Collections.emptyMap() :
                activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>().in(ActivityDO::getId, activityIds))
                        .stream()
                        .collect(Collectors.toMap(ActivityDO::getId, Function.identity()));

        Map<Long, List<Long>> activityStoreMap = activityIds.isEmpty() ? Collections.emptyMap() :
                activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>().in(ActivityStoreDO::getActivityId, activityIds))
                        .stream()
                        .collect(Collectors.groupingBy(
                                ActivityStoreDO::getActivityId,
                                Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())
                        ));

        // 3. 过滤出符合重置条件的活动（核心筛选逻辑）
        List<LotterySettingsDO> filteredActivities = resetActivities.stream()
                .filter(activity -> {
                    Long activityId = activity.getActivityId();
                    if (activityId == null) {
                        log.warn("活动ID为空，跳过筛选");
                        return false;
                    }

                    // 3.1 校验关联的ActivityDO是否存在
                    ActivityDO activityDO = activityMap.get(activityId);
                    if (activityDO == null) {
                        log.warn("活动ID={}未查询到对应的ActivityDO，跳过", activityId);
                        return false;
                    }

                    // 3.2 校验活动时间有效性（包含当前有效或后续有场次）
                    String timeRange = activityDO.getTimeRange();
                    boolean isTimeValid;
                    try {
                        if (StringUtils.isBlank(timeRange)) {
                            // timeRange为空视为全天有效
                            isTimeValid = true;
                        } else {
                            // 校验当前是否在有效场次内
                            boolean currentValid = TimeValidationUtil.isTimeValid(
                                    activityDO.getStartDate(),
                                    activityDO.getEndDate(),
                                    activityDO.getDayNumbers(),
                                    activityDO.getWeekNumbers(),
                                    timeRange
                            );
                            // 校验今日是否有后续场次
                            boolean hasFutureSession = getNextSessionStartTime(activityDO, currentTime) != null;
                            isTimeValid = currentValid || hasFutureSession;
                        }
                    } catch (Exception e) {
                        log.error("活动ID={}时间校验异常，跳过", activityId, e);
                        return false;
                    }

                    if (!isTimeValid) {
                        log.debug("活动ID={}不在有效时间范围且无后续场次，跳过", activityId);
                        return false;
                    }

                    // 3.3 排除当前正在进行的场次（避免干扰）
                    if (isInAnySession(activityDO, currentTime)) {
                        log.info("活动ID={}当前在场次进行中（{}），不执行重置", activityId, currentTime);
                        return false;
                    }

                    // 3.4 校验距离下次场次的间隔（≥5分钟才允许重置）
                    LocalTime nextSessionStartTime = getNextSessionStartTime(activityDO, currentTime);
                    if (nextSessionStartTime == null) {
                        log.debug("活动ID={}今日无后续场次，不重置", activityId);
                        return false;
                    }
                    long minutesToNext = ChronoUnit.MINUTES.between(currentTime, nextSessionStartTime);
                    if (minutesToNext < 10) {
                        log.debug("活动ID={}距离下次场次仅{}分钟，不重置", activityId, minutesToNext);
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // 4. 执行奖池重置（含重复重置防护）
        for (LotterySettingsDO activity : filteredActivities) {
            Long activityId = activity.getActivityId();
            Long lotteryId = activity.getId();
            Integer prizePoolRules = activity.getPrizePoolRules();
            if (Objects.equals(activity.getRuntimeVersion(), 2)) {
                ActivityDO main = activityMap.get(activityId);
                lotteryRuntime.reset(activityId, currentDateTime.toLocalDate() + "_" + getNextSessionStartTime(main, currentTime));
                continue;
            }


            if (prizePoolRules == null) {
                log.warn("活动ID={}未配置奖池规则（prizePoolRules为空），跳过", activityId);
                continue;
            }
            // 定义格式（例如：yyyy-MM-dd HH:mm:ss）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 转换为字符串
            String dateTimeStr = currentDateTime.format(formatter);
            // 4.1 检查1小时内是否已重置过（避免重复）
            String resetCacheKey = "lottery:reset:latest:" + activityId;
            String latestResetTime =  redisCache.getCacheObject(resetCacheKey);
            LocalDateTime latestResetTimeDateTime = latestResetTime != null ? LocalDateTime.parse(latestResetTime, formatter) : null;
            if (latestResetTimeDateTime != null) {
                long minutesSinceLast = ChronoUnit.MINUTES.between(latestResetTimeDateTime, currentDateTime);
                if (minutesSinceLast < 60) {
                    log.info("活动ID={}最近{}分钟内已重置过，跳过", activityId, minutesSinceLast);
                    continue;
                }
            }

            try {
                // 4.2 查询关联奖品（区分奖池规则）
                LambdaQueryWrapperX<LotteryPrizeDO> prizeQuery = new LambdaQueryWrapperX<LotteryPrizeDO>()
                        .eq(LotteryPrizeDO::getLotteryId, lotteryId);
                if (prizePoolRules == 2) { // 独立奖池：仅查询关联门店的奖品
                    List<Long> storeIds = activityStoreMap.getOrDefault(activityId, Collections.emptyList());
                    if (CollectionUtils.isEmpty(storeIds)) {
                        log.warn("活动ID={}为独立奖池但未关联任何门店，跳过", activityId);
                        continue;
                    }
                    prizeQuery.in(LotteryPrizeDO::getStoreId, storeIds);
                }

                List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectList(prizeQuery);
                if (CollectionUtils.isEmpty(prizes)) {
                    log.warn("活动ID={}未查询到关联奖品（规则={}），跳过", activityId, prizePoolRules);
                    continue;
                }

                // 4.3 重置奖品已领取数量（remainNum=0）
                List<LotteryPrizeDO> updatePrizes = prizes.stream()
                        .peek(prize -> prize.setRemainNum(0))
                        .collect(Collectors.toList());

                // 4.4 批量更新数据库
                boolean updateSuccess = lotteryPrizeMapper.updateBatch(updatePrizes);
                if (!updateSuccess) {
                    log.error("活动ID={}奖品重置失败（数据库更新失败）", activityId);
                    continue;
                }

                // 4.5 按奖池规则更新缓存（先删后更，确保一致性）
                if (prizePoolRules == 2) { // 独立奖池：按门店更新缓存
                    List<Long> storeIds = activityStoreMap.get(activityId);
                    for (Long storeId : storeIds) {
                        String cacheKey = buildPrizeCacheKey(activityId, prizePoolRules, storeId);
                        lotteryRedisDAO.deleteObject(cacheKey);
                        List<LotteryPrizeDO> storePrizes = prizes.stream()
                                .filter(p -> p.getStoreId().equals(storeId))
                                .collect(Collectors.toList());
                        lotteryRedisDAO.setCacheObject(cacheKey, storePrizes);
                        log.debug("活动ID={}独立奖池缓存更新：门店={}，奖品数={}", activityId, storeId, storePrizes.size());
                    }
                } else { // 共用奖池：更新全局缓存
                    String cacheKey = buildPrizeCacheKey(activityId, prizePoolRules, null);
                    lotteryRedisDAO.deleteObject(cacheKey);
                    lotteryRedisDAO.setCacheObject(cacheKey, prizes);
                    log.debug("活动ID={}共用奖池缓存更新：奖品数={}", activityId, prizes.size());
                }

                // 4.6 记录重置时间（缓存1小时，避免重复）
                redisCache.setCacheObject(resetCacheKey, dateTimeStr, 1, TimeUnit.HOURS);
                log.info("活动ID={}奖池重置成功（规则={}），共重置{}个奖品", activityId, prizePoolRules, prizes.size());

            } catch (Exception e) {
                log.error("活动ID={}奖池重置异常（规则={}）", activityId, prizePoolRules, e);
            }
        }

        log.info("====== 奖池规则重置定时任务执行完毕 ======");
    }

// ------------------------------ 辅助方法 ------------------------------


/**
 * 判断当前时间是否在任意场次进行中
 */
private boolean isInAnySession(ActivityDO activityDO, LocalTime currentTime) {
    String timeRange = activityDO.getTimeRange();
    if (StringUtils.isBlank(timeRange)) {
        return false; // timeRange为空视为无场次限制
    }

    for (String session : timeRange.split(",")) {
        session = session.trim();
        if (session.isEmpty()) continue;

        String[] segments = session.split("-");
        if (segments.length != 2) continue;

        LocalTime startTime = parseTime(segments[0], activityDO.getId());
        LocalTime endTime = parseTime(segments[1], activityDO.getId());
        if (startTime == null || endTime == null) continue;

        // 判断当前时间是否在[startTime, endTime)范围内
        if ((currentTime.isAfter(startTime) && currentTime.isBefore(endTime))
                || currentTime.equals(startTime)) {
            return true;
        }
    }
    return false;
}

/**
 * 获取距离当前时间最近的下一场次开始时间（今日内）
 */
private LocalTime getNextSessionStartTime(ActivityDO activityDO, LocalTime currentTime) {
    String timeRange = activityDO.getTimeRange();
    if (StringUtils.isBlank(timeRange)) {
        return null; // 无场次定义
    }

    List<LocalTime> startTimes = new ArrayList<>();
    for (String session : timeRange.split(",")) {
        session = session.trim();
        if (session.isEmpty()) continue;

        String[] segments = session.split("-");
        if (segments.length != 2) continue;

        LocalTime startTime = parseTime(segments[0], activityDO.getId());
        if (startTime != null) {
            startTimes.add(startTime);
        }
    }

    // 筛选当前时间之后的开始时间，取最小的一个
    return startTimes.stream()
            .filter(start -> start.isAfter(currentTime))
            .min(LocalTime::compareTo)
            .orElse(null);
}

/**
 * 解析时间字符串为LocalTime（HH:mm格式）
 */
private LocalTime parseTime(String timeStr, Long activityId) {
    try {
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
    } catch (DateTimeParseException e) {
        log.error("活动ID={}时间格式错误：{}（应为HH:mm）", activityId, timeStr);
        return null;
    }
}


/**
 * 按奖池规则构建奖品缓存键
 */
private String buildPrizeCacheKey(Long lotteryId, Integer prizePoolRules, Long storeId) {
    if (prizePoolRules == 1) {
        // 共用奖池：缓存键仅包含活动ID（全局唯一）
        return RedisKeyConstants.LOTTERY_PRIZE + lotteryId;
    } else {
        // 独立奖池：缓存键包含活动ID+门店ID（门店级唯一，避免跨门店缓存污染）
        if (storeId == null) {
            throw new IllegalArgumentException("独立奖池模式下，门店ID不能为空（缓存键构建失败）");
        }
        return RedisKeyConstants.LOTTERY_PRIZE + lotteryId + ":store:" + storeId;
    }
}

private void syncPrizeRecordAndInventoryByLogs(Long lotteryId, Long activityId, Integer prizePoolRules) {
    List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectList(new LambdaQueryWrapperX<LotteryPrizeDO>()
            .eq(LotteryPrizeDO::getLotteryId, lotteryId));
    if (CollectionUtils.isEmpty(prizes)) {
        lotteryRedisDAO.deleteObject(LOTTERY_PRIZE + lotteryId);
        if (activityId != null && !Objects.equals(activityId, lotteryId)) {
            lotteryRedisDAO.deleteObject(LOTTERY_PRIZE + activityId);
        }
        return;
    }

    Integer actualPrizePoolRules = ObjectUtil.defaultIfNull(prizePoolRules, 1);
    Map<Long, Integer> prizeUsedCountMap = countPrizeUsedByLotteryLogs(lotteryId, activityId, actualPrizePoolRules, prizes);
    for (LotteryPrizeDO prize : prizes) {
        Integer usedCount = prizeUsedCountMap.getOrDefault(prize.getId(), 0);
        if (!Objects.equals(prize.getRemainNum(), usedCount)) {
            LotteryPrizeDO updateDO = new LotteryPrizeDO();
            updateDO.setId(prize.getId());
            updateDO.setRemainNum(usedCount);
            lotteryPrizeMapper.updateById(updateDO);
            prize.setRemainNum(usedCount);
        }
        stringRedisTemplate.opsForValue().set(
                buildPrizeStockKey(lotteryId, prize.getId(), actualPrizePoolRules, prize.getStoreId()),
                String.valueOf(usedCount)
        );
        if (activityId != null && !Objects.equals(activityId, lotteryId)) {
            stringRedisTemplate.opsForValue().set(
                    buildPrizeStockKey(activityId, prize.getId(), actualPrizePoolRules, prize.getStoreId()),
                    String.valueOf(usedCount)
            );
        }
    }

    syncPrizeListCache(lotteryId, activityId, actualPrizePoolRules, prizes);
}

private Map<Long, Integer> countPrizeUsedByLotteryLogs(Long lotteryId, Long activityId, Integer prizePoolRules, List<LotteryPrizeDO> prizes) {
    Long actualActivityId = activityId == null ? lotteryId : activityId;
    if (!Objects.equals(prizePoolRules, 2)) {
        return buildPrizeUsedCountMap(lotteryLogMapper.selectPrizeUsedCountGroupByPrizeId(lotteryId, actualActivityId));
    }
    Map<Long, Integer> result = new HashMap<>();
    Set<Long> storeIds = prizes.stream()
            .map(LotteryPrizeDO::getStoreId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(storeIds)) {
        return Collections.emptyMap();
    }
    for (Long storeId : storeIds) {
        Map<Long, Integer> storeCountMap = buildPrizeUsedCountMap(
                lotteryLogMapper.selectPrizeUsedCountGroupByPrizeIdAndStoreId(lotteryId, actualActivityId, storeId)
        );
        result.putAll(storeCountMap);
    }
    return result;
}

private Map<Long, Integer> buildPrizeUsedCountMap(List<Map<String, Object>> countRows) {
    if (CollectionUtils.isEmpty(countRows)) {
        return Collections.emptyMap();
    }
    return countRows.stream()
            .filter(Objects::nonNull)
            .filter(row -> row.get("lotteryPrizeId") != null && row.get("usedCount") != null)
            .collect(Collectors.toMap(
                    row -> ((Number) row.get("lotteryPrizeId")).longValue(),
                    row -> ((Number) row.get("usedCount")).intValue()
            ));
}

private void syncPrizeListCache(Long lotteryId, Long activityId, Integer prizePoolRules, List<LotteryPrizeDO> prizes) {
    if (Objects.equals(prizePoolRules, 2)) {
        Map<Long, List<LotteryPrizeDO>> storePrizeMap = prizes.stream()
                .filter(prize -> prize.getStoreId() != null)
                .collect(Collectors.groupingBy(LotteryPrizeDO::getStoreId));
        for (Map.Entry<Long, List<LotteryPrizeDO>> entry : storePrizeMap.entrySet()) {
            Long storeId = entry.getKey();
            List<LotteryPrizeDO> storePrizes = entry.getValue();
            lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotteryId + ":store:" + storeId, storePrizes);
            if (activityId != null && !Objects.equals(activityId, lotteryId)) {
                lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + activityId + ":store:" + storeId, storePrizes);
            }
        }
        return;
    }

    lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + lotteryId, prizes);
    if (activityId != null && !Objects.equals(activityId, lotteryId)) {
        lotteryRedisDAO.setCacheObject(LOTTERY_PRIZE + activityId, prizes);
    }
}

private String buildPrizeStockKey(Long lotteryId, Long prizeId, Integer prizePoolRules, Long storeId) {
    if (Objects.equals(prizePoolRules, 2)) {
        return RedisKeyConstants.PRIZE_NUM + lotteryId + ":store:" + storeId + ":prize:" + prizeId;
    }
    return RedisKeyConstants.PRIZE_NUM + lotteryId + ":prize:" + prizeId;
}

}
