package com.htyoudao.youdao.module.promotion.service.usercoupon;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.ItemDto;
import com.htyoudao.youdao.module.member.api.point.PointLogApi;
import com.htyoudao.youdao.module.member.api.point.dto.PointsLogDTO;
import com.htyoudao.youdao.module.member.api.pointsproduct.PointsProductApi;
import com.htyoudao.youdao.module.member.api.pointsproduct.dto.PointsProductDTO;
import com.htyoudao.youdao.module.member.api.wecom.WecomGroupApi;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDCouponPackageRespSaveVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDCouponRespSaveVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.enums.IsGroundConstant;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CouponNumDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.*;
import com.htyoudao.youdao.module.promotion.constant.CouponSourceConstant;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
import com.htyoudao.youdao.module.promotion.context.CouponTypeContext;
import com.htyoudao.youdao.module.promotion.context.UserCouponCommodityContext;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponPackageReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo.CouponDataSumExportExcel;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponCountRespVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponListExcelVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageReqVo;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponPageRespVo;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.core.calc.OrderCacheService;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CalculateCacheDataCopyDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponRedeem.DouyinCouponRedeemRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangecommodity.ExchangeCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponcommodity.CouponCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstore.CouponStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.exchangecommodity.ExchangeCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordMapper;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.enums.CouponStatusEnum;
import com.htyoudao.youdao.module.promotion.framework.config.PromotionExportProperties;
import com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon.ActivitySeckillCouponService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillActivityCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillCouponStockCacheService;
import com.htyoudao.youdao.module.promotion.service.couponRedeem.DouyinCouponRedeemRecordService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageMasterService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.couponshare.CouponShareService;
import com.htyoudao.youdao.module.promotion.service.douyin.DyClient;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.*;
import com.htyoudao.youdao.module.promotion.service.exchangelog.ExchangeLogService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.goodcouponpackage.GoodCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.ratelimit.RateLimitService;
import com.htyoudao.youdao.module.promotion.util.CouponCountUtil;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;

import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/**
 * 用户优惠券 Service 实现类
 *
 * @author dht
 */
@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class UserCouponServiceImpl implements UserCouponService {

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private PromotionExportProperties promotionExportProperties;

    @Resource
    private UserCouponMasterService userCouponMasterService;

    @Resource
    private JobService jobService;

    @Resource
    private CouponCommodityMapper couponCommodityMapper;

    @Resource
    private CouponStoreMapper couponStoreMapper;

    @Resource
    private CouponShareService couponShareService;

    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private UserCouponRecordMapper userCouponRecordMapper;

    @Resource
    private CouponTypeContext couponTypeContext;

    @Resource
    @Qualifier("habitCanUse")
    private CouponCanUseService couponCanUseService;

    @Resource
    @Qualifier("claimCouponEmpty")
    private ClaimCouponService claimCouponService;

    private static final RateLimiter rateLimiter = RateLimiter.create(5);

    private static final RateLimiter seckillLimiter = RateLimiter.create(3);

    private static final RateLimiter collectPointsLimiter = RateLimiter.create(3);
    @DubboReference
    private WxMemberApi wxMemberApi;

    @DubboReference
    private PointsProductApi pointsProductApi;

    @DubboReference
    private PointLogApi pointLogApi;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CommodityApi commodityApi;

    @Resource
    @Qualifier("promotionThreadPool") // 注入配置的全局线程池
    private Executor promotionThreadPool;

    @Resource
    private DyClient dyClient;

    @Resource
    private DouyinCouponRedeemRecordService douyinCouponRedeemRecordService;


    @Resource
    private RateLimitService rateLimitService;
    @Autowired
    private CouponPackageMapper couponPackageMapper;
    @Autowired
    private GoodCouponPackageMapper goodCouponPackageMapper;

    @Autowired
    private GoodCouponPackageService goodCouponPackageService;

    @Resource
    private CouponPackageMasterService couponPackageMasterService;

    @Resource
    @Lazy
    private CouponPackageService couponPackageService;

    @Resource
    private ExchangeCommodityMapper exchangeCommodityMapper;

    @DubboReference
    private StoreApi storeApi;

    @Resource
    private SeckillCouponStockCacheService seckillCouponStockCacheService;

    @Resource
    private ActivitySeckillCouponService activitySeckillCouponService;

    @Resource
    private ActivityAppService activityAppService;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @Resource
    private ActivityJDCacheService activityJDCacheService;

    @Resource
    private ExchangeLogService exchangeLogService;
    @Qualifier("businessContextWebFilter")
    @Autowired
    private FilterRegistrationBean businessContextWebFilter;

    @Resource
    private OrderCacheService orderCacheService;

    @DubboReference
    private WecomGroupApi wecomGroupApi;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private ExcelActionService excelActionService;

    @Override
    public Long getCountNum(Long userId, Integer isUsed) {
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_used", isUsed);
        queryWrapper.geSql("expiration_time", "now()");
        return userCouponMapper.selectCount(queryWrapper);
    }
    @Override
    public PageResult<AppCouponListRespVO> selectUserCouponListByUserId(AppCouponListReqVO appCouponListReqVO) {
        Date now = new Date();
        Date sixMonthsAgo = DateUtil.offsetMonth(now, -6);

        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(appCouponListReqVO)) {
            if (ObjectUtil.isNotEmpty(appCouponListReqVO.getUserId())) {
                queryWrapper.eq("user_id", appCouponListReqVO.getUserId());
            }
            if (ObjectUtil.isNotEmpty(appCouponListReqVO.getIsUsed())) {
                if (appCouponListReqVO.getIsUsed().equals(UserCouponConstants.IS_USED_2)) {
                    queryWrapper.eq("is_used", UserCouponConstants.IS_USED_0);
                    queryWrapper.ge("vaild_start_time", sixMonthsAgo);
                    queryWrapper.le("vaild_start_time", now);
                    queryWrapper.le("expiration_time", now);
                } else if (appCouponListReqVO.getIsUsed().equals(UserCouponConstants.IS_USED_1)) {
                    queryWrapper.eq("is_used", UserCouponConstants.IS_USED_1);
                    queryWrapper.ge("vaild_start_time", sixMonthsAgo);
                    queryWrapper.le("vaild_start_time", now);
                } else {
                    queryWrapper.eq("is_used", appCouponListReqVO.getIsUsed());
                    queryWrapper.gt("expiration_time", now);
                }
            }

            if (ObjectUtil.isNotEmpty(appCouponListReqVO.getDistributionMethod())) {
                queryWrapper.eq("distribution_method", appCouponListReqVO.getDistributionMethod());
            }
            if (ObjectUtil.isNotEmpty(appCouponListReqVO.getCouponCode())) {
                queryWrapper.eq("coupon_code", appCouponListReqVO.getCouponCode());
            }
        }

        if (ObjectUtil.isNotEmpty(appCouponListReqVO.getHabit())) {
            if (ObjectUtil.equals(appCouponListReqVO.getHabit(), UserCouponConstants.HABIT_2)) {
                queryWrapper.lambda().in(UserCouponDO::getHabit, List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_2));
            } else {
                queryWrapper.lambda().in(UserCouponDO::getHabit, List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_1));
            }
        }

        queryWrapper.orderByAsc("coupon_type");
        queryWrapper.orderByDesc("expiration_time");

        Page<UserCouponDO> page = userCouponMapper.selectPage(
                new Page<>(appCouponListReqVO.getPageNo(), appCouponListReqVO.getPageSize()),
                queryWrapper
        );

        List<UserCouponDO> records = page.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return PageResult.empty();
        }
        PageResult<AppCouponListRespVO> list = PageResult.empty(page.getTotal());
        List<AppCouponListRespVO> result = BeanUtils.toBean(records, AppCouponListRespVO.class);

        extracted(appCouponListReqVO, result);
        list.setList(result);
        return list;
    }


    private void extracted(AppCouponListReqVO appCouponListReqVO, List<AppCouponListRespVO> result) {
        if (appCouponListReqVO.getStoreId() == null) {
            return;
        }

        Date now = new Date();
        Set<Long> couponIds = result.stream().map(AppCouponListRespVO::getCouponId).collect(Collectors.toSet());

        // 批量查询优惠券商品关系
        Map<Long, List<CouponCommodityDO>> couponCommodityMap = batchQueryCouponCommodities(couponIds);

        // 批量查询优惠券门店关系
        Map<Long, CouponStoreDO> couponStoreMap = batchQueryCouponStores(couponIds, appCouponListReqVO.getStoreId());

        // 处理每个优惠券
        for (AppCouponListRespVO coupon : result) {
            // 检查有效期
            if (coupon.getVaildStartTime() != null &&
                    (now.before(coupon.getVaildStartTime()) || now.after(coupon.getExpirationTime()))) {
                coupon.setIsUserForStore(2);
            }

            // 检查是否多门店
            List<CouponCommodityDO> commodities = couponCommodityMap.getOrDefault(coupon.getCouponId(), Collections.emptyList());
            if (commodities.size() == 1) {
                coupon.setIsManyStore(1);
            }

            // 检查门店是否可用
            if (coupon.getIsCommon() == 2 && !couponStoreMap.containsKey(coupon.getCouponId())) {
                coupon.setIsUserForStore(1);
                log.info("优惠券不在当前门店可用 {},{}", coupon.getCouponId(), coupon.getId());
            }
        }

        // 排序
        result.sort(Comparator.comparing(AppCouponListRespVO::getIsUserForStore));

        // 处理已使用标记
        if (UserCouponConstants.IS_USED_2.equals(appCouponListReqVO.getIsUsed())) {
            result.forEach(item -> item.setIsUsed(1));
        }
    }

    private Map<Long, List<CouponCommodityDO>> batchQueryCouponCommodities(Set<Long> couponIds) {
        if (couponIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper<CouponCommodityDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(CouponCommodityDO::getCouponId, couponIds)
                .eq(CouponCommodityDO::getType, 2);

        return couponCommodityMapper.selectList(queryWrapper)
                .stream()
                .collect(Collectors.groupingBy(CouponCommodityDO::getCouponId));
    }

    private Map<Long, CouponStoreDO> batchQueryCouponStores(Set<Long> couponIds, String storeId) {
        if (couponIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper<CouponStoreDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(CouponStoreDO::getCouponId, couponIds)
                .eq(CouponStoreDO::getStoreId, storeId);

        return couponStoreMapper.selectList(queryWrapper)
                .stream()
                .collect(Collectors.toMap(
                        CouponStoreDO::getCouponId,
                        item -> item,
                        (existing, replacement) -> replacement // Key 冲突时，用新的覆盖旧的
                ));
    }

    private void extracted2(AppCouponListReqVO appCouponListReqVO, List<AppCouponListRespVO> result) {
        if (appCouponListReqVO.getStoreId() != null) {
            result.forEach(userCoupon1 -> {
                if (userCoupon1.getVaildStartTime() != null) {
                    // 检查优惠券是否在有效时间内
                    Date now = new Date();
                    if (now.before(userCoupon1.getVaildStartTime()) || now.after(userCoupon1.getExpirationTime())) {
                        userCoupon1.setIsUserForStore(2);
                    }
                }
                QueryWrapper<CouponCommodityDO> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(CouponCommodityDO::getCouponId, userCoupon1.getCouponId()).eq(CouponCommodityDO::getType, 2);
                List<CouponCommodityDO> list1 = couponCommodityMapper.selectList(queryWrapper1);
                if (list1 != null && list1.size() == 1) {
                    userCoupon1.setIsManyStore(1);
                }
            });
            result.stream().filter(userCoupon1 -> userCoupon1.getIsCommon() == 2).forEach(userCoupon1 -> {
                QueryWrapper<CouponStoreDO> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(CouponStoreDO::getCouponId, userCoupon1.getCouponId()).eq(CouponStoreDO::getStoreId, appCouponListReqVO.getStoreId());
                CouponStoreDO couponStore = couponStoreMapper.selectOne(queryWrapper1);
                if (ObjectUtil.isEmpty(couponStore)) {
                    userCoupon1.setIsUserForStore(1);
                    log.info("优惠券不在当前门店可用{}{}", userCoupon1.getCouponId(), userCoupon1.getId());
                }
            });
        }
        result.sort(Comparator.comparing(AppCouponListRespVO::getIsUserForStore));
        if (appCouponListReqVO.getIsUsed().equals(UserCouponConstants.IS_USED_2)) {
            result.forEach(item -> {
                item.setIsUsed(1);
            });
        }
    }

    @Override
    public UserOneCouponRespVO getCouponByUserAndCouponId(UserCouponReqVO userCouponReqVO) {
        Long couponId = userCouponReqVO.getCouponId();
        Long memberId = userCouponReqVO.getMemberId();
        Long userCouponId = userCouponReqVO.getUserCouponId();
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", couponId);
        queryWrapper.eq("user_id", memberId);
        queryWrapper.eq("id", userCouponId);
        UserCouponDO coupon = userCouponMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(coupon)) {
            return new UserOneCouponRespVO();
        }
        CouponShareDO couponShare = couponShareService.getById(couponId);
        UserOneCouponRespVO userCouponRespVO = BeanUtils.toBean(coupon, UserOneCouponRespVO.class);
        CouponShareRespVO couponShareRespVO = BeanUtils.toBean(couponShare, CouponShareRespVO.class);
        userCouponRespVO.setCouponShare(couponShareRespVO);

        // 批量查询优惠券门店关系
        Map<Long, CouponStoreDO> couponStoreMap = batchQueryCouponStores(Set.of(couponId), userCouponReqVO.getStoreId());

        // 检查有效期
        if (coupon.getVaildStartTime() != null &&
                (new Date().before(coupon.getVaildStartTime()) || new Date().after(coupon.getExpirationTime()))) {
            userCouponRespVO.setIsUserForStore(2);
            if(new Date().before(coupon.getVaildStartTime())){
                // 待使用
                userCouponRespVO.setIsUsed(2);
            }
            if(new Date().after(coupon.getExpirationTime())){
                // 过期
                userCouponRespVO.setIsUsed(3);
            }
        }

        // 检查门店是否可用
        if (coupon.getIsCommon() == 2 && !couponStoreMap.containsKey(coupon.getCouponId())) {
            userCouponRespVO.setIsUserForStore(1);
            log.info("优惠券不在当前门店可用 {},{}", coupon.getCouponId(), coupon.getId());
        }
        return userCouponRespVO;
    }

    @Override
    public void delOverdueCoupon() {
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserCouponDO::getCouponStatus, 4).lt(UserCouponDO::getExpirationTime, new Date());
        userCouponMapper.delete(queryWrapper);
    }

    @Override
    public void usedCoupon(UsedCouponReqVO usedCouponReqVO) {
        log.info(">>> 优惠券使用开始{}{}", LocalDateTime.now(), usedCouponReqVO);
        //已使用数量
        Long userCouponId = usedCouponReqVO.getUserCouponId();
        Long memberId = usedCouponReqVO.getMemberId();

        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userCouponId);
        queryWrapper.eq("user_id", memberId);
        UserCouponDO userCoupon = userCouponMapper.selectOne(queryWrapper);

        Long couponId = userCoupon.getCouponId();
        Integer isUsed = usedCouponReqVO.getIsUsed();

        DouyinCouponRedeemRecordDO douyinCouponRedeemRecordDO = douyinCouponRedeemRecordService.getOne(new LambdaQueryWrapper<DouyinCouponRedeemRecordDO>().eq(DouyinCouponRedeemRecordDO::getUserCouponId, userCouponId));

        if (isUsed.equals(0)) {

            //抖音券需要做撤销核销
            if (!ObjectUtil.isEmpty(douyinCouponRedeemRecordDO)) {
                try {
                    this.cancelDouyinCoupon(douyinCouponRedeemRecordDO);
                } catch (Exception e) {
                    log.error("==> 抖音券撤销核销异常 {}", douyinCouponRedeemRecordDO, e);
                }
            }

            if (usedCouponReqVO.getIsRefundAction() != 1 || ObjectUtil.isEmpty(douyinCouponRedeemRecordDO)) {
                UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
                log.info("优惠券使用数量减一");
                goodCouponUpdateWrapper.setSql("used_num = used_num - 1");
                goodCouponUpdateWrapper.eq("id", couponId);
                boolean b = goodCouponService.updateByWrapper(goodCouponUpdateWrapper);
                log.info("优惠券使用数量减一结果：{}", b);
                if (!b) {
                    log.warn("优惠券： {}使用失败", couponId);
                }
                QueryWrapper<UserCouponRecordDO> recordQueryWrapper = new QueryWrapper<>();
                recordQueryWrapper.eq("user_coupon_id", userCouponId);
                recordQueryWrapper.eq("coupon_id", userCoupon.getCouponId());
                userCouponRecordMapper.delete(recordQueryWrapper);
                log.info("优惠券使用数量减一完成");
            }

        } else {
            //抖音券需要做核销
            if (!ObjectUtil.isEmpty(douyinCouponRedeemRecordDO)) {
                this.verifyDouyinCoupon(douyinCouponRedeemRecordDO);
            }

            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            log.info("优惠券使用数量加一");
            goodCouponUpdateWrapper.setSql("used_num = used_num + 1");
            goodCouponUpdateWrapper.eq("id", couponId);
            boolean b = goodCouponService.updateByWrapper(goodCouponUpdateWrapper);
            log.info("优惠券使用数量加一结果：{}", b);
            if (!b) {
                log.warn("优惠券： {}回退失败！", couponId);
            }
            UserCouponRecordDO userCouponRecord = getUserCouponRecordDO(usedCouponReqVO, userCoupon);
            userCouponRecordMapper.insert(userCouponRecord);
            log.info("优惠券使用数量加一完成");
        }
        LambdaUpdateChainWrapper<UserCouponDO> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userCouponMapper);
        lambdaUpdateChainWrapper.eq(UserCouponDO::getId, usedCouponReqVO.getUserCouponId()).eq(UserCouponDO::getUserId, usedCouponReqVO.getMemberId()).set(UserCouponDO::getIsUsed, usedCouponReqVO.getIsUsed()).set(UserCouponDO::getUseTime, new Date()).update();
    }

    /**
     * 撤销抖音券核销
     *
     * @param douyinCouponRedeemRecordDO
     */
    private void cancelDouyinCoupon(DouyinCouponRedeemRecordDO douyinCouponRedeemRecordDO) {
        String verifyResponseStr = douyinCouponRedeemRecordDO.getVerifyResponse();
        Map<String, Object> verifyMap = JSON.parseObject(verifyResponseStr, Map.class);

        CancelVerifyRequest cancelVerifyRequest = new CancelVerifyRequest();
        cancelVerifyRequest.setCertificateId(verifyMap.get("certificate_id").toString());
        cancelVerifyRequest.setVerifyId(verifyMap.get("verify_id").toString());
        cancelVerifyRequest.setShopOrderId(verifyMap.get("order_id").toString());

        DouYinApiResponse<CancelVerifyResponse> cancelVerifyResponse = dyClient.cancelVerify(cancelVerifyRequest);

        if (cancelVerifyResponse.getExtra().getErrorCode() != 0) {
            throw new ServiceException(DOUYIN_JSON_PROCESSING.getCode(), cancelVerifyResponse.getExtra().getDescription());
        }

        douyinCouponRedeemRecordService.update(
                new LambdaUpdateWrapper<DouyinCouponRedeemRecordDO>()
                        .eq(DouyinCouponRedeemRecordDO::getUserCouponId, douyinCouponRedeemRecordDO.getUserCouponId())
                        .set(DouyinCouponRedeemRecordDO::getStatus, CouponStatusEnum.TO_BE_USED.getCode())
        );
    }

    /**
     * 抖音券核销
     *
     * @param douyinCouponRedeemRecordDO
     */
    private void verifyDouyinCoupon(DouyinCouponRedeemRecordDO douyinCouponRedeemRecordDO) {

        //############################# 验券 ##############################
        PrepareResponse data = douyinCouponRedeemRecordService.getPrepareResponse(douyinCouponRedeemRecordDO.getDouyinCouponCode(), douyinCouponRedeemRecordDO.getShortLink(), douyinCouponRedeemRecordDO.getDouyinStoreId().toString());

        Certificate certificate = data.getCertificates().get(0);

        Map<String, Object> preMap = new HashMap<>();
        preMap.put("verify_token", data.getVerifyToken());
        preMap.put("encrypted_code", certificate.getEncryptedCode());

        //############################# 核销 ##############################

        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setEncryptedCodes(Collections.singletonList(certificate.getEncryptedCode()));
        verifyRequest.setVerifyToken(data.getVerifyToken());
        verifyRequest.setPoiId(douyinCouponRedeemRecordDO.getDouyinStoreId().toString());
        DouYinApiResponse<VerifyResponse> verifyResponse = dyClient.verify(verifyRequest);

        if (verifyResponse.getExtra().getErrorCode() != 0) {
            throw new ServiceException(DOUYIN_JSON_PROCESSING.getCode(), verifyResponse.getExtra().getDescription());
        }

        VerifyResponse.VerifyResult verifyResult = verifyResponse.getData().getVerifyResults().get(0);
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("certificate_id", verifyResult.getCertificateId());
        respMap.put("verify_id", verifyResult.getVerifyId());
        respMap.put("order_id", verifyResult.getOrderId());

        douyinCouponRedeemRecordService.update(
                new LambdaUpdateWrapper<DouyinCouponRedeemRecordDO>()
                        .eq(DouyinCouponRedeemRecordDO::getUserCouponId, douyinCouponRedeemRecordDO.getUserCouponId())
                        .set(DouyinCouponRedeemRecordDO::getVerifyResponse, JSON.toJSONString(respMap))
                        .set(DouyinCouponRedeemRecordDO::getPrepareResponse, JSON.toJSONString(preMap))
                        .set(DouyinCouponRedeemRecordDO::getStatus, CouponStatusEnum.USED.getCode())
        );
    }

    @Deprecated
    @Override
    public void updateUserCoupon(UpdateUserCouponReqVO updateUserCouponReqVO) {
//        LambdaUpdateChainWrapper<UserCouponDO> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userCouponMapper);
//        lambdaUpdateChainWrapper
//                .eq(UserCouponDO::getId, userCoupon.getId())
//                .eq(UserCouponDO::getUserId, userCoupon.getMemberId())
//                .set(UserCouponDO::getIsUsed, userCoupon.getIsUsed())
//                .set(UserCouponDO::getUseTime, new Date())
//                .update();

        //goodCoupon优惠卷使用数量修改
        goodCouponService.update(new LambdaUpdateWrapper<GoodCouponDO>().setSql(" used_num = used_num + " + updateUserCouponReqVO.getAddNumber()).eq(GoodCouponDO::getCouponCode, updateUserCouponReqVO.getCouponCode()));
    }

    @Override
    public AppUserCouponRespVO getCouponList(SettlementReqVO settlementReqVO) {
        //Long userId = settlementReqVO.getUserId();
        List<OrderGoods> orderGoods = buildReqPrams(settlementReqVO);
        settlementReqVO.setGoodsList(orderGoods);
        log.info("查询用户优惠券列表开始{}", LocalDateTime.now());
        AppUserCouponRespVO appUserCouponRespVO = new AppUserCouponRespVO();
        Set<AppCouponCalculateRespVO> list = new HashSet<>();
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        if (CollectionUtil.isEmpty(goodsList)) {
            List<UserCouponDO> userCouponList = getNoUseUserCoupon(settlementReqVO);
            if (CollectionUtils.isEmpty(userCouponList)) {
                return appUserCouponRespVO;
            }
            userCouponList.forEach(item -> {
                AppCouponCalculateRespVO appCouponCalculateRespVO = BeanUtils.toBean(item, AppCouponCalculateRespVO.class);
                appCouponCalculateRespVO.setRemark("所选商品均不可叠加优惠券");
                list.add(appCouponCalculateRespVO);
            });
            appUserCouponRespVO.setCanNotUseCoupons(list);
            return appUserCouponRespVO;
        }
        BigDecimal transactionAmount = goodsList.stream().map(OrderGoods::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        settlementReqVO.setTransactionAmount(transactionAmount);
        settlementReqVO.setGoodSize(goodsList.size());
        //0.01元不给显示券
        transactionAmount = settlementReqVO.getTransactionAmount();
        if (transactionAmount.compareTo(new BigDecimal("0.01")) < 0) {
            return appUserCouponRespVO;
        }
        Long userCouponId = settlementReqVO.getUserCouponId();
        // 查询用户未使用优惠券列表
        List<UserCouponDO> userCouponList = getNoUseUserCoupon(settlementReqVO);
        if (CollectionUtils.isEmpty(userCouponList)) {
            return appUserCouponRespVO;
        }
        // 查询优惠券可用商品
        List<Long> couponIds = userCouponList.stream().map(UserCouponDO::getCouponId).toList();
        List<Long> exCouponIds = userCouponList.stream().filter(coupon -> coupon.getExchangeFlag() != null || coupon.getExchangeFlag() == GoodCouponConstants.EXCHANGE_FLAG_1).map(UserCouponDO::getCouponId).toList();

        QueryWrapper<CouponCommodityDO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("coupon_id", couponIds);
        List<CouponCommodityDO> couponCommodityList = couponCommodityMapper.selectList(queryWrapper1);

        if (CollectionUtil.isNotEmpty(exCouponIds)) {
            LambdaQueryWrapper<ExchangeCommodityDO> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.in(ExchangeCommodityDO::getCouponId, exCouponIds);
            List<ExchangeCommodityDO> exchangeCommodityList = exchangeCommodityMapper.selectList(queryWrapper2);
            if (CollectionUtil.isNotEmpty(exchangeCommodityList)) {
                Map<Long, List<Long>> canUseMap = exchangeCommodityList.stream().collect(Collectors.groupingBy(ExchangeCommodityDO::getCouponId, Collectors.mapping(ExchangeCommodityDO::getCommodityId, Collectors.toList())));
                CommodityIdsContext.setCommodityId3(canUseMap);
            }
        }

        List<CouponCommodityDO> couponCommodityList1 = couponCommodityList.stream().filter(couponCommodity -> couponCommodity.getType() == 2).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(couponCommodityList1)) {
            Map<Long, List<Long>> canUseMap = couponCommodityList1.stream().collect(Collectors.groupingBy(CouponCommodityDO::getCouponId, Collectors.mapping(CouponCommodityDO::getCommodityId, Collectors.toList())));
            CommodityIdsContext.setCommodityId1(canUseMap);
        }
        //查询优惠券不可用商品
        List<CouponCommodityDO> couponCommodityList2 = couponCommodityList.stream().filter(couponCommodity -> couponCommodity.getType() == 3).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(couponCommodityList2)) {
            Map<Long, List<Long>> notUseMap = couponCommodityList2.stream().collect(Collectors.groupingBy(CouponCommodityDO::getCouponId, Collectors.mapping(CouponCommodityDO::getCommodityId, Collectors.toList())));
            CommodityIdsContext.setCommodityId2(notUseMap);
        }
        log.info("查询完用户优惠券列表{}", LocalDateTime.now());
        List<AppCouponCalculateRespVO> result = BeanUtils.toBean(userCouponList, AppCouponCalculateRespVO.class);
        if (CollectionUtils.isEmpty(result)) {
            return appUserCouponRespVO;
        }

        try {
            //筛选出所有符合条件的券 (能用的券) 责任链模式
            Set<AppCouponCalculateRespVO> set = new HashSet<>(result);
            appUserCouponRespVO = couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO, set, appUserCouponRespVO);
            Set<AppCouponCalculateRespVO> determineIfItCanBeUsed = appUserCouponRespVO.getCanUseCoupons();
            if (CollectionUtils.isEmpty(determineIfItCanBeUsed)) {
                return appUserCouponRespVO;
            }

            log.info("放进上下文中{}", LocalDateTime.now());
            List<AppCouponCalculateRespVO> couponListWithOrder = getCouponListWithOrder(settlementReqVO, determineIfItCanBeUsed, list, userCouponId);
            appUserCouponRespVO.setResult(couponListWithOrder);
            appUserCouponRespVO.setCanUseCoupons(null);
            return appUserCouponRespVO;
        } finally {
            clearCommodityIds();
        }
    }

    private List<OrderGoods> buildReqPrams(SettlementReqVO settlementReqVO) {
//        CalculateCacheDataDTO orderCache = orderCacheService.getOrderCache(userId);
//        List<CalculateCacheDataDTO.CommodityInfoVO> commodityInfos = orderCache.getCommodityInfos();
        List<CalculateCacheDataCopyDTO.CommodityInfoVO> commodityInfos = settlementReqVO.getCommodityInfos();
        if (CollectionUtil.isEmpty(commodityInfos)) {
            log.warn("没有商品信息{}", settlementReqVO);
            return new ArrayList<>();
        }
        List<CalculateCacheDataCopyDTO.CommodityInfoVO> collect = commodityInfos.stream().filter(item -> {
            List<Integer> stackableActivities = item.getStackableActivities();
            Integer isPurchase = item.getIsPurchase();
            if (Objects.equals(isPurchase, 1) || Objects.equals(item.getIsGift(), 1)) {
                return false;
            }
            Integer discountStackable = item.getDiscountStackable();
            // null 表示未命中营销活动，正常参与优惠券；命中活动后必须显式允许叠加优惠。
            if (discountStackable == null) {
                return true;
            }
            return Integer.valueOf(1).equals(discountStackable)
                    && CollectionUtil.isNotEmpty(stackableActivities)
                    && stackableActivities.contains(1);
        }).collect(Collectors.toList());

        List<OrderGoods> orderGoods = new ArrayList<>();
        for (CalculateCacheDataCopyDTO.CommodityInfoVO commodityInfo : collect) {
            List<OrderGoods> result = convertToOrderGoodsList(commodityInfo);
            orderGoods.addAll(result);
        }
        return orderGoods;
    }


    /**
     * 将 CommodityInfoVO 转换为 OrderGoods 集合
     * 优惠金额如果不能整除，则前 n-1 件商品使用向下取整的金额，最后一件商品补齐余数
     *
     * @param vo 原始商品信息
     * @return OrderGoods 集合
     */
    public static List<OrderGoods> convertToOrderGoodsList(CalculateCacheDataCopyDTO.CommodityInfoVO vo) {
        List<OrderGoods> result = new ArrayList<>();

        if (vo == null || vo.getCopies() == null || vo.getCopies() <= 0) {
            return result;
        }

        // 获取单价（假设 skuPrice 是单价）
        BigDecimal unitPrice = vo.getSkuPrice() != null ? vo.getSkuPrice() : BigDecimal.ZERO;

        // 获取总优惠金额
        BigDecimal totalDiscount = vo.getPromotionDiscountAmount() != null ?
                vo.getPromotionDiscountAmount() : BigDecimal.ZERO;

        Integer copies = vo.getCopies();

        // 计算每件商品的基础优惠金额（向下取整到2位小数）
        BigDecimal baseDiscount = totalDiscount.divide(
                BigDecimal.valueOf(copies), 2, RoundingMode.DOWN);

        // 计算最后一件商品的额外补偿（因为向下取整丢失的精度）
        BigDecimal remainder = totalDiscount.subtract(
                baseDiscount.multiply(BigDecimal.valueOf(copies)));

        // 构建每件商品的 OrderGoods
        for (int i = 0; i < copies; i++) {
            OrderGoods orderGoods = new OrderGoods();

            // 商品ID
            orderGoods.setCommodityId(vo.getCommodityId());

            // 活动ID
            orderGoods.setActivityId(vo.getActivityId());

            // 计算该商品的优惠金额
            BigDecimal currentDiscount;
            if (i == copies - 1) {
                // 最后一件商品加上余数，补齐总优惠
                currentDiscount = baseDiscount.add(remainder);
            } else {
                currentDiscount = baseDiscount;
            }

            // 计算实际支付金额（单价 - 该商品优惠金额）
            BigDecimal transactionAmount = unitPrice.subtract(currentDiscount);

            // 设置交易金额（保留2位小数）
            orderGoods.setTransactionAmount(transactionAmount.setScale(2, RoundingMode.HALF_UP));

            result.add(orderGoods);
        }
        return result;
    }

    /**
     * 批量转换方法
     */
    public static List<OrderGoods> convertBatch(List<CalculateCacheDataCopyDTO.CommodityInfoVO> vos) {
        List<OrderGoods> result = new ArrayList<>();

        if (vos == null || vos.isEmpty()) {
            return result;
        }

        for (CalculateCacheDataCopyDTO.CommodityInfoVO vo : vos) {
            result.addAll(convertToOrderGoodsList(vo));
        }

        return result;
    }

    @Override
    public CouponCalculateRespVO getReduceAmount(GetReduceAmountReqVO getReduceAmountReqVO) {
        CouponCalculateRespVO returnObj = new CouponCalculateRespVO();
        //构建请求
        SettlementReqVO settlementReqVO = this.getSettlementReqVO(getReduceAmountReqVO);

        AppUserCouponRespVO appUserCouponRespVO = this.getCouponList(settlementReqVO);

        if (CollectionUtils.isEmpty(appUserCouponRespVO.getResult())) {
            return null;
        }
        BeanUtils.copyProperties(appUserCouponRespVO.getResult().get(0), returnObj);
        return returnObj;
    }

    private SettlementReqVO getSettlementReqVO(GetReduceAmountReqVO getReduceAmountReqVO) {
        SettlementReqVO settlementReqVO = new SettlementReqVO();
        BeanUtils.copyProperties(getReduceAmountReqVO, settlementReqVO);
        settlementReqVO.setCommodityInfos(getReduceAmountReqVO.getCommodityInfos());
        settlementReqVO.setIsSupportSingle(true);

        return settlementReqVO;
    }

    /**
     * 查询用户未使用优惠券列表
     *
     * @param settlementReqVO settlementReqVO
     * @return UserCouponDO
     */
    private List<UserCouponDO> getNoUseUserCoupon(SettlementReqVO settlementReqVO) {
        //用户未使用的券
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserCouponDO::getUserId, settlementReqVO.getUserId()).eq(UserCouponDO::getIsUsed, 0)
                //.le(UserCouponDO::getVaildStartTime, LocalDateTime.now())
                .ge(UserCouponDO::getExpirationTime, LocalDateTime.now());

        //单个优惠券计算
        if (settlementReqVO.getIsSupportSingle()) {
            queryWrapper.lambda().eq(UserCouponDO::getId, settlementReqVO.getUserCouponId());
        }

//        Integer habit = settlementReqVO.getHabit();
//        if (ObjectUtil.isNotEmpty(habit)) {
//            if (ObjectUtil.equals(habit, UserCouponConstants.HABIT_2)) {
//                queryWrapper.lambda().in(UserCouponDO::getHabit, List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_2));
//            } else {
//                queryWrapper.lambda().in(UserCouponDO::getHabit, List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_1));
//            }
//        }
        return userCouponMapper.selectList(queryWrapper);
    }

    /**
     * 下单时候的优惠金额计算
     * 超过80行了 提出来
     *
     * @param settlementReqVO settlementReqVO
     * @param result          result
     * @param list            list
     * @param userCouponId    userCouponId
     * @return AppCouponListRespVO
     */
    private List<AppCouponCalculateRespVO> getCouponListWithOrder(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> result, Set<AppCouponCalculateRespVO> list, Long userCouponId) {
        //优惠金额计算
        for (AppCouponCalculateRespVO coupon : result) {
            //策略嵌套策略
            //AppCouponCalculateRespVO userCoupon = ;
            list.add(couponTypeContext.calculateDiscount(coupon, settlementReqVO));
        }
        // 简化收集操作
//        List<CompletableFuture<AppCouponCalculateRespVO>> futures = result.stream()
//                .map(coupon -> CompletableFuture.supplyAsync(
//                        () -> {
//                            try {
//                                return couponTypeContext.calculateDiscount(coupon, settlementReqVO);
//                            } catch (Exception e) {
//                                log.error("优惠券[{}]计算异常: {}", coupon.getId(), e.getMessage());
//                                return null;
//                            }
//                        },
//                        promotionThreadPool))
//                .toList();
//        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
//                futures.toArray(CompletableFuture[]::new)
//        );
//
//        // 合并结果并处理异常
//        List<AppCouponCalculateRespVO> calculatedCoupons = allFutures
//                .thenApply(v -> futures.stream()
//                        .map(future -> {
//                            try {
//                                return future.join();
//                            } catch (CompletionException e) {
//                                log.error("获取优惠券结果异常", e.getCause());
//                                return null;
//                            }
//                        })
//                        .filter(Objects::nonNull)
//                        .toList())
//                .join();
//
//        //list.addAll(calculatedCoupons);
//
//        // 等待所有任务完成并收集结果
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        // 将结果添加到list集合
//        futures.forEach(future -> {
//            try {
//                list.add(future.get());
//            } catch (InterruptedException | ExecutionException e) {
//                log.error("优惠券计算异常", e);
//                Thread.currentThread().interrupt();
//            }
//        });
        log.info("优惠金额计算{}", LocalDateTime.now());
        //优惠金额排序
        List<AppCouponCalculateRespVO> sortedList = new ArrayList<>(list); // 转为List保证初始顺序

        if (ObjectUtil.isNotEmpty(userCouponId)) {
            sortedList = sortedList.stream().sorted(Comparator
                    // 优先匹配 userCouponId
                    .comparing((AppCouponCalculateRespVO vo) -> ObjectUtil.notEqual(vo.getId(), userCouponId))
                    // 其次按 reduceAmount 降序
                    .thenComparing(AppCouponCalculateRespVO::getReduceAmount, Comparator.reverseOrder())).peek(item -> {
                item.setUserCouponId(item.getId());
            }).toList();
        } else {
            sortedList = sortedList.stream().sorted(Comparator.comparing(AppCouponCalculateRespVO::getReduceAmount, Comparator.reverseOrder())).peek(item -> {
                item.setUserCouponId(item.getId());
            }).toList();
        }

//        log.info("优惠金额排序{},{},{}", list, LocalDateTime.now(), settlementReqVO.getUserId());
        clearCommodityIds();
        return sortedList;
    }

    /**
     * 清除商品ids上下文
     */
    private void clearCommodityIds() {
        CommodityIdsContext.clearCommodityId1();
        CommodityIdsContext.clearCommodityId2();
        CommodityIdsContext.clearCommodityId3();
        UserCouponCommodityContext.clear();
    }

    private static UserCouponRecordDO getUserCouponRecordDO(UsedCouponReqVO usedCouponReqVO, UserCouponDO userCoupon) {
        UserCouponRecordDO userCouponRecord = new UserCouponRecordDO();
        userCouponRecord.setUserCouponId(usedCouponReqVO.getUserCouponId());
        userCouponRecord.setCouponId(userCoupon.getCouponId());
        userCouponRecord.setTotalAmount(usedCouponReqVO.getPayAmount());
        userCouponRecord.setCouponAmount(usedCouponReqVO.getCouponPrice());
        userCouponRecord.setItemNum(usedCouponReqVO.getCount());
        userCouponRecord.setBusinessId(userCoupon.getBusinessId());
        userCouponRecord.setStoreId(usedCouponReqVO.getStoreId());
        userCouponRecord.setIsCommon(userCoupon.getIsCommon());
        userCouponRecord.setCouponSource(userCoupon.getCouponSource());
        return userCouponRecord;
    }

    @Override
    public Boolean claimOneCoupon(ClaimCouponReqVO claimCoupon) {
        boolean acquire = rateLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }

        Long userId = claimCoupon.getMemberId();
        Long couponId = claimCoupon.getCouponId();
        Long storeId = claimCoupon.getStoreId();
        Integer couponSource = claimCoupon.getCouponSource();
        log.info("claimACoupon:{}{}{}{}", userId, couponId, storeId, couponSource);
        GoodCouponRespVO goodCoupon = goodCouponService.getCouponById(couponId);
        if (ObjectUtil.isEmpty(goodCoupon.getId())) {
            throw exception(ErrorCodeConstants.COUPON_EXPIRED);
        }
        goodCoupon.setStoreId(claimCoupon.getStoreId());

        CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = memberById.getData();
        //领取优惠券的一堆判断 优惠券存在 剩余数量 上下架 过期  会员过期 新老用户 领取数量 --判断
        claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        Integer storeLimitNum = goodCoupon.getStoreLimitNum();
        if (ObjectUtil.notEqual(storeLimitNum, GoodCouponConstants.STORE_LIMIT_0) && ObjectUtil.isNotEmpty(storeId)) {
            CouponCountUtil.claimCoupon(redisTemplate, couponId, storeId, 1, storeLimitNum);
        }
        try {
            //减去优惠券库存
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + 1");
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - 1");
            goodCouponUpdateWrapper.eq("id", couponId);
            //修改优惠券
            boolean b = goodCouponService.updateByWrapper(goodCouponUpdateWrapper);
            if (!b) {
                log.warn("优惠券： {}当前领取人数过多，请稍后重试！", couponId);
                throw new RuntimeException("优惠券： " + goodCoupon.getCouponName() + "当前领取人数过多，请稍后重试！");
            }
            goodCoupon.setUserRestrictions(claimCoupon.getUserRestrictions());
            return insertUserCoupon(userId, goodCoupon, wxMember, couponSource, storeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean claimSeckillCoupon(ClaimCouponReqVO claimCoupon) {
        boolean acquire = seckillLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        //秒杀活动是否可以参与 进没进群
        Boolean b = activityAppService.checkCanJoin(claimCoupon.getActivityId());
        //秒杀优惠券领取前的检查
        preCheckInRedis(claimCoupon.getStoreId(), claimCoupon.getActivityId(), claimCoupon.getSessionId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());

        if(!b){
            throw exception(ErrorCodeConstants.BASE_ACTIVITY_NOT_IN_GROUP);
        }

        try {
            //GoodCouponRespVO couponById = goodCouponService.getCouponById(claimCoupon.getCouponId());
            GoodCouponDO couponByCache = activitySeckillCouponService.getCouponByCache(claimCoupon.getActivityId(), claimCoupon.getCouponId());

            GoodCouponRespVO couponById = BeanUtils.toBean(couponByCache, GoodCouponRespVO.class);
            CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(claimCoupon.getMemberId());
            WxMemberDTO wxMember = memberById.getData();

            // 保留领券时 member的类型
            couponById.setUserRestrictions(claimCoupon.getUserRestrictions());
            couponById.setBusinessId(BusinessContextHolder.getBusinessId());
            insertUserCouponWithSeckill(claimCoupon.getMemberId(), couponById, wxMember, CouponSourceType.SECKILL.getCode(), claimCoupon.getStoreId());

            // 设置领取成功弹幕
            if (wxMember.getMemberMobile() != null) {
                String key = "claim_coupon_queue:" + claimCoupon.getActivityId();
                String tel = wxMember.getMemberMobile();
                String maskedTel = tel.length() > 7 ? tel.substring(7) : tel;
                String msg = maskedTel + "会员 刚刚成功抢购";
                // 将消息推送到弹幕队列
                redisTemplate.opsForList().leftPush(key, msg);
                redisTemplate.opsForList().trim(key, 0, 99);
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }

        } catch (Exception e) {
            //回滚
            rollbackRedis(claimCoupon.getStoreId(), claimCoupon.getActivityId(), claimCoupon.getSessionId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());
            throw exception(CLAIM_FAIL);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean claimPointsCoupon(ClaimPointsCouponReqVO claimCoupon) {
        boolean acquire = collectPointsLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        // 是否已经兑换过此
        Boolean couponExist = activityJDCacheService.isCouponExist(claimCoupon.getActivityId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());
        if (couponExist) {
            throw exception(JD_MEMBER_COUPON_EXIST);
        }

        //秒杀活动是否可以参与 进没进群
        Boolean b = activityAppService.checkCanJoin(claimCoupon.getActivityId());
        if(!b){
            throw exception(ErrorCodeConstants.BASE_ACTIVITY_NOT_IN_GROUP);
        }
        // 兑换物详情
        List<ActivityJDCouponRespSaveVO> couponList = new ArrayList<>();
        List<ActivityJDCouponPackageRespSaveVO> couponPackageList = new ArrayList<>();
        ActivityJDFullRespVO activity = activityJDCacheService.getActivity(claimCoupon.getActivityId(), RedisKeyConstants.JD_ACTIVITY);
        if (ObjectUtil.isEmpty(activity.getIsEnabled()) || ObjectUtil.equal(activity.getIsEnabled(), 0)) {
            throw exception(JD_GROUND);
        }
        Integer goodsType = claimCoupon.getGoodsType();
        if (ObjectUtil.equal(goodsType, 1)) {
            // 会员的点数是否可以兑换
            ActivityJDCouponReqSaveVO activityCoupon = activityJDCacheService.getActivityCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
            if (ObjectUtil.isEmpty(activityCoupon)) {
                throw exception(JD_GROUND);
            }
            // 获取会员的点数
            Integer memberPoints = activityJDCacheService.getMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId());
            if (memberPoints < activityCoupon.getRedeemPoints()) {
                throw exception(JD_POINT_NOT_ENOUGH);
            }
            // 兑换物是否还有库存
            Integer incrClaimedCoupon = activityJDCacheService.getIncrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
            if (incrClaimedCoupon > activityCoupon.getInventory()) {
                // 减库存
                activityJDCacheService.decrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
                throw exception(JD_GOODS_NOT_ENOUGH);
            }

            try {
                // 扣掉用户手中的点数
                activityJDCacheService.addMemberClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());


                Integer distributeMode = activity.getDistributeMode();
                GoodCouponRespVO goodCouponRespVO = activityCoupon.getGoodCouponRespVO();
                CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(claimCoupon.getMemberId());
                WxMemberDTO wxMember = memberById.getData();

                // 插入记录
                ActivityExchangeLogDO activityExchangeLogDO = new ActivityExchangeLogDO();
                activityExchangeLogDO.setMemberId(claimCoupon.getMemberId());
                activityExchangeLogDO.setMemberNickName(wxMember.getMemberNickName());
                activityExchangeLogDO.setMemberMobile(wxMember.getMemberMobile());
                activityExchangeLogDO.setActivityId(claimCoupon.getActivityId());
                activityExchangeLogDO.setAwardType(goodsType);
                activityExchangeLogDO.setAwardName(activityCoupon.getCouponName());
                activityExchangeLogDO.setAwardPic(activityCoupon.getImageUrl());
                activityExchangeLogDO.setForeignId(activityCoupon.getId());
                activityExchangeLogDO.setStoreId(claimCoupon.getStoreId());
                activityExchangeLogDO.setDistributeMode(distributeMode);
                activityExchangeLogDO.setUserRestrictions(claimCoupon.getUserRestrictions());
                exchangeLogService.insert(activityExchangeLogDO);
                if (ObjectUtil.equal(distributeMode, 1)) {
                    // 保留领券时 member的类型
                    goodCouponRespVO.setUserRestrictions(claimCoupon.getUserRestrictions());
                    goodCouponRespVO.setBusinessId(BusinessContextHolder.getBusinessId());
                    insertUserCouponWithSeckill(claimCoupon.getMemberId(), goodCouponRespVO, wxMember, CouponSourceType.COLLECT_POINTS.getCode(), claimCoupon.getStoreId());
                }
                //activityJDCacheService.decrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());


            } catch (Exception e) {
                log.warn("会员领取优惠券失败{}", e.getMessage());
                activityJDCacheService.decrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
                //activityJDCacheService.incrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());
                activityJDCacheService.removeMemberClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());
                throw exception(CLAIM_FAIL);
            }
        } else {
            // 优惠券包
            ActivityJDCouponPackageReqSaveVO activityCoupon = activityJDCacheService.getActivityCouponPackage(claimCoupon.getActivityId(), claimCoupon.getCouponId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
            if (ObjectUtil.isEmpty(activityCoupon)) {
                throw exception(JD_GROUND);
            }
            // 获取会员的点数
            Integer memberPoints = activityJDCacheService.getMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId());
            if (memberPoints < activityCoupon.getRedeemPoints()) {
                throw exception(JD_POINT_NOT_ENOUGH);
            }

            // 兑换物是否还有库存
            Integer incrClaimedCoupon = activityJDCacheService.getIncrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
            if (incrClaimedCoupon > activityCoupon.getInventory()) {
                // 减库存
                activityJDCacheService.decrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
                throw exception(JD_MEMBER_COUPON_EXIST);
            }

            try {
                // 扣掉用户手中的点数
                //activityJDCacheService.decrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());
                activityJDCacheService.addMemberClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());
                Integer distributeMode = activity.getDistributeMode();
                CouponPackageRespVO couponPackageRespVO = activityCoupon.getCouponPackageRespVO();
                CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(claimCoupon.getMemberId());
                WxMemberDTO wxMember = memberById.getData();
                ActivityExchangeLogDO activityExchangeLogDO = ActivityExchangeLogDO.builder()
                        .memberId(claimCoupon.getMemberId())
                        .memberNickName(wxMember.getMemberNickName())
                        .memberMobile(wxMember.getMemberMobile())
                        .activityId(claimCoupon.getActivityId())
                        .awardType(goodsType)
                        .awardName(activityCoupon.getPackageName())
                        .awardPic(activityCoupon.getImageUrl())
                        .foreignId(activityCoupon.getId())
                        .storeId(claimCoupon.getStoreId())
                        .distributeMode(distributeMode)
                        .userRestrictions(claimCoupon.getUserRestrictions())
                        .build();
                exchangeLogService.insert(activityExchangeLogDO);
                if (ObjectUtil.equal(distributeMode, 1)) {
                    insertCouponPackageWithPoints(couponPackageRespVO, wxMember, claimCoupon.getUserRestrictions(), claimCoupon.getStoreId(), CouponSourceType.COLLECT_POINTS.getCode());
                }
            } catch (Exception e) {
                log.warn("会员领取优惠券失败{}", e.getMessage());
                activityJDCacheService.decrClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getCouponId());
                //activityJDCacheService.incrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());
                activityJDCacheService.removeMemberClaimedCoupon(claimCoupon.getActivityId(), claimCoupon.getMemberId(), claimCoupon.getCouponId());
                throw exception(CLAIM_FAIL);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 集点活动领取优惠券包
     *
     * @param wxMember
     * @param couponPackageRespVO
     * @param couponSource
     * @param userRestrictions
     * @param storeId
     * @return
     */
    @Override
    public Boolean insertCouponPackageWithPoints(CouponPackageRespVO couponPackageRespVO, WxMemberDTO wxMember, Integer userRestrictions, Long storeId, Integer couponSource) {
        List<UserCouponDO> userCoupons = new ArrayList<>();
        List<UserCouponPackageDO> userCouponPackages = new ArrayList<>();
        UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();

        if (ObjectUtil.isEmpty(couponPackageRespVO)) {
            throw exception(COUPON_PACKAGE_NOT_EXISTS);
        }

        //判断优惠券是否上架
        if (couponPackageRespVO.getIsGround() != IsGroundConstant.IS_GROUND_1) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }

        List<GoodCouponPackageRespVO> goodCouponPackageRespVOS = couponPackageRespVO.getGoodCouponPackageRespVOS();
        List<GoodCouponRespVO> list = goodCouponPackageRespVOS.stream().map(goodCouponPackageRespVO -> {
            return goodCouponPackageRespVO.getGoodCouponRespVO();
        }).toList();
        Map<Long, Integer> couponNumMap = goodCouponPackageRespVOS.stream().collect(Collectors.toMap(GoodCouponPackageRespVO::getCouponId, GoodCouponPackageRespVO::getNum));
        UserCouponDO userCoupon = new UserCouponDO();

        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String creator = loginUser.getId().toString();

        for (GoodCouponRespVO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            int packNum = couponNumMap.get(goodCouponId);
            for (int i = 0; i < packNum; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(wxMember.getMemberId());
                userCoupon.setCouponId(goodCoupon.getId());
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setUseTime(null);
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                userCoupon.setMemberName(wxMember.getMemberNickName());
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(wxMember.getMemberMobile());
                userCoupon.setCouponSource(couponSource);
                userCoupon.setStoreId(storeId);
                userCoupon.setDeleted(Boolean.FALSE);
                userCoupon.setCreator(wxMember.getMemberId().toString());
                //定时任务 手动setBusinessId
                userCoupon.setBusinessId(BusinessContextHolder.getBusinessId());
                userCoupon.setCreateTime(goodCoupon.getCreateTime());
                userCoupons.add(userCoupon);
            }
        }
        // 优惠券包关系表
        userCouponPackage = new UserCouponPackageDO();
        userCouponPackage.setBusinessId(BusinessContextHolder.getBusinessId());
        userCouponPackage.setUserId(wxMember.getMemberId());
        userCouponPackage.setPackageId(couponPackageRespVO.getId());
        userCouponPackage.setPackageSource(couponSource);
        userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
        userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
        userCouponPackage.setBusinessId(BusinessContextHolder.getBusinessId());
        userCouponPackage.setCreator(creator);
        userCouponPackages.add(userCouponPackage);

        Map<Long, List<UserCouponDO>> collect = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
        Map<Long, List<UserCouponPackageDO>> collect2 = userCouponPackages.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
        for (Long shardingValue = 0L; shardingValue < 10L; shardingValue++) {
            List<UserCouponDO> insertList = collect.get(shardingValue);
            if (CollectionUtil.isNotEmpty(insertList)) {
                Long finalShardingValue = shardingValue;
                promotionThreadPool.execute(() -> {
                    userCouponMasterService.insertBatch(finalShardingValue, insertList);
                });
            }
            List<UserCouponPackageDO> userCouponPackageDOS = collect2.get(shardingValue);
            if (CollectionUtil.isNotEmpty(userCouponPackageDOS)) {
                Long finalShardingValue = shardingValue;
                promotionThreadPool.execute(() -> {
                    couponPackageMasterService.batchInsert(finalShardingValue, userCouponPackageDOS);
                });
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 批量发放优惠券（会员日发放）
     *
     * @param result memberIds
     * @return Boolean
     */
    @Override
    public Boolean sendCoupons(Set<MemberCouponDTO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return false;
        }
        // 提取所有优惠券ID
        Set<Long> allCouponIds = extractAllCouponIds(result);
        if (CollectionUtils.isEmpty(allCouponIds)) {
            throw exception(COUPON_NOT_EXISTS);
        }
        Map<Long, GoodCouponRespVO> couponMap = batchQueryCoupons(allCouponIds);
        List<UserCouponDO> userCoupons = new ArrayList<>(result.size() * allCouponIds.size()); // 预估容量，减少扩容
        for (MemberCouponDTO memberCouponDTO : result) {
            // 会员信息空值校验
            if (ObjectUtil.isEmpty(memberCouponDTO)
                    || ObjectUtil.isEmpty(memberCouponDTO.getMemberId())
                    || CollectionUtils.isEmpty(memberCouponDTO.getCouponIds())) {
                continue;
            }

            for (Long couponId : memberCouponDTO.getCouponIds()) {
                // 优惠券信息校验
                GoodCouponRespVO goodCoupon = couponMap.get(couponId);
                validateCoupon(goodCoupon, couponId);
                // 构建用户券DO
                UserCouponDO userCoupon = buildUserCouponDO(memberCouponDTO, goodCoupon, couponId);
                userCoupons.add(userCoupon);
            }
        }

        if (CollectionUtils.isEmpty(userCoupons)) {
            return false;
        }
        return userCouponMapper.insertBatch(userCoupons, 5000);
    }

    /**
     * 秒杀优惠券领取前的检查
     *
     * @param storeId
     * @param activityId
     * @param sessionId
     * @param memberId
     * @param couponId
     * @return
     */
    private boolean preCheckInRedis(Long storeId, Long activityId, Integer sessionId, Long memberId, Long couponId) {
        // 获取领取上限
        String userReceiveKey = "seckill:" + activityId + ":user_receive:" + sessionId + ":coupon:" + couponId;

        Integer limitByCache = activitySeckillCouponService.getLimitByCache(activityId, couponId);
        if (ObjectUtil.isNotEmpty(limitByCache) && limitByCache != 0) {
            // 检查用户领取次数
            Integer currentReceive = (Integer) redisTemplate.opsForHash().get(userReceiveKey, memberId.toString());
            currentReceive = currentReceive != null ? currentReceive : 0;

            if (currentReceive >= limitByCache) {
                log.warn("用户领取次数已达上限: userId={}, current={}, limit={}", memberId, currentReceive, limitByCache);
                throw exception(SECKILL_LIMIT_MEMBER);
            }
        }


        //获取秒杀优惠券的库存
        String couponStockKey = seckillCouponStockCacheService.buildStockKey(storeId, activityId, couponId, sessionId);
        //Integer currentStock = seckillCouponStockCacheService.getCurrentStock(claimCoupon.getStoreId(), claimCoupon.getActivityId(), claimCoupon.getCouponId(), claimCoupon.getSessionId());
        Long stock = redisTemplate.opsForValue().decrement(couponStockKey);
        // 检查库存
        if (stock == null || stock < 0) {
            // 库存不足，回滚
            if (stock != null && stock < 0) {
                redisTemplate.opsForValue().increment(couponStockKey);
            }
            log.warn("优惠券库存不足");
            throw exception(SECKILL_COUPON_INVENTORY);
        }
        // 增加用户领取次数
        redisTemplate.opsForHash().increment(userReceiveKey, memberId.toString(), 1);


        // 设置过期时间
        redisTemplate.expire(userReceiveKey, Duration.ofHours(24));


        return true;
    }

    @Override
    public void insertCouponWithRegister(MemberCouponDTO memberCouponDTO) {
        LambdaQueryWrapper<GoodCouponDO> lqw = new LambdaQueryWrapper<>();
        List<GoodCouponDO> goodCoupons = goodCouponMapper.selectByIds(memberCouponDTO.getCouponIds());
        List<CouponNumDTO> couponNumDTOList = memberCouponDTO.getCouponNumDTOList();
        List<UserCouponDO> userCoupons = new ArrayList<>(goodCoupons.size());
        UserCouponDO userCoupon = new UserCouponDO();
        Map<Long, GoodCouponDO> goodCouponMap = goodCoupons.stream()
                .collect(Collectors.toMap(
                        GoodCouponDO::getId,
                        Function.identity()
                ));
        Date date = Date.from(
                LocalDate.now()
                        .atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        for (CouponNumDTO couponNumDTO : couponNumDTOList) {
            for (int i = 0; i < couponNumDTO.getNum(); i++) {
                GoodCouponDO goodCoupon = goodCouponMap.get(couponNumDTO.getCouponId());
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setCouponType(Integer.valueOf(goodCoupon.getCouponType()));

                userCoupon.setUserId(memberCouponDTO.getMemberId());
                userCoupon.setCouponId(goodCoupon.getId());
                userCoupon.setCouponCode(goodCoupon.getCouponCode());
                userCoupon.setCouponName(goodCoupon.getCouponName());

                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                userCoupon.setUseTime(null);
                userCoupon.setDistributionMethod(GoodCouponConstants.LONG_DISTRIBUTE_METHOD_1);
                userCoupon.setMemberMobile(memberCouponDTO.getMemberMobile());
                userCoupon.setMemberName(memberCouponDTO.getMemberName());
                userCoupon.setCouponSource(CouponSourceType.AUTOMATIC_DISTRIBUTION.getCode());
                userCoupon.setStoreId(null);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setVaildStartTime(new Date());
                userCoupon.setExpirationTime(date);
                userCoupons.add(userCoupon);
            }
        }
        userCouponMapper.insertBatch(userCoupons, 5000);
    }

    /**
     * 回滚 Redis 秒杀优惠券
     *
     * @param storeId
     * @param activityId
     * @param sessionId
     * @param memberId
     * @param couponId
     */
    private void rollbackRedis(Long storeId, Long activityId, Integer sessionId, Long memberId, Long couponId) {
        try {
            // 回滚库存
            String couponStockKey = seckillCouponStockCacheService.buildStockKey(storeId, activityId, couponId, sessionId);

            redisTemplate.opsForValue().increment(couponStockKey);

            // 回滚用户领取次数
            String userReceiveKey = "seckill:" + activityId + ":user_receive:" + sessionId;
            redisTemplate.opsForHash().increment(userReceiveKey, memberId, -1);

            log.info("Redis 操作已回滚: activityId={}, sessionId={}, userId={}", activityId, sessionId, memberId);
        } catch (Exception e) {
            log.error("Redis 回滚失败", e);
        }
    }


    @Override
    public Long claimTiktokCoupon(ClaimTiktokCouponReqVO claimCoupon) {
        boolean acquire = rateLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }

        UserCouponDO userCoupon = new UserCouponDO();
        Long couponId = claimCoupon.getCouponId();
        Long storeId = claimCoupon.getStoreId();
        int code = CouponSourceType.TIKTOK.getCode();

        Long userId = SecurityFrameworkUtils.getLoginUserId();

        CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = memberById.getData();

        GoodCouponRespVO goodCoupon = goodCouponService.getCouponById(couponId);
        if (ObjectUtil.isEmpty(goodCoupon.getId())) {
            throw exception(ErrorCodeConstants.COUPON_EXPIRED);
        }
        if (goodCoupon.getCouponNum() <= 0) {
            throw exception(ErrorCodeConstants.COUPON_NO_REST);
        }
        if (!Objects.equals(goodCoupon.getLimitNum(), GoodCouponConstants.DAY_LIMIT_0)) {
            QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", wxMember.getMemberId());
            queryWrapper.eq("coupon_id", goodCoupon.getId());
            queryWrapper.eq("coupon_source", code);
            Long receivedNum = userCouponMapper.selectCount(queryWrapper);
            if (receivedNum >= goodCoupon.getLimitNum()) {
                throw exception(ErrorCodeConstants.COUPON_OVER_LIMIT_0);
            }
        }

        try {
            //减去优惠券库存
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + 1");
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - 1");
            goodCouponUpdateWrapper.eq("id", couponId);
            //修改优惠券
            boolean b = goodCouponService.updateByWrapper(goodCouponUpdateWrapper);

            if (!b) {
                log.warn("优惠券： {}当前领取人数过多，请稍后重试！", couponId);
                throw new RuntimeException("优惠券： " + goodCoupon.getCouponName() + "当前领取人数过多，请稍后重试！");
            }

            BeanUtil.copyProperties(goodCoupon, userCoupon);
            userCoupon.setCouponType(Integer.valueOf(goodCoupon.getCouponType()));

            userCoupon.setUserId(userId);
            userCoupon.setCouponId(goodCoupon.getId());
            userCoupon.setCouponCode(goodCoupon.getCouponCode());
            userCoupon.setCouponName(goodCoupon.getCouponName());

            userCoupon.setId(null);
            userCoupon.setIsUsed(0);
            userCoupon.setUseTime(null);
            userCoupon.setDistributionMethod(GoodCouponConstants.LONG_DISTRIBUTE_METHOD_1);
            userCoupon.setMemberMobile(wxMember.getMemberMobile());
            userCoupon.setMemberName(wxMember.getMemberNickName());
            userCoupon.setCouponSource(code);
            userCoupon.setStoreId(storeId);
            userCoupon.setCouponUseTime(goodCoupon.getUseTime());
            userCoupon.setCouponCreateTime(LocalDateTime.now());
            userCoupon.setVaildStartTime(claimCoupon.getVaildStartTime());
            userCoupon.setExpirationTime(claimCoupon.getExpirationTime());
            int insert = userCouponMapper.insert(userCoupon);
        } catch (Exception e) {
            throw exception(CLAIM_COUPON_LIMITER);
        }

        return userCoupon.getId();
    }

    @Override
    public Boolean claimCouponWithProduct(ClaimCouponReqVO claimCoupon) {
        boolean acquire = rateLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        Long userId = claimCoupon.getMemberId();
        boolean b = rateLimitService.claimCouponWithProductAllowRequest(userId);
        if (!b) {
            throw exception(CLAIM_NOT_MORE);
        }
        Long productId = claimCoupon.getProductId();
        //查询商品对应的优惠券
        CommonResult<PointsProductDTO> product = pointsProductApi.getById(productId);
        PointsProductDTO pointsProduct = product.getData();
        CommonResult<WxMemberDTO> memberById = wxMemberApi.getMemberById(userId);
        WxMemberDTO wxMember = memberById.getData();

        if (ObjectUtil.isEmpty(wxMember)) {
            throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER);
        }
        String couponCode = pointsProduct.getCouponCode();
        GoodCouponRespVO goodCoupon = goodCouponService.getCouponByCode(couponCode);
        if (goodCoupon == null) {
            throw exception(ErrorCodeConstants.COUPON_NO_EXISTS);
        }
        String memberMobile = wxMember.getMemberMobile();
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(ErrorCodeConstants.COUPON_NO_MOBILE);
        }
        Long productPrice = pointsProduct.getProductPrice();
        Integer memberIntegral = wxMember.getMemberIntegral();

        if (memberIntegral < productPrice) {
            throw exception(ErrorCodeConstants.COUPON_NO_POINT);
        }
        //锁优惠券
//        String transLock = "LOCK_PRODUCT#" + productId;
//        RLock fairMethodLock = redissonClient.getLock(transLock);
        try {
            //减去用户积分
            wxMemberApi.updateIntegral(wxMember.getMemberId(), wxMember.getShardingValue(), productPrice);

            //插入积分消费记录
            PointsLogDTO pointsLog = new PointsLogDTO();
            pointsLog.setShardingValue(wxMember.getShardingValue());
            pointsLog.setMemberId(userId);
            pointsLog.setPointsLogStatus(1);
            pointsLog.setProductId(productId);
            pointsLog.setProductName(pointsProduct.getProductName());
            pointsLog.setMemberName(wxMember.getMemberName());
            pointsLog.setPointsChange(-productPrice);
            pointsLog.setPointsProduct(pointsProduct);
            pointsLog.setMemberNickName(wxMember.getMemberNickName());
            pointsLog.setProductType(1);
            pointsLog.setLogCode(generateNumber());
            pointsLog.setIsPointsProduct(1);
            pointsLog.setPointsType(2);
            pointsLog.setProductPrice(productPrice);
            pointLogApi.save(pointsLog);
            //减去积分商品库存
            pointsProductApi.updateRest(productId);

            return insertUserCoupon(userId, goodCoupon, wxMember, UserCouponConstants.COUPON_SOURCE_7, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Integer makeShardingValueForWxById(Long memberId) {
        return Integer.parseInt(String.valueOf(memberId % 10));
    }

    private boolean insertUserCoupon(Long userId, GoodCouponRespVO goodCoupon, WxMemberDTO wxMember, Integer couponSource, Long storeId) {
        return userCouponMapper.insert(buildUserCouponWithSeckill(userId, goodCoupon, wxMember, couponSource, storeId)) > 0;
    }

    @Override
    public Boolean insertUserCouponWithSeckill(Long userId, GoodCouponRespVO goodCoupon, WxMemberDTO wxMember, Integer couponSource, Long storeId) {
        return userCouponMapper.insert(buildUserCouponWithSeckill(userId, goodCoupon, wxMember, couponSource, storeId)) > 0;
    }

    @Override
    public Boolean insertUserCouponWithSeckillBatch(Long userId, List<GoodCouponRespVO> goodCoupons, WxMemberDTO wxMember, Integer couponSource, Long storeId) {
        if (CollectionUtil.isEmpty(goodCoupons)) {
            return true;
        }
        List<UserCouponDO> userCoupons = new ArrayList<>(goodCoupons.size());
        for (GoodCouponRespVO goodCoupon : goodCoupons) {
            userCoupons.add(buildUserCouponWithSeckill(userId, goodCoupon, wxMember, couponSource, storeId));
        }
        return insertBatch(userCoupons);
    }

    @Override
    public Integer insertMemberCardBenefitCouponsBatch(List<UserCouponDO> userCoupons) {
        if (CollectionUtil.isEmpty(userCoupons)) {
            return 0    ;
        }
       // return userCouponMapper.insertBatch(userCoupons,5000);
        return userCouponMapper.insertBatchSomeColumn(userCoupons,500);

    }

    @Override
    @DS(DsNameConstants.MASTER)
    @Transactional(rollbackFor = Exception.class)
    public Integer insertMemberCardBenefitCouponsShardChunk(Integer shardingValue, List<UserCouponDO> userCoupons) {
        if (shardingValue == null || CollectionUtil.isEmpty(userCoupons)) {
            return 0;
        }
        return userCouponMapper.writtenInsertion(Long.valueOf(shardingValue), userCoupons);
    }

    private UserCouponDO buildUserCouponWithSeckill(Long userId, GoodCouponRespVO goodCoupon, WxMemberDTO wxMember,
                                                    Integer couponSource, Long storeId) {
        UserCouponDO userCoupon = new UserCouponDO();
        BeanUtil.copyProperties(goodCoupon, userCoupon);
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(goodCoupon.getId());
        userCoupon.setCouponType(Integer.valueOf(goodCoupon.getCouponType()));
        userCoupon.setId(null);
        userCoupon.setIsUsed(0);
        userCoupon.setUseTime(null);
        CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
        userCoupon.setDistributionMethod(0L);
        userCoupon.setMemberMobile(wxMember.getMemberMobile());
        userCoupon.setMemberName(wxMember.getMemberNickName());
        userCoupon.setCouponSource(couponSource);
        userCoupon.setStoreId(storeId);
        userCoupon.setDeleted(false);
        userCoupon.setCouponUseTime(goodCoupon.getUseTime());
        userCoupon.setCreateTime(goodCoupon.getCreateTime());
        userCoupon.setCouponCreateTime(LocalDateTime.now());
        userCoupon.setBusinessId(goodCoupon.getBusinessId());
        return userCoupon;
    }

    /**
     * 根据时效判断开始和过期时间 版本2
     *
     * @param goodCoupon goodCoupon
     * @param userCoupon userCoupon
     */
    private void parseCouponTime(GoodCouponRespVO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    /**
     * 生成领取编号
     *
     * @return String
     */
    public static String generateNumber() {
        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();
        // 使用当前时间的毫秒数作为编号
        return String.valueOf(currentTimeMillis);
    }

    @Override
    public Boolean insertBatch(List<UserCouponDO> userCoupons) {
        return userCouponMapper.insertBatch(userCoupons);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public List<UserCouponVO> selectCouponData(Long memberId) {
        List<UserCouponVO> userCouponDOS1 = new ArrayList<>();
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponDO::getUserId, memberId);
        List<UserCouponDO> userCouponDOS = userCouponMapper.selectList(wrapper);
        if (ObjectUtil.isNotEmpty(userCouponDOS)) {
            userCouponDOS1 = BeanCopyUtils.copyBeanList(userCouponDOS, UserCouponVO.class);
        }


        return userCouponDOS1;
    }

    /**
     * 添加优惠卷
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    public void insertCouponByPoints(UserCouponVO userCouponDO) {
        UserCouponDO userCoupon = BeanCopyUtils.copyBean(userCouponDO, UserCouponDO.class);
        userCouponMapper.insert(userCoupon);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public PageResult<UserCouponPageRespVo> userCouponList(UserCouponPageReqVo couponPageReqVo) {
        Integer pageNo = couponPageReqVo.getPageNum();
        if(pageNo >= 1000){
            throw exception(COUPON_LIST_LIMIT);
        }
        Date date = new Date();
        LambdaQueryWrapper<UserCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserCouponDO::getCouponId, couponPageReqVo.getCouponId());
        if (!StringUtils.isEmpty(couponPageReqVo.getMemberName())) {
            // 原来是name or mobile 现在只要mobile
            lambdaQueryWrapper.eq(UserCouponDO::getMemberMobile, couponPageReqVo.getMemberName());
        }

        if (ObjectUtil.isNotEmpty(couponPageReqVo.getOrgId())) {
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(couponPageReqVo.getOrgId());
            List<Long> data = listCommonResult.getData();
            if (CollectionUtil.isNotEmpty(data)) {
                lambdaQueryWrapper.in(UserCouponDO::getStoreId, data);
            }
        } else {
            lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getStoreId()), UserCouponDO::getStoreId, couponPageReqVo.getStoreId());
        }

        boolean useTimeExist = ObjectUtil.isNotEmpty(couponPageReqVo.getUseStartTime()) && ObjectUtil.isNotEmpty(couponPageReqVo.getUseEndTime());
        if (useTimeExist) {
            lambdaQueryWrapper.between(UserCouponDO::getUseTime, couponPageReqVo.getUseStartTime(), couponPageReqVo.getUseEndTime());
        }
        boolean createTimeExist = ObjectUtil.isNotEmpty(couponPageReqVo.getCouponCreateStartTime()) && ObjectUtil.isNotEmpty(couponPageReqVo.getCouponCreateEndTime());
        if (createTimeExist) {
            lambdaQueryWrapper.between(UserCouponDO::getCouponCreateTime, couponPageReqVo.getCouponCreateStartTime(), couponPageReqVo.getCouponCreateEndTime());
        }

        if (!StringUtils.isEmpty(couponPageReqVo.getCouponStatus())) {
            if (couponPageReqVo.getCouponStatus().equals(2)) {
                if(useTimeExist){
                    throw exception(STATUS_USE_TIME_OPTION_CONFLICT);
                }
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.lt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(0)) {
                if(useTimeExist){
                    throw exception(STATUS_USE_TIME_OPTION_CONFLICT);
                }
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.gt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(1)) {
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 1);
            }

        }
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getUserRestrictions()), UserCouponDO::getUserRestrictions, couponPageReqVo.getUserRestrictions());
        if (!StringUtils.isEmpty(couponPageReqVo.getCouponSource())) {
            lambdaQueryWrapper.eq(UserCouponDO::getCouponSource, couponPageReqVo.getCouponSource());
        }

        lambdaQueryWrapper.orderByDesc(UserCouponDO::getCouponCreateTime);
        Page<UserCouponDO> userCouponDOPage = userCouponMapper.selectPage(new Page<>(couponPageReqVo.getPageNum(), couponPageReqVo.getPageSize()), lambdaQueryWrapper);
        List<UserCouponDO> records = userCouponDOPage.getRecords();
        PageResult<UserCouponPageRespVo> pageResult = new PageResult<>();
        if (records.size() > 0) {
            List<UserCouponPageRespVo> userCouponPageRespVos = new ArrayList<>();
//            List<UserCouponPageRespVo> userCouponPageRespVos = BeanCopyUtils.copyBeanList(records, UserCouponPageRespVo.class);
            List<Long> userCouponIds = records.stream().map(UserCouponDO::getId).toList();
            List<Long> storeIds = records.stream().map(UserCouponDO::getStoreId).toList();
            Map<Long, String> storeMap = new HashMap<>();
            if (CollectionUtil.isNotEmpty(storeIds)) {
                CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
                List<StoreInfoDTO> stores = storesByStoreIds.getData();
                storeMap = stores.stream()
                        .collect(Collectors.toMap(
                                StoreInfoDTO::getStoreId,
                                StoreInfoDTO::getStoreName,
                                (existing, replacement) -> replacement // 覆盖旧值
                        ));
            }
            LambdaQueryWrapper<UserCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getTotalAmount, UserCouponRecordDO::getCreateTime);
            wrapper.eq(UserCouponRecordDO::getCouponId, couponPageReqVo.getCouponId());
            wrapper.in(UserCouponRecordDO::getUserCouponId, userCouponIds);
            List<UserCouponRecordDO> userCouponRecordDOS = userCouponRecordMapper.selectList(wrapper);
            Map<Long, BigDecimal> amountMap = userCouponRecordDOS.stream()
                    .collect(Collectors.toMap(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getTotalAmount));
            Map<Long, LocalDateTime> useTimeMap = userCouponRecordDOS.stream()
                    .collect(Collectors.toMap(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getCreateTime));
            for (UserCouponDO record : records) {
                UserCouponPageRespVo respVo = new UserCouponPageRespVo();
                BeanUtils.copyProperties(record, respVo);
                respVo.setPayAmount(amountMap.get(record.getId()));
//                ZoneId zoneId = ZoneId.systemDefault(); // 获取系统默认时区
//                if (ObjectUtil.isNotEmpty(record.getVaildStartTime())) {
//                    respVo.setCouponCreateTime(record.getVaildStartTime());
//                }
                if (ObjectUtil.isNotEmpty(record.getStoreId())) {
                    respVo.setStoreName(storeMap.get(record.getStoreId()));
                }
                respVo.setUserRestrictions(record.getUserRestrictions());

                if (record.getIsUsed().equals(0)) {
                    if (ObjectUtil.isNotEmpty(record.getIsUsed())) {
                        if (date.getTime() > record.getExpirationTime().getTime()) {
                            respVo.setCouponStatus(2);

                        } else if (date.getTime() < record.getExpirationTime().getTime()) {
                            respVo.setCouponStatus(0);
                        }
                    }

                } else if (record.getIsUsed().equals(1)) {
                    if (ObjectUtil.isNotEmpty(record.getIsUsed())) {
                        respVo.setCouponStatus(1);
                        respVo.setUseTime(useTimeMap.get(record.getId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }

                }
                userCouponPageRespVos.add(respVo);
            }
//            List<UserCouponPageRespVo> collect = userCouponPageRespVos.stream()
//                    .sorted(Comparator.comparing(UserCouponPageRespVo::getCouponCreateTime).reversed())
//                    .collect(Collectors.toList());
            pageResult.setList(userCouponPageRespVos);
            pageResult.setTotal(userCouponDOPage.getTotal());
        } else {
            pageResult.setList(new ArrayList<>());
            pageResult.setTotal(0L);
        }
        return pageResult;
    }

    @Override
    public void exportUserCouponList(UserCouponPageReqVo couponPageReqVo) {
        Page<UserCouponDO> page = new Page<>(1, 3000);
        LambdaQueryWrapper<UserCouponDO> countWrapper = buildUserCouponQueryWrapper(couponPageReqVo, new Date());
        Long total = userCouponMapper.selectCount(countWrapper);
        Long userCouponExportLimit = promotionExportProperties.getUserCouponExportLimit();
        if (total != null && userCouponExportLimit != null && total > userCouponExportLimit) {
            throw new ServiceException(COUPON_EXPORT_LIMIT);
        }
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(couponPageReqVo.getCouponId());
        String fileName = "优惠券领取记录--" + goodCouponDO.getId();
        excelActionService.exportAsyncExcel(UserCouponListExcelVO.class,page, param -> this.queryUserCouponData(page,couponPageReqVo), fileName);
    }

    @DS(DsNameConstants.SHARDING)
    private List<UserCouponListExcelVO> queryUserCouponData(Page<UserCouponDO> page,UserCouponPageReqVo couponPageReqVo) {
        Date date = new Date();
        LambdaQueryWrapper<UserCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserCouponDO::getCouponId, couponPageReqVo.getCouponId());
        if (!StringUtils.isEmpty(couponPageReqVo.getMemberName())) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(UserCouponDO::getMemberName, couponPageReqVo.getMemberName()).or().eq(UserCouponDO::getMemberMobile, couponPageReqVo.getMemberName()));
//            lambdaQueryWrapper.like(UserCouponDO::getMemberName, couponPageReqVo.getMemberName()).or().like(UserCouponDO::getMemberMobile, couponPageReqVo.getMemberName());
        }

        if (ObjectUtil.isNotEmpty(couponPageReqVo.getOrgId())) {
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(couponPageReqVo.getOrgId());
            List<Long> data = listCommonResult.getData();
            if (CollectionUtil.isNotEmpty(data)) {
                lambdaQueryWrapper.in(UserCouponDO::getStoreId, data);
            }
        } else {
            lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getStoreId()), UserCouponDO::getStoreId, couponPageReqVo.getStoreId());
        }

        boolean useTimeExist = ObjectUtil.isNotEmpty(couponPageReqVo.getUseStartTime()) && ObjectUtil.isNotEmpty(couponPageReqVo.getUseEndTime());
        if (useTimeExist) {
            lambdaQueryWrapper.between(UserCouponDO::getUseTime, couponPageReqVo.getUseStartTime(), couponPageReqVo.getUseEndTime());
        }

        boolean createTimeExist = ObjectUtil.isNotEmpty(couponPageReqVo.getCouponCreateStartTime()) && ObjectUtil.isNotEmpty(couponPageReqVo.getCouponCreateEndTime());
        if (createTimeExist) {
            lambdaQueryWrapper.between(UserCouponDO::getCouponCreateTime, couponPageReqVo.getCouponCreateStartTime(), couponPageReqVo.getCouponCreateEndTime());
        }

        if (!StringUtils.isEmpty(couponPageReqVo.getCouponStatus())) {
            if (couponPageReqVo.getCouponStatus().equals(2)) {
                if(useTimeExist){
                    throw exception(STATUS_USE_TIME_OPTION_CONFLICT);
                }
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.lt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(0)) {
                if(useTimeExist){
                    throw exception(STATUS_USE_TIME_OPTION_CONFLICT);
                }
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.gt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(1)) {
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 1);
            }
        }
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getUserRestrictions()), UserCouponDO::getUserRestrictions, couponPageReqVo.getUserRestrictions());
        if (!StringUtils.isEmpty(couponPageReqVo.getCouponSource())) {
            lambdaQueryWrapper.eq(UserCouponDO::getCouponSource, couponPageReqVo.getCouponSource());
        }

        lambdaQueryWrapper.orderByDesc(UserCouponDO::getCouponCreateTime);

        //List<UserCouponDO> list = userCouponMapper.selectPage(couponPageReqVo,lambdaQueryWrapper);
        Page<UserCouponDO> userCouponDOPageResult = userCouponMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), lambdaQueryWrapper);
        List<UserCouponDO> list = userCouponDOPageResult.getRecords();
        LambdaQueryWrapper<UserCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getTotalAmount, UserCouponRecordDO::getCreateTime);
        wrapper.eq(UserCouponRecordDO::getCouponId, couponPageReqVo.getCouponId());
        //wrapper.in(UserCouponRecordDO::getUserCouponId, userCouponIds);
        List<UserCouponRecordDO> userCouponRecordDOS = userCouponRecordMapper.selectList(wrapper);
        Map<Long, BigDecimal> amountMap = userCouponRecordDOS.stream()
                .collect(Collectors.toMap(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getTotalAmount));
        Map<Long, LocalDateTime> useTimeMap = userCouponRecordDOS.stream()
                .collect(Collectors.toMap(UserCouponRecordDO::getUserCouponId, UserCouponRecordDO::getCreateTime));

        List<Long> storeIds = list.stream().map(UserCouponDO::getStoreId).toList();
        Map<Long, String> storeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
            List<StoreInfoDTO> stores = storesByStoreIds.getData();
            storeMap = stores.stream()
                    .collect(Collectors.toMap(
                            StoreInfoDTO::getStoreId,
                            StoreInfoDTO::getStoreName,
                            (existing, replacement) -> replacement // 覆盖旧值
                    ));
        }

        List<UserCouponListExcelVO> userCouponListExcelVOS = new ArrayList<>();
        UserCouponListExcelVO userCouponListExcelVO = new UserCouponListExcelVO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (UserCouponDO item : list) {
            userCouponListExcelVO = new UserCouponListExcelVO();
            userCouponListExcelVO.setMemberName(item.getMemberName());
            userCouponListExcelVO.setMemberMobile(item.getMemberMobile());
            userCouponListExcelVO.setCouponCreateTime(item.getCouponCreateTime());
            userCouponListExcelVO.setCouponSource(item.getCouponSource());
            userCouponListExcelVO.setUserRestrictions(item.getUserRestrictions());

            //有效期
            userCouponListExcelVO.setValidityPeriod(
                    sdf.format(item.getVaildStartTime()) + " - " + sdf.format(item.getExpirationTime())
            );

            // 优惠券领取门店名称
            if (ObjectUtil.isNotEmpty(item.getStoreId())) {
                userCouponListExcelVO.setStoreName(storeMap.get(item.getStoreId()));
            }

            // 优惠券使用状态
            if (item.getIsUsed().equals(0)) {
                if (ObjectUtil.isNotEmpty(item.getIsUsed())) {
                    if (date.getTime() > item.getExpirationTime().getTime()) {
                        userCouponListExcelVO.setCouponStatus(2);
                    } else if (date.getTime() < item.getExpirationTime().getTime()) {
                        userCouponListExcelVO.setCouponStatus(0);
                    }
                }

            } else if (item.getIsUsed().equals(1)) {
                if (ObjectUtil.isNotEmpty(item.getIsUsed())) {
                    userCouponListExcelVO.setCouponStatus(1);
                    userCouponListExcelVO.setCouponUseTime(useTimeMap.get(item.getId()));
                    userCouponListExcelVO.setPayAmount(amountMap.get(item.getId()));
                }
            }
            userCouponListExcelVOS.add(userCouponListExcelVO);
        }
        return userCouponListExcelVOS;
    }

    private LambdaQueryWrapper<UserCouponDO> buildUserCouponQueryWrapper(UserCouponPageReqVo couponPageReqVo, Date date) {
        LambdaQueryWrapper<UserCouponDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserCouponDO::getCouponId, couponPageReqVo.getCouponId());
        if (!StringUtils.isEmpty(couponPageReqVo.getMemberName())) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(UserCouponDO::getMemberName, couponPageReqVo.getMemberName()).or().eq(UserCouponDO::getMemberMobile, couponPageReqVo.getMemberName()));
//            lambdaQueryWrapper.like(UserCouponDO::getMemberName, couponPageReqVo.getMemberName()).or().like(UserCouponDO::getMemberMobile, couponPageReqVo.getMemberName());
        }

        if (ObjectUtil.isNotEmpty(couponPageReqVo.getOrgId())) {
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(couponPageReqVo.getOrgId());
            List<Long> data = listCommonResult.getData();
            if (CollectionUtil.isNotEmpty(data)) {
                lambdaQueryWrapper.in(UserCouponDO::getStoreId, data);
            }
        } else {
            lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getStoreId()), UserCouponDO::getStoreId, couponPageReqVo.getStoreId());
        }

        if (!StringUtils.isEmpty(couponPageReqVo.getCouponStatus())) {

            if (couponPageReqVo.getCouponStatus().equals(2)) {
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.lt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(0)) {
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 0);
                lambdaQueryWrapper.gt(UserCouponDO::getExpirationTime, date);
            } else if (couponPageReqVo.getCouponStatus().equals(1)) {
                lambdaQueryWrapper.eq(UserCouponDO::getIsUsed, 1);
            }

        }
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(couponPageReqVo.getUserRestrictions()), UserCouponDO::getUserRestrictions, couponPageReqVo.getUserRestrictions());
        if (!StringUtils.isEmpty(couponPageReqVo.getCouponSource())) {
            lambdaQueryWrapper.eq(UserCouponDO::getCouponSource, couponPageReqVo.getCouponSource());
        }
        return lambdaQueryWrapper;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public UserCouponCountRespVo getCount(Long couponId) {
        UserCouponCountRespVo userCouponCountRespVo = new UserCouponCountRespVo();
        Integer a = 0;
        Integer b = 0;
        Integer c = 0;
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<UserCouponDO>();
        wrapper.eq(UserCouponDO::getCouponId, couponId);
        List<UserCouponDO> userCouponDOS = userCouponMapper.selectList(wrapper);
        for (UserCouponDO userCouponDO : userCouponDOS) {
            a = a + userCouponDO.getReceivedNum();
            b = b + userCouponDO.getUsedNum();
        }
        c = a - b;
        userCouponCountRespVo.setReceivedNum(a);
        userCouponCountRespVo.setUsedNum(b);
        userCouponCountRespVo.setNotUsedNum(c);
        return userCouponCountRespVo;
    }

    @Override
    public ItemDto couponIsUsed(Long couponId, Long storeId) {
        GoodCouponDO goodCoupon = goodCouponService.getById(couponId);
        ItemDto itemDto = new ItemDto();
        if (goodCoupon != null && goodCoupon.getSingleIds() != null) {
            CommonResult<ItemDto> commodity = commodityApi.getCouponIsUsed(Long.valueOf(goodCoupon.getSingleIds()), storeId);
            log.info(">>> 远程调用commodity模块【/admin-commodity/commodity-store-spu/v3/couponIsUsed】响应 res={}", commodity);
            itemDto = commodity.getData();
        } else {
            throw exception(COUPON_CAN_NOT_USE);
        }
        return itemDto;
    }

    @Override
    public Boolean sendCoupon(Set<MemberCouponDTO> result, Long couponId, Integer sendNum) {
        //优惠券信息
        GoodCouponDO goodCouponDO = goodCouponService.getById(couponId);
        GoodCouponRespVO goodCoupon = BeanUtil.toBean(goodCouponDO, GoodCouponRespVO.class);
        if (ObjectUtil.isEmpty(goodCoupon)) {
            throw exception(COUPON_NOT_EXISTS);
        }
        //判断优惠券是否上架
        if (goodCoupon.getIsGround() != 1) {
            throw exception(COUPON_NOT_ON_SHELF);
        }
        //使用时间是否过期
        Date couponEndTime = goodCoupon.getCouponEndTime();
        if (goodCoupon.getUseType() == 0 && DateUtil.compare(new Date(), couponEndTime) > 0) {
            throw exception(COUPON_EXPIRED);
        }

        //Map<String, MemberCouponDTO> memberMap = result.stream().collect(Collectors.toMap(MemberCouponDTO::getMemberMobile, p -> p));
        Integer couponNum = goodCoupon.getCouponNum();
        int changedNum = sendNum * result.size();
        if (couponNum < changedNum) {
            throw exception(COUPON_NO_REST);
        }
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String creator = loginUser.getId().toString();
        UserCouponDO userCoupon = new UserCouponDO();
        List<UserCouponDO> userCoupons = new ArrayList<>();
        for (MemberCouponDTO item : result) {
            for (int i = 0; i < sendNum; i++) {
                userCoupon = new UserCouponDO();
                BeanUtil.copyProperties(goodCoupon, userCoupon);
                userCoupon.setUserId(item.getMemberId());
                userCoupon.setCouponId(couponId);
                userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                userCoupon.setUseTime(null);
                userCoupon.setCouponCreateTime(LocalDateTime.now());
                userCoupon.setId(null);
                userCoupon.setIsUsed(0);
                userCoupon.setMemberName(item.getMemberName());
                userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                userCoupon.setDistributionMethod(0L);
                userCoupon.setMemberMobile(item.getMemberMobile());
                userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
                userCoupon.setDeleted(Boolean.FALSE);
                userCoupon.setCreator(creator);
                userCoupons.add(userCoupon);
            }
        }
        //更新库存
        UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
        goodCouponUpdateWrapper.setSql("received_num = received_num + " + changedNum);
        goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - " + changedNum);
        goodCouponUpdateWrapper.eq("id", couponId);
        boolean update = goodCouponService.update(goodCouponUpdateWrapper);
        if (!update) {
            log.warn("优惠券： {}库存数量已被他人修改，请重试！", couponId);
            throw new RuntimeException("优惠券： " + userCoupon.getCouponName() + "当前领取人数过多，请稍后重试！");
        }
        Map<Long, List<UserCouponDO>> ageGroupMap = userCoupons.stream()
                .collect(Collectors.groupingBy(UserCouponDO::getUserId));
        //goodCouponService.insertBatchByThread(ageGroupMap);
        Map<Long, List<UserCouponDO>> collect = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));

        for (Map.Entry<Long, List<UserCouponDO>> entry : collect.entrySet()) {
            Long shardingValue = entry.getKey();
            List<UserCouponDO> insertList = entry.getValue();
            promotionThreadPool.execute(() -> {
                userCouponMasterService.insertBatch(shardingValue, insertList);
            });
        }
        return update;
    }

    @Override
    public Boolean sendPackage(Set<MemberCouponDTO> memberList, Long couponId, Integer num) {
        CouponPackageDO couponPackageRespVO = couponPackageMapper.selectById(couponId);

        List<UserCouponDO> userCoupons = new ArrayList<>();
        List<UserCouponPackageDO> userCouponPackages = new ArrayList<>();

        UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();


        if (ObjectUtil.isEmpty(couponPackageRespVO)) {
            throw exception(COUPON_PACKAGE_NOT_EXISTS);
        }
        CouponPackageDO couponPackage = new CouponPackageDO();
        couponPackage.setId(couponPackageRespVO.getId());
        couponPackage.setIsShare(couponPackageRespVO.getIsShare());
        couponPackage.setIsGround(couponPackageRespVO.getIsGround());
        couponPackage.setPackageNum(couponPackageRespVO.getPackageNum());


        //判断优惠券是否上架
        if (couponPackage.getIsGround() != IsGroundConstant.IS_GROUND_1) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }

        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageService.selectListByPackageId(couponId);


        List<Long> couponIds = goodCouponPackages.stream().map(goodCouponPackage -> goodCouponPackage.getCouponId()).toList();
        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);
        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));

        //人数
        int size = memberList.size();

        Integer packageNum = couponPackage.getPackageNum();
        if (packageNum < size * num) {
            throw exception(COUPON_PACKAGE_REST_ERROR);
        }
        UserCouponDO userCoupon = new UserCouponDO();
        //UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();
        Map<Long, Integer> goodCouponNumMap = new HashMap<>();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String creator = loginUser.getId().toString();
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            //包中优惠券数量
            int packNum = couponNumMap.get(goodCoupon.getId());
            //优惠券数量
            int couponNum = goodCoupon.getCouponNum();
            // 总发放数量
            int totalSendNum = size * packNum * num;
            if (totalSendNum > couponNum) {
                throw exception(COUPON_NO_REST, goodCoupon.getCouponName());
            }
            goodCouponNumMap.put(goodCouponId, totalSendNum);
            for (MemberCouponDTO wxMember : memberList) {
                for (int i = 0; i < num * packNum; i++) {
                    userCoupon = new UserCouponDO();
                    BeanUtil.copyProperties(goodCoupon, userCoupon);
                    userCoupon.setUserId(wxMember.getMemberId());
                    userCoupon.setCouponId(goodCoupon.getId());
                    userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                    userCoupon.setUseTime(null);
                    userCoupon.setCouponCreateTime(LocalDateTime.now());
                    userCoupon.setId(null);
                    userCoupon.setIsUsed(0);
                    userCoupon.setMemberName(wxMember.getMemberName());
                    //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                    //userCoupon.setCommodityNameStr(goodCoupon.getCouponCommodities().stream().map(CouponCommodityDO::getCommodityName).collect(Collectors.joining(" ")));
                    CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
                    userCoupon.setDistributionMethod(0L);
                    userCoupon.setMemberMobile(wxMember.getMemberMobile());
                    userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
                    userCoupon.setDeleted(Boolean.FALSE);
                    userCoupon.setCreator(creator);
                    userCoupon.setBusinessId(goodCoupon.getBusinessId());
                    userCoupons.add(userCoupon);
                }
                // 优惠券包关系表
                userCouponPackage = new UserCouponPackageDO();
                userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
                userCouponPackage.setUserId(wxMember.getMemberId());
                userCouponPackage.setPackageId(couponId);
                userCouponPackage.setPackageSource(CouponSourceConstant.COUPON_SOURCE_1);
                userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
                userCouponPackage.setMemberNickName(wxMember.getMemberName());
                userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
                userCouponPackage.setCreator(creator);
                userCouponPackages.add(userCouponPackage);
            }
            goodCouponService.updateReceivedNumAndCouponNumById(totalSendNum, goodCouponId);
        }
        couponPackageService.updateReceivedNumAndPackageNumById(num * size, couponId);

        Map<Long, List<UserCouponDO>> collect = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
        Map<Long, List<UserCouponPackageDO>> collect2 = userCouponPackages.stream()
                .collect(Collectors.groupingBy(
                        coupon -> (coupon.getUserId() % 10)
                ));
//        for (Map.Entry<Long, List<UserCouponDO>> entry : collect.entrySet()) {
//            Long shardingValue = entry.getKey();
//            List<UserCouponDO> insertList = entry.getValue();
//            promotionThreadPool.execute(() -> {userCouponMasterService.insertBatch(shardingValue,insertList);});
//        }
        for (Long shardingValue = 0L; shardingValue < 10L; shardingValue++) {
            List<UserCouponDO> insertList = collect.get(shardingValue);
            if (CollectionUtil.isNotEmpty(insertList)) {
                Long finalShardingValue = shardingValue;
                promotionThreadPool.execute(() -> {
                    userCouponMasterService.insertBatch(finalShardingValue, insertList);
                });
            }
            List<UserCouponPackageDO> userCouponPackageDOS = collect2.get(shardingValue);
            if (CollectionUtil.isNotEmpty(userCouponPackageDOS)) {
                Long finalShardingValue = shardingValue;
                promotionThreadPool.execute(() -> {
                    couponPackageMasterService.batchInsert(finalShardingValue, userCouponPackageDOS);
                });
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 会员日发放
     */
    public void memberDayCoupon() {
        jobService.memberDayCoupon();
    }

    public void memberCardBenefitJob(){
        jobService.memberCardBenefitJob();
    }


    public List<UserCouponDO> addUserCouponToList(List<GoodCouponDO> goodCouponList, Long userId, String memberMobile) {
        List<UserCouponDO> userCoupons = new ArrayList<>();
        for (GoodCouponDO goodCoupon : goodCouponList) {
            UserCouponDO coupon = new UserCouponDO();
            BeanUtils.copyProperties(goodCoupon, coupon);
            coupon.setId(null);
            coupon.setUserId(userId);
            coupon.setCouponId(goodCoupon.getId());
            coupon.setIsUsed(0);
            parseUserCouponTime(goodCoupon, coupon);
            coupon.setUseTime(null);
            coupon.setCreateTime(LocalDateTime.now());
            coupon.setUpdateTime(LocalDateTime.now());
            coupon.setMemberMobile(memberMobile);
            coupon.setDeleted(false);
            coupon.setCouponCreateTime(LocalDateTime.now());
            userCoupons.add(coupon);
        }
        return userCoupons;
    }

    private void parseUserCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            userCoupon.setExpirationTime(goodCoupon.getCouponEndTime());
            userCoupon.setVaildStartTime(goodCoupon.getCouponStartTime());
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            userCoupon.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), DateUtils.YYYY_MM_DD);
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }
    /**
     * 提取所有会员DTO中的优惠券ID
     */
    private Set<Long> extractAllCouponIds(Set<MemberCouponDTO> result) {
        return result.stream()
                .filter(Objects::nonNull)
                .flatMap(dto -> dto.getCouponIds().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 批量查询优惠券信息并转换为Map（couponId -> GoodCouponRespVO）
     */
    private Map<Long, GoodCouponRespVO> batchQueryCoupons(Set<Long> couponIds) {
        // 批量查询优惠券DO
        List<GoodCouponDO> goodCouponDOList = goodCouponService.listByIds(couponIds);
        if (CollectionUtils.isEmpty(goodCouponDOList)) {
            throw exception(COUPON_NOT_EXISTS);
        }

        return goodCouponDOList.stream()
                .collect(Collectors.toMap(
                        GoodCouponDO::getId,
                        doObj -> BeanUtil.toBean(doObj, GoodCouponRespVO.class),
                        (k1, k2) -> k1 // 重复ID取第一个
                ));
    }

    /**
     * 校验优惠券有效性（上架状态、过期时间）
     */
    private void validateCoupon(GoodCouponRespVO goodCoupon, Long couponId) {
        // 优惠券不存在校验
        if (ObjectUtil.isEmpty(goodCoupon)) {
            throw exception(COUPON_NOT_EXISTS, "优惠券ID：" + couponId);
        }
        // 优惠券未上架校验
        if (goodCoupon.getIsGround() != 1) {
            throw exception(COUPON_NOT_ON_SHELF, "优惠券ID：" + couponId);
        }
        // 优惠券过期校验（使用类型为0时）
        Date couponEndTime = goodCoupon.getCouponEndTime();
        if (goodCoupon.getUseType() == 0 && ObjectUtil.isNotEmpty(couponEndTime) && DateUtil.compare(new Date(), couponEndTime) > 0) {
            throw exception(COUPON_EXPIRED, "优惠券ID：" + couponId + "，过期时间：" + couponEndTime);
        }
    }

    /**
     * 构建用户优惠券DO对象
     */
    private UserCouponDO buildUserCouponDO(MemberCouponDTO dto, GoodCouponRespVO goodCoupon, Long couponId) {
        UserCouponDO userCoupon = new UserCouponDO();
        BeanUtil.copyProperties(goodCoupon, userCoupon);

        // 会员相关字段
        userCoupon.setUserId(dto.getMemberId());
        userCoupon.setMemberName(dto.getMemberName());
        userCoupon.setMemberMobile(dto.getMemberMobile());

        // 优惠券相关字段
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponUseTime(goodCoupon.getUseTime());
        userCoupon.setUseTime(null);
        userCoupon.setCouponCreateTime(LocalDateTime.now());
        userCoupon.setId(null);
        userCoupon.setIsUsed(0);
        userCoupon.setDistributionMethod(0L);
        userCoupon.setCouponSource(UserCouponConstants.COUPON_SOURCE_6);
        userCoupon.setDeleted(Boolean.FALSE);
        userCoupon.setBusinessId(10L);
        userCoupon.setCreateTime(LocalDateTime.now());
        userCoupon.setUpdateTime(LocalDateTime.now());
        userCoupon.setCommodityNameStr(Optional.ofNullable(goodCoupon.getCouponCommodities())
                .orElse(Collections.emptyList())
                .stream()
                .map(CouponCommodityDO::getCommodityName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" ")));

        // 优惠券时间（生效/失效时间）
        CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);

        userCoupon.setCreator("会员日发放0001");

        return userCoupon;
    }


}
