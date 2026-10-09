package com.htyoudao.youdao.module.promotion.service.job;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDayDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.api.wxmembercard.WxMemberCardApi;
import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardJobDTO;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.promotion.api.enums.IsGroundConstant;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.constant.CouponSourceConstant;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponPackageReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponDateVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.MemberExlVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.AdvertisingDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponRedeem.DouyinCouponRedeemRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketPhoneDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJD.ActivityJDMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.advertising.AdvertisingMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage.CouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.market.SmsMarketMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.MemberCardBenefitRedisDAO;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.enums.CouponStatusEnum;
import com.htyoudao.youdao.module.promotion.enums.DouyinCouponStatusEnum;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDCacheService;
import com.htyoudao.youdao.module.promotion.service.couponRedeem.DouyinCouponRedeemRecordService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.douyin.DyClient;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.DouYinApiResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.QueryResponse;
import com.htyoudao.youdao.module.promotion.service.exchangelog.ExchangeLogService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.goodcouponpackage.GoodCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotterySettingService;
import com.htyoudao.youdao.module.promotion.service.market.SmsMarketPhoneService;
import com.htyoudao.youdao.module.promotion.service.market.SmsService;
import com.htyoudao.youdao.module.promotion.service.userCouponPackage.UserCouponPackageService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponShardService;
import com.htyoudao.youdao.module.promotion.util.CouponScheduledTimeUtil;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.promotion.util.redis.RedisForAppletAd;
import com.htyoudao.youdao.module.system.api.business.BusinnessApi;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.xxl.job.core.context.XxlJobHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_NO_REST;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.SEND_ERROR;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_NO_REST;


@Slf4j
@Service
@RefreshScope
public class JobServiceImpl implements JobService {



    @Resource
    private RedisForAppletAd redisForAppletAd;


    @Resource
    private AdvertisingMapper advertisingMapper;

    @Resource
    private ActivityMapper activityMapper;


    @Resource
    private ActivityJDMapper activityJDMapper;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private MemberCardBenefitRedisDAO memberCardBenefitRedisDAO;

    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;


    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private DouyinCouponRedeemRecordService douyinCouponRedeemRecordService;

    @Resource
    private LotterySettingService lotterySettingService;
    //@DubboReference(timeout = 50000,pa)
    //@DubboReference(methods = {@Method(name = "getDayMembers", parameters = {"payload", "83886080"})},timeout = 50000)
    @DubboReference(
            methods = {
                    @Method(name = "getDayMembers", parameters = {"payload", "83886080"}, timeout = 50000),
                    @Method(name = "getMemberByMemberLevel", parameters = {"payload", "83886080"}, timeout = 50000)
            },
            timeout = 50000
    )
    private WxMemberApi wxMemberApi;

    @DubboReference
    private WxMemberCardApi wxMemberCardApi;

    @DubboReference
    private BusinnessApi businnessApi;



    @Resource
    private DyClient dyClient;

    @Resource
    private SmsMarketMapper smsMarketMapper;

    @Resource
    private RedisCache redisCache;


    @Value("${market.sms.signName}")
    private String signName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MARKET_USER_KEY = "market:user:";


    @Value("${market.sms.templateParam}")
    private String templateParam;

    @Value("${market.sms.templateCode}")
    private String templateCode;

    @Value("${promotion.member-card-benefit.write-thread-num:10}")
    private Integer memberCardBenefitWriteThreadNum;

    @Value("${promotion.member-card-benefit.write-chunk-size:1000}")
    private Integer memberCardBenefitWriteChunkSize;

    @Value("${promotion.member-card-benefit.member-whitelist-enabled:false}")
    private Boolean memberCardBenefitMemberWhitelistEnabled;

    @Value("${promotion.member-card-benefit.member-whitelist-ids:}")
    private String memberCardBenefitMemberWhitelistIds;


    @Resource
    private SmsService smsService;


    @Resource
    private SmsMarketPhoneService smsMarketPhoneService;

    @Resource
    private UserCouponRecordMapper userCouponRecordMapper;

    @Resource
    private CouponPackageMapper couponPackageMapper;


    // 优惠券数据 的 key
    private static final String COUPON_DATA = "COUPON_DATA:";

    @Resource
    private UserCouponShardService userCouponShardService;


    @Resource
    private LotteryLogService lotteryLogService;


    @Resource
    private GoodCouponPackageService goodCouponPackageService;

    @Resource
    private CouponPackageService couponPackageService;


    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserCouponPackageService userCouponPackageService;

    @Resource
    private UserCouponPackageMapper userCouponPackageMapper;


    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @DubboReference
    private BzOrderApi bzOrderApi;

    @Resource
    private ExchangeLogService exchangeLogService;

    @Resource
    private ActivityJDCacheService activityJDCacheService;
    private static final int BATCH_SIZE = 3000;
    private static final int MEMBER_CARD_BENEFIT_BATCH_SIZE = 2000;
    private static final int MEMBER_CARD_BENEFIT_DEFAULT_WRITE_CHUNK_SIZE = 1000;
    private static final int MEMBER_CARD_BENEFIT_DEFAULT_WRITE_THREAD_NUM = 10;
    private static final int MEMBER_CARD_BENEFIT_WRITE_QUEUE_CAPACITY = 100;
    private static final long MEMBER_CARD_BENEFIT_WRITE_KEEP_ALIVE_SECONDS = 60L;
    private static final int SEND_COUPON_RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 500;
    private static final int SHARD_THREAD_NUM = 5;
    private static final int MEMBER_CARD_SHARD_THREAD_NUM = 2;
    private static final int MEMBER_BENEFIT_SCENE = 1;
    private static final int BIRTHDAY_BENEFIT_SCENE = 2;
    private static final int UPGRADE_BENEFIT_SCENE = 3;
    private static final int COUPON_TYPE_COUPON = 1;
    private static final int REPEAT_TYPE_WEEK = 1;
    private static final int REPEAT_TYPE_MONTH = 2;
    private ExecutorService couponSendExecutor;
    @Override
    public void screenAdvertisements() {
        LambdaQueryWrapper<AdvertisingDO> lq = new LambdaQueryWrapper<>();
        lq.eq(AdvertisingDO::getDeleted, 0);
        List<AdvertisingDO> list = advertisingMapper.selectList(lq);
        for (AdvertisingDO advertising : list) {
            Integer isOpen = getIsOpen(advertising);
            LambdaUpdateWrapper<AdvertisingDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(AdvertisingDO::getIsOpen, isOpen);
            updateWrapper.eq(AdvertisingDO::getId, advertising.getId());
            advertisingMapper.update(updateWrapper);

        }
        CommonResult<List<BusinessDTO>> listCommonResult = businnessApi.listAll();
        List<BusinessDTO> data = listCommonResult.getData();
        if (ObjectUtil.isNotEmpty(data)) {
            for (BusinessDTO dto : data) {
                redisForAppletAd.delAllStore(dto.getId());
            }
        }

    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public void memberDayCoupon() {
        long startTime = System.currentTimeMillis();
        log.info("会员日发券流程开始（分片并行+发券异步）");

        couponSendExecutor = new ThreadPoolExecutor(
                10, // 核心线程数
                20, // 最大线程数
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), // 增大队列容量
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        ExecutorService shardExecutor = Executors.newFixedThreadPool(SHARD_THREAD_NUM);
        // 存储所有分片的异步任务（包含发券任务的等待）
        List<CompletableFuture<Void>> shardFutureList = new ArrayList<>();

        // 2. 提交分片任务
        for (int shard = 0; shard < 10; shard++) {
            int finalShard = shard;
            CompletableFuture<Void> shardFuture = CompletableFuture.runAsync(() -> {
                processShardAsync(finalShard);
            }, shardExecutor);
            shardFutureList.add(shardFuture);
        }

        // 等待所有分片任务（含内部发券任务）全部完成
        CompletableFuture.allOf(shardFutureList.toArray(new CompletableFuture[0])).join();

        if (couponSendExecutor != null) {
            couponSendExecutor.shutdown(); // 发起关闭请求（不再接收新任务）
            try {
                // 等待60秒，让剩余任务执行完成
                if (!couponSendExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    couponSendExecutor.shutdownNow(); // 超时强制关闭
                    log.warn("发券线程池超时未关闭，强制终止剩余任务");
                }
            } catch (InterruptedException e) {
                couponSendExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("发券线程池关闭被中断", e);
            }
        }

        shardExecutor.shutdown();
        try {
            if (!shardExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                shardExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            shardExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        log.info("所有分片+发券任务处理完成，总耗时：{}毫秒", endTime - startTime);
    }

    /**
     * 单个分片处理（确保所有发券任务完成后再结束）
     */
    private void processShardAsync(int shard) {
        log.info("线程{}：开始处理分片{}", Thread.currentThread().getName(), shard);
        Long lastCursorId = 0L;
        boolean hasMoreData = true;
        // 存储当前分片的所有发券异步任务
        List<CompletableFuture<Void>> couponFutureList = new ArrayList<>();

        try {
            while (hasMoreData) {
                Map<Integer, List<WxMemberDTO>> levelMemberMap =
                        wxMemberApi.getMembersMapByShard(shard, lastCursorId, BATCH_SIZE);

                if (levelMemberMap.isEmpty()) {
                    hasMoreData = false;
                    log.info("线程{}：分片{}游标{}后无更多数据", Thread.currentThread().getName(), shard, lastCursorId);
                    continue;
                }

                BatchProcessResult batchResult = buildCouponDTO(levelMemberMap);
                if (CollectionUtils.isEmpty(batchResult.getCouponSet())) {
                    lastCursorId = batchResult.getMaxCursorId();
                    continue;
                }

                // 异步发券
                long startCursor = lastCursorId;
                long endCursor = batchResult.getMaxCursorId();
                CompletableFuture<Void> couponFuture = CompletableFuture.runAsync(() -> {
                    sendCouponWithRetry(shard, startCursor, endCursor, batchResult.getCouponSet());
                }, couponSendExecutor);

                couponFutureList.add(couponFuture);
                lastCursorId = endCursor;
                Thread.sleep(50);
            }

            // 等待当前分片的所有发券任务完成后，再结束分片任务
            if (!couponFutureList.isEmpty()) {
                CompletableFuture.allOf(couponFutureList.toArray(new CompletableFuture[0])).join();
                log.info("线程{}：分片{}所有发券任务已完成", Thread.currentThread().getName(), shard);
            }
        } catch (Exception e) {
            log.error("线程{}：分片{}处理异常", Thread.currentThread().getName(), shard, e);
        }
        log.info("线程{}：分片{}处理完成", Thread.currentThread().getName(), shard);
    }

    /**
     * 发券重试逻辑（无修改）
     */
    private void sendCouponWithRetry(int shard, Long startCursor, Long endCursor, Set<MemberCouponDTO> couponSet) {
        boolean sendSuccess = false;
        int retryCount = 0;

        while (retryCount < SEND_COUPON_RETRY_TIMES && !sendSuccess) {
            try {
                userCouponService.sendCoupons(couponSet);
                sendSuccess = true;
                log.info("异步发券-线程{}：分片{}游标{}~{}成功，共{}条",
                        Thread.currentThread().getName(), shard, startCursor, endCursor, couponSet.size());
            } catch (Exception e) {
                retryCount++;
                log.error("异步发券-线程{}：分片{}游标{}~{}失败（重试第{}次）",
                        Thread.currentThread().getName(), shard, startCursor, endCursor, retryCount, e);
                try {
                    Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!sendSuccess) {
            log.error("异步发券-线程{}：分片{}游标{}~{}最终失败，需人工处理",
                    Thread.currentThread().getName(), shard, startCursor, endCursor);
        }
    }
    private BatchProcessResult buildCouponDTO(Map<Integer, List<WxMemberDTO>> levelMemberMap) {
        Set<MemberCouponDTO> couponSet = new HashSet<>();
        AtomicLong maxCursorId = new AtomicLong(0L);

        for (Map.Entry<Integer, List<WxMemberDTO>> entry : levelMemberMap.entrySet()) {
            Integer memberLevel = entry.getKey();
            List<WxMemberDTO> memberList = entry.getValue();
            List<Long> couponIds = LEVEL_COUPON_MAP.get(memberLevel);

            if (Objects.isNull(memberLevel) || memberLevel == 1
                    || CollectionUtils.isEmpty(memberList) || CollectionUtils.isEmpty(couponIds)) {
                continue;
            }

            List<MemberCouponDTO> batchDTO = memberList.stream()
                    .filter(dto -> Objects.nonNull(dto) && Objects.nonNull(dto.getMemberId()) && Objects.nonNull(dto.getMemberId()))
                    .peek(dto -> maxCursorId.updateAndGet(prev -> Math.max(prev, dto.getMemberId())))
                    .map(dto -> {
                        MemberCouponDTO couponDTO = new MemberCouponDTO();
                        couponDTO.setMemberId(dto.getMemberId());
                        couponDTO.setMemberMobile(dto.getMemberMobile());
                        couponDTO.setMemberName(dto.getMemberName());
                        couponDTO.setCouponIds(couponIds);
                        return couponDTO;
                    })
                    .collect(Collectors.toList());

            couponSet.addAll(batchDTO);
        }

        return new BatchProcessResult(couponSet, maxCursorId.get());
    }

    // 内部类（无修改）
    private static class BatchProcessResult {
        private final Set<MemberCouponDTO> couponSet;
        private final Long maxCursorId;

        public BatchProcessResult(Set<MemberCouponDTO> couponSet, Long maxCursorId) {
            this.couponSet = couponSet;
            this.maxCursorId = maxCursorId;
        }

        public Set<MemberCouponDTO> getCouponSet() {
            return couponSet;
        }

        public Long getMaxCursorId() {
            return maxCursorId;
        }
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


    public Integer getIsOpen(AdvertisingDO advertising) {

        Integer isOpen = 1;
        if (advertising.getIsAllTime().equals(1)) {
            return isOpen;
        }
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        //当前日期
        Integer currDate = Integer.parseInt(DateUtils.getDate().replace("-", ""));
        //当前日
        int day = now.get(Calendar.DAY_OF_MONTH);

        boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
        int week = now.get(Calendar.DAY_OF_WEEK);
        // 若一周第一天为星期天，则-1
        if (isFirstSunday) {
            week = week - 1;
            if (week == 0) {
                week = 7;
            }
        }


        if (ObjectUtil.isNotEmpty(advertising.getStartTime())) {
            Integer begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertising.getStartTime()).replace("-", ""));
            //结束日期

            Integer end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, advertising.getEndTime()).replace("-", ""));

            if (begin <= currDate && currDate <= end) {
                //在日期范围内
                isOpen = 1;
            } else {
                isOpen = 2;
            }
        }
        if (isOpen == 1) {
            if (ObjectUtil.isNotEmpty(advertising.getDayNumberList())) {
                if (advertising.getDayNumberList().contains(day)) {
                    isOpen = 1;
                } else {
                    isOpen = 2;
                }
            }
        }

        if (isOpen == 1) {
            if (ObjectUtil.isNotEmpty(advertising.getWeekNumberList())) {
                if (advertising.getWeekNumberList().contains(week)) {
                    isOpen = 1;
                } else {
                    isOpen = 2;
                }
            }
        }
        return isOpen;
    }

    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    @Override
    public void cancelDouyinCoupon() {
        log.info("==> 执行抖音券清理定时任务");
        //找出没有核销成功的记录， 要么是核销成功过，但做了订单取消走了撤销核销，要么是就没核销成功过
        List<DouyinCouponRedeemRecordDO> list = douyinCouponRedeemRecordService.list(
                new LambdaQueryWrapper<DouyinCouponRedeemRecordDO>()
                        .eq(DouyinCouponRedeemRecordDO::getStatus, CouponStatusEnum.TO_BE_USED.getCode())
//                        .isNotNull(DouyinCouponRedeemRecordDO::getVerifyResponse)
        );

        list.forEach(douyinCouponRedeemRecordDO -> {
            DouYinApiResponse<QueryResponse> queryResponse = dyClient.query(douyinCouponRedeemRecordDO.getDouyinOrderId());
            QueryResponse data = queryResponse.getData();

            data.getCertificates().forEach(certificate -> {
                //抖音退款的直接删除平台内部优惠券
                if (DouyinCouponStatusEnum.REFUND_SUCCESS.getCode() == certificate.getStatus()) {
                    userCouponMapper.update(
                            new LambdaUpdateWrapper<UserCouponDO>()
                                    .eq(UserCouponDO::getId, douyinCouponRedeemRecordDO.getUserCouponId())
                                    .eq(UserCouponDO::getUserId, douyinCouponRedeemRecordDO.getUserId())
                                    .set(UserCouponDO::getDeleted, 1)
                    );

                    douyinCouponRedeemRecordService.remove(
                            new LambdaQueryWrapper<DouyinCouponRedeemRecordDO>()
                                    .eq(DouyinCouponRedeemRecordDO::getId, douyinCouponRedeemRecordDO.getId())
                    );
                }
            });
        });
    }

    @Override
    @DataPermission(enable = false)
    public void scheduledSendMessage() {

        String time = DateUtils.getTimeNow();
        QueryWrapper<SmsMarketDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SmsMarketDO::getSendTime, time)
                .eq(SmsMarketDO::getSendStatus, 1)
                .eq(SmsMarketDO::getSendMethod, 1);
        SmsMarketDO smsMarket = smsMarketMapper.selectOne(queryWrapper);

        if (ObjectUtil.isEmpty(smsMarket)) {
            return;
        }

        Long id = smsMarket.getId();
        List<String> groupedPhoneNumbers = new ArrayList<>();
        groupedPhoneNumbers.add("13194235615");
        groupedPhoneNumbers.add("13543437336");
        groupedPhoneNumbers.add("13940562934");

        groupedPhoneNumbers.add("15304001307");
        groupedPhoneNumbers.add("18202489118");
        groupedPhoneNumbers.add("15640572470");
        groupedPhoneNumbers.add("18940536673");

        List<WxMemberDTO> wxMembers = wxMemberApi.getMemberByMobiles(groupedPhoneNumbers);
        List<Long> ids = wxMembers.stream().map(WxMemberDTO::getMemberId).toList();
        redisCache.opsForSet(MARKET_USER_KEY + id, new HashSet<>(ids));
        SmsMarketDO byId = smsMarketMapper.selectById(id);
        String shortCode = byId.getShortCode();
        try {
            List<String> signNames = Collections.nCopies(groupedPhoneNumbers.size(), signName);
            Map<String, String> stringStringMap = objectMapper.readValue(templateParam, new TypeReference<Map<String, String>>() {
            });
            stringStringMap.put("code", shortCode);
            List<Map<String, String>> templateParams = Collections.nCopies(groupedPhoneNumbers.size(), stringStringMap);

            log.info("发送短信：{}{}{}{}", signNames, groupedPhoneNumbers, templateCode, templateParams);
            String s = smsService.sendBatch(groupedPhoneNumbers, signNames, templateCode, templateParams);
            if ("fail".equals(s)) {
                List<SmsMarketPhoneDO> list = groupedPhoneNumbers.stream().map(item -> {
                    SmsMarketPhoneDO smsMarketPhone = new SmsMarketPhoneDO();
                    smsMarketPhone.setPhone(item);
                    smsMarketPhone.setSmsTemplateId(id);
                    return smsMarketPhone;
                }).toList();
                smsMarketPhoneService.insertBatch(list);
                throw exception(SEND_ERROR);
            }
        } catch (Exception e) {
            log.warn("定时发送短信失败");
        }
    }


    @Override
    @DataPermission(enable = false)
    public void nocCouponDataToRedis() {
        HashOperations<String, String, Object> hashOperations = redisCache.getHash();
        LocalDateTime yesterdayLastSecond = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).minusSeconds(1);
        QueryWrapper<UserCouponRecordDO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().le(UserCouponRecordDO::getCreateTime, yesterdayLastSecond);
        List<UserCouponRecordDO> userCouponRecords = userCouponRecordMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(userCouponRecords)) {
            return;
        }
        Map<Long, List<UserCouponRecordDO>> ageGroupMap = userCouponRecords.stream()
                .collect(Collectors.groupingBy(UserCouponRecordDO::getCouponId));

        List<GoodCouponDO> goodCoupons = goodCouponMapper.selectList();
        Map<Long, Integer> receivedNumMap = goodCoupons.stream().collect(Collectors.toMap(GoodCouponDO::getId, GoodCouponDO::getReceivedNum));


        ageGroupMap.forEach((k, v) -> {
            GoodCouponDateVO couponDateVO = new GoodCouponDateVO();
            // 支付金额
            BigDecimal totalAmount = v.stream().map(UserCouponRecordDO::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            couponDateVO.setTurnover(totalAmount);
            // 优惠金额
            BigDecimal couponAmount = v.stream().map(UserCouponRecordDO::getCouponAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            couponDateVO.setOfferTotal(couponAmount);
            BigDecimal multiplied = couponAmount.multiply(new BigDecimal("100"));
            // 费效比
            if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal cost = multiplied.divide(totalAmount, 2, RoundingMode.HALF_UP);
                couponDateVO.setCost(cost);
            } else {
                couponDateVO.setCost(BigDecimal.ZERO);
            }

            // 订单数
            int orderNum = v.size();
            couponDateVO.setOrderNum(orderNum);
            //单价
            BigDecimal singelPrice = totalAmount.divide(new BigDecimal(orderNum), 2, RoundingMode.HALF_UP);
            couponDateVO.setSinglePrice(singelPrice);
            //使用率
            Integer receivedNum = receivedNumMap.get(k);
            if (ObjectUtil.isNotEmpty(receivedNum)) {
                if (receivedNum != 0) {
                    BigDecimal usedRate = new BigDecimal(orderNum).multiply(new BigDecimal("100"))
                            .divide(new BigDecimal(receivedNum), 2, RoundingMode.HALF_UP);
                    couponDateVO.setUsedRate(usedRate);
                } else {
                    couponDateVO.setUsedRate(BigDecimal.ZERO);
                }
            }
            // 购买数量
            int commodityNum = v.stream().mapToInt(UserCouponRecordDO::getItemNum).sum();
            couponDateVO.setItemNum(commodityNum);
            hashOperations.put(COUPON_DATA, k.toString(), couponDateVO);
        });
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void lotteryReturnReal() {
        lotteryLogService.returnReal();
    }

    @Override
    @DataPermission(enable = false)
    public void xxjobSpecifyDateCoupon() {
        QueryWrapper<GoodCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GoodCouponDO::getIsGround, GoodCouponConstants.IS_GROUND_1);
        queryWrapper.lambda().eq(GoodCouponDO::getDistributionMethod, GoodCouponConstants.DISTRIBUTE_METHOD_0);
        List<GoodCouponDO> goodCoupons = goodCouponMapper.selectList(queryWrapper);

        for (GoodCouponDO goodCoupon : goodCoupons) {
            // 判断今天是否该发券
            Boolean b = CouponScheduledTimeUtil.validateCouponTime(goodCoupon);
            if (!b) {
                log.info("优惠券{}不发放", goodCoupon.getId());
                break;
            }
            // 获取优惠券的会员等级
            Integer memberLevel = goodCoupon.getMemberLevel();
            // 获取此会员等级的会员
            CommonResult<List<WxMemberDTO>> result = wxMemberApi.getMemberByMemberLevel(memberLevel);
            List<WxMemberDTO> members = result.getData();

            // 获取会员的数量 优惠券库存够不够
            int size = members.size();
            Integer couponNum = goodCoupon.getCouponNum();
            if (size > couponNum) {
                break;
            }
            log.info("优惠券{}发放", goodCoupon.getId());
            List<UserCouponDO> list = new ArrayList<>();
            UserCouponDO userCouponDO = new UserCouponDO();
            for (int i = 0; i < size; i++) {
                userCouponDO = new UserCouponDO();
                com.htyoudao.youdao.framework.common.util.object.BeanUtils.copyProperties(goodCoupon, userCouponDO);
                GoodCouponRespVO goodCouponRespVO = com.htyoudao.youdao.framework.common.util.object.BeanUtils.toBean(goodCoupon, GoodCouponRespVO.class);
                WxMemberDTO member = members.get(i);
                Long memberId = member.getMemberId();
                Long couponId = goodCoupon.getId();
                userCouponDO.setCouponCode(goodCoupon.getCouponCode());
                userCouponDO.setId(null);
                userCouponDO.setUserId(memberId);
                userCouponDO.setCouponId(goodCoupon.getId());
                userCouponDO.setIsUsed(UserCouponConstants.IS_USED_0);
                userCouponDO.setCouponUseTime(goodCoupon.getUseTime());
                userCouponDO.setUseTime(null);
                userCouponDO.setCouponCreateTime(goodCoupon.getCreateTime());

                CouponTimeUtil.parseCouponTime(goodCouponRespVO, userCouponDO);
                userCouponDO.setUseTime(null);
                userCouponDO.setCreateTime(LocalDateTime.now());
                userCouponDO.setUpdateTime(LocalDateTime.now());
                userCouponDO.setMemberMobile(member.getMemberMobile());
                userCouponDO.setDeleted(Boolean.FALSE);
                list.add(userCouponDO);
            }

            userCouponShardService.insertBatch(list);
            UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper = new UpdateWrapper<>();
            goodCouponUpdateWrapper.setSql("received_num = received_num + " + size);
            goodCouponUpdateWrapper.setSql("coupon_num = coupon_num - " + size);
            goodCouponUpdateWrapper.eq("id", goodCoupon.getId());
            goodCouponMapper.update(goodCouponUpdateWrapper);
        }
    }

    @Override
    @DataPermission(enable = false)
    public void automaticDistributionOnMemberDaysJobHandler() {


        UserCouponDO userCoupon = new UserCouponDO();
        List<UserCouponDO> userCoupons = new ArrayList<>();

        List<UserCouponPackageDO> userCouponPackages = new ArrayList<>();
        UserCouponPackageDO userCouponPackage = new UserCouponPackageDO();

        //优惠券信息
        QueryWrapper<CouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_restrictions",4);
        CouponPackageDO couponPackageDO = couponPackageMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(couponPackageDO)) {
            throw exception(COUPON_NOT_EXISTS);
        }
        Long packageId = couponPackageDO.getId();
        //Long packageId = 1943585381307908096L;

        //判断优惠券是否上架
        if (couponPackageDO.getIsGround() != IsGroundConstant.IS_GROUND_1) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }
        List<WxMemberDayDTO> memberList = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            List<WxMemberDayDTO> dayMembers = wxMemberApi.getDayMembers(i);
            memberList.addAll(dayMembers);
        }

//        List<WxMemberDayDTO> dayMembers = wxMemberApi.getDayMembers(8);
//        memberList.addAll(dayMembers);
        // 查询优惠券包
        List<GoodCouponPackageDO> goodCouponPackages = goodCouponPackageService.selectListByPackageId(packageId);

        List<Long> couponIds = goodCouponPackages.stream().map(goodCouponPackage -> goodCouponPackage.getCouponId()).toList();
        List<GoodCouponDO> list = goodCouponService.getByIds(couponIds);

        Map<Long, Integer> couponNumMap = goodCouponPackages.stream().collect(Collectors.toMap(GoodCouponPackageDO::getCouponId, GoodCouponPackageDO::getNum));

        //人数
        int size = memberList.size();

        GoodCouponDO goodCoupon1 = new GoodCouponDO();
        for (GoodCouponDO goodCoupon : list) {
            Long goodCouponId = goodCoupon.getId();
            //包中优惠券数量
            int packNum = couponNumMap.get(goodCoupon.getId());
            //优惠券数量
            int couponNum = goodCoupon.getCouponNum();

            int totalSendNum = size * packNum;
            for(int i = 0; i < couponNumMap.get(goodCouponId); i++){
                for (WxMemberDayDTO wxMember : memberList) {
                    userCoupon = new UserCouponDO();
                    BeanUtil.copyProperties(goodCoupon, userCoupon);
                    userCoupon.setUserId(wxMember.getMemberId());
                    userCoupon.setCouponId(goodCouponId);
                    userCoupon.setCouponUseTime(goodCoupon.getUseTime());
                    userCoupon.setUseTime(null);
                    userCoupon.setCouponCreateTime(goodCoupon.getCreateTime());
                    userCoupon.setId(null);
                    userCoupon.setIsUsed(0);
                    parseCouponTime(goodCoupon, userCoupon);
                    userCoupon.setDistributionMethod(0L);
                    userCoupon.setMemberMobile(wxMember.getMemberMobile());
                    userCoupon.setCouponSource(CouponSourceConstant.COUPON_SOURCE_10);
                    userCoupon.setPackageId(packageId);
                    userCoupon.setBusinessId(goodCoupon.getBusinessId());
                    userCoupons.add(userCoupon);
                    // 优惠券包关系表
                    userCouponPackage.setBusinessId(goodCoupon.getBusinessId());
                    userCouponPackage.setUserId(wxMember.getMemberId());
                    userCouponPackage.setPackageId(packageId);
                    userCouponPackage.setPackageSource(CouponSourceConstant.COUPON_SOURCE_10);

                    userCouponPackage.setMemberMobile(wxMember.getMemberMobile());
                    userCouponPackage.setMemberNickName(wxMember.getMemberNickName());
                }
            }
            goodCouponService.updateReceivedNumAndCouponNumById(totalSendNum,goodCouponId);

        }
        threadPoolTaskExecutor.execute(() -> {
            couponPackageService.updateReceivedNumAndPackageNumById(size,packageId);
            userCouponService.insertBatch(userCoupons);
            userCouponPackageService.insert(userCouponPackage);
        });
    }


    /**
     * 根据时效判断开始和过期时间 版本2
     *
     * @param goodCoupon
     */
    private void parseCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCoupon.setCouponStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_00_00_00));
            String endTime = com.htyoudao.youdao.framework.common.util.date.DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.dateTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + com.htyoudao.youdao.framework.common.util.date.DateUtils.T_23_59_59));
        }
    }
    @Override
    @DataPermission(enable = false)
    public void lotteryPoolReset() {
        lotterySettingService.lotteryPoolReset();
    }

    @Override
    @DataPermission(enable = false)
    public void resetPoint() {
        Date date = new Date();
        LambdaQueryWrapper<ActivityDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ActivityDO::getIsEnabled, 1);
        wrapper.eq(ActivityDO::getActivityType, 4);
        List<ActivityDO> activityDOS = activityMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(activityDOS)) {
            for (ActivityDO activityDO : activityDOS) {
                LambdaQueryWrapper<ActivityJDDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(ActivityJDDO::getActivityId, activityDO.getId());
                ActivityJDDO activityJDDO = activityJDMapper.selectOne(lambdaQueryWrapper);

                if (activityJDDO != null && !activityJDDO.getValidityPeriod().equals(0)) {
                    Date startDate = activityDO.getStartDate();
                    Date endDate = activityDO.getEndDate();

                    // 判断当前日期是否在活动期间内
                    if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
                        // 获取重置天数
                        Integer validityPeriod = activityJDDO.getValidityPeriod();

                        // 计算相隔天数
                        long daysBetween = calculateDaysBetween(startDate, date);

                        // 进行取余操作，余数为0说明符合重置条件
                        if (daysBetween % validityPeriod == 0) {
                            try{
                                bzOrderApi.delOrderPointsByActivityId(activityDO.getId());
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }
        //90天的数据
        LambdaQueryWrapper<ActivityDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(ActivityDO::getActivityType, 4);
        List<ActivityDO> activityDOList = activityMapper.selectList(queryWrapper);
        if(ObjectUtil.isNotEmpty(activityDOList)){
            for (ActivityDO activityDO : activityDOList) {
                LambdaQueryWrapper<ActivityJDDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(ActivityJDDO::getActivityId, activityDO.getId());
                ActivityJDDO activityJDDO = activityJDMapper.selectOne(lambdaQueryWrapper);
                if(activityJDDO!=null){
                    Date endDate = activityDO.getEndDate();
                    // 使用LocalDate进行精确的日期计算（忽略时间部分）
                    LocalDate currentDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    // 计算两个日期之间的天数差
                    long daysBetween = ChronoUnit.DAYS.between(currentDate, endLocalDate);

                    // 如果正好等于90天，继续执行下面的代码
                    if (daysBetween == 90) {
                        try{
                            bzOrderApi.delOrderPointsByActivityId(activityDO.getId());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
            }
        }



    }

    /**
     * 计算两个日期之间的天数差
     */
    private long calculateDaysBetween(Date startDate, Date endDate) {
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    @DataPermission(enable = false)
    public void automaticPointsGoodsJobHandler() {
        LambdaQueryWrapper<ActivityExchangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ActivityExchangeLogDO::getCreateTime,
                LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        wrapper.eq(ActivityExchangeLogDO::getDistributeMode, 2);
        List<ActivityExchangeLogDO> activityExchangeLogDOS = exchangeLogService.list(wrapper);
        WxMemberDTO wxMember = new WxMemberDTO();
        for (ActivityExchangeLogDO activityExchangeLogDO : activityExchangeLogDOS) {
            BusinessContextHolder.setBusinessId(activityExchangeLogDO.getBusinessId());
            wxMember = new WxMemberDTO();
            wxMember.setMemberId(activityExchangeLogDO.getMemberId());
            wxMember.setMemberMobile(activityExchangeLogDO.getMemberMobile());
            wxMember.setMemberNickName(activityExchangeLogDO.getMemberNickName());
            Integer awardType = activityExchangeLogDO.getAwardType();
            if(ObjectUtil.equal(awardType,1)){
                ActivityJDCouponReqSaveVO activityCoupon = activityJDCacheService.getActivityCoupon(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getForeignId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON);
                GoodCouponRespVO goodCouponRespVO = activityCoupon.getGoodCouponRespVO();
                if(ObjectUtil.isEmpty(goodCouponRespVO)){
                    goodCouponRespVO = goodCouponService.getAppCouponById(activityExchangeLogDO.getForeignId());
                    if(ObjectUtil.isEmpty(goodCouponRespVO)){
                        return;
                    }
                }
                log.info("会员ID发放隔日单奖品:{},{}",wxMember.getMemberId(),activityExchangeLogDO.getForeignId());
                try {
                    goodCouponRespVO.setBusinessId(activityExchangeLogDO.getBusinessId());
                    userCouponService.insertUserCouponWithSeckill(activityExchangeLogDO.getMemberId(), goodCouponRespVO, wxMember, CouponSourceType.COLLECT_POINTS.getCode(), activityExchangeLogDO.getStoreId());
                }catch (Exception e){
                    log.warn("会员ID发放隔日多奖品失败:{},{}",wxMember.getMemberId(),activityExchangeLogDO.getForeignId());
                    activityJDCacheService.decrClaimedCoupon(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getForeignId());
                    //activityJDCacheService.incrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());
                    activityJDCacheService.removeMemberClaimedCoupon(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getMemberId(), activityExchangeLogDO.getForeignId());
                    exchangeLogService.removeById(activityExchangeLogDO.getId());
                }
            }else {
                ActivityJDCouponPackageReqSaveVO activityCoupon = activityJDCacheService.getActivityCouponPackage(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getForeignId(), RedisKeyConstants.JD_ACTIVITY_GOODS_COUPON_PACKAGE);
                CouponPackageRespVO couponPackageRespVO = activityCoupon.getCouponPackageRespVO();
                if (ObjectUtil.isEmpty(couponPackageRespVO)) {
                    couponPackageRespVO = couponPackageService.selectById(activityExchangeLogDO.getForeignId());
                    if (ObjectUtil.isEmpty(couponPackageRespVO)) {
                        return;
                    }
                }
                log.info("会员ID发放隔日多奖品:{},{}",wxMember.getMemberId(),activityExchangeLogDO.getForeignId());
                try {
                    userCouponService.insertCouponPackageWithPoints(couponPackageRespVO, wxMember, activityExchangeLogDO.getUserRestrictions(), activityExchangeLogDO.getStoreId(), CouponSourceType.COLLECT_POINTS.getCode());
                } catch (Exception e) {
                    log.warn("会员ID发放隔日多奖品失败:{},{},{}",wxMember.getMemberId(),activityExchangeLogDO.getForeignId(),e.getMessage());
                    activityJDCacheService.decrClaimedCoupon(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getForeignId());
                    //activityJDCacheService.incrMemberPoints(claimCoupon.getActivityId(), claimCoupon.getMemberId(),activityCoupon.getRedeemPoints());
                    activityJDCacheService.removeMemberClaimedCoupon(activityExchangeLogDO.getActivityId(), activityExchangeLogDO.getMemberId(), activityExchangeLogDO.getForeignId());
                    exchangeLogService.removeById(activityExchangeLogDO.getId());
                }
            }
        }
    }

    @Override
    public void memberCardBenefitJob() {
        long startTime = System.currentTimeMillis();

        String jobParam = XxlJobHelper.getJobParam();
        Long businessId = Long.parseLong(jobParam);


        int shardIndex = XxlJobHelper.getShardIndex(); // 0、1、2、3、4...
        int shardTotal = XxlJobHelper.getShardTotal(); // 作业实例总数
        // ======================================================================



        log.info("会员卡权益每日发放任务分片，businessId={}，分片索引：{}，分片总数：{}", businessId, shardIndex, shardTotal);

        // 单次任务固定使用同一份白名单快照，避免 Nacos 刷新后不同分片处理范围不一致。
        MemberCardBenefitMemberWhitelist memberWhitelist = snapshotMemberCardBenefitMemberWhitelist();
        log.info("会员卡权益每日发放任务会员白名单配置，businessId={}，开关={}，有效会员数={}",
                businessId, memberWhitelist.enabled(), memberWhitelist.memberIds().size());
        if (memberWhitelist.enabled() && memberWhitelist.memberIds().isEmpty()) {
            log.warn("会员卡权益每日发放任务跳过，会员白名单开关已开启但未配置有效会员ID，businessId={}", businessId);
            return;
        }

        MemberCardJobConfig config;
        try {
            config = buildMemberCardJobConfig(businessId);
        } catch (Exception e) {
            log.error("会员卡权益每日发放任务加载会员卡配置失败，businessId={}", businessId, e);
            return;
        }
        if (config.cardIds().isEmpty()) {
            log.info("会员卡权益每日发放任务跳过，本次没有可用会员卡配置，businessId={}", businessId);
            return;
        }


        List<Integer> currentShards = new ArrayList<>();

        //分片逻辑
        extracted(shardTotal, currentShards, shardIndex);

        if (currentShards.isEmpty()) {
            log.info("当前pod无分配分片，直接结束 shardIndex={}", shardIndex);
            return;
        }

        int threadNum = currentShards.size();
        log.info("会员卡权益每日发放任务 当前pod分配分片：{}，线程数：{}，businessId={}", currentShards, threadNum, businessId);

        ExecutorService shardExecutor = Executors.newFixedThreadPool(threadNum);
        List<Future<Long>> futures = new ArrayList<>();
        for (Integer currentShard : currentShards) {
            futures.add(shardExecutor.submit(() -> {
                long shardStartTime = System.currentTimeMillis();
                long shardInsertedCount = 0L;
                log.info("会员卡权益每日发放任务开始处理分片，businessId={}，分片={}", businessId, currentShard);
                try {
                    shardInsertedCount = processMemberCardBenefitShard(businessId, currentShard, config, memberWhitelist);
                    return shardInsertedCount;
                } finally {
                    long shardCost = System.currentTimeMillis() - shardStartTime;
                    log.info("会员卡权益每日发放任务分片处理结束，businessId={}，分片={}，写入券数={}，耗时={}ms，吞吐={}行/s",
                            businessId, currentShard, shardInsertedCount, shardCost, calculateRowsPerSecond(shardInsertedCount, shardCost));
                }
            }));
        }

        long podInsertedCount = 0L;
        for (Future<Long> future : futures) {
            try {
                podInsertedCount += future.get();
            } catch (Exception e) {
                log.error("会员卡权益每日发放任务分片执行异常，businessId={}", businessId, e);
            }
        }
        shardExecutor.shutdown();

        long totalCost = System.currentTimeMillis() - startTime;
        log.info("会员卡权益每日发放任务执行结束，businessId={}，写入券数={}，总耗时={}ms，pod吞吐={}行/s",
                businessId, podInsertedCount, totalCost, calculateRowsPerSecond(podInsertedCount, totalCost));
    }

    private static void extracted(int shardTotal, List<Integer> currentShards, int shardIndex) {
        if (shardTotal == 1) {
            for (int i = 0; i < 10; i++) currentShards.add(i);
        } else if (shardTotal == 2) {
            if (shardIndex == 0) for (int i = 0; i <= 4; i++) currentShards.add(i);
            else if (shardIndex == 1) for (int i = 5; i <= 9; i++) currentShards.add(i);
        } else if (shardTotal == 3) {
            if (shardIndex == 0) for (int i = 0; i <= 3; i++) currentShards.add(i);
            else if (shardIndex == 1) for (int i = 4; i <= 6; i++) currentShards.add(i);
            else if (shardIndex == 2) for (int i = 7; i <= 9; i++) currentShards.add(i);
        } else if (shardTotal == 4) {
            if (shardIndex == 0) for (int i = 0; i <= 2; i++) currentShards.add(i);
            else if (shardIndex == 1) for (int i = 3; i <= 5; i++) currentShards.add(i);
            else if (shardIndex == 2) for (int i = 6; i <= 7; i++) currentShards.add(i);
            else if (shardIndex == 3) for (int i = 8; i <= 9; i++) currentShards.add(i);
        } else {
            int start = shardIndex * 2;
            int end = start + 1;
            if (start < 10) {
                currentShards.add(start);
                if (end < 10) currentShards.add(end);
            }
        }
    }

    private MemberCardJobConfig buildMemberCardJobConfig(Long businessId) {
        List<WxMemberCardJobDTO> cards = Optional.ofNullable(wxMemberCardApi.listMemberCardJobCards(businessId)).orElse(Collections.emptyList());
        if (cards.isEmpty()) {
            log.info("会员卡权益预热完成，无可用会员卡配置，businessId={}", businessId);
            return new MemberCardJobConfig(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList());
        }

        List<Long> memberCardIds = cards.stream()
                .map(WxMemberCardJobDTO::getMemberCardId)
                .filter(Objects::nonNull)
                .toList();
        List<WxMemberCardBenefitJobDTO> benefits = Optional.ofNullable(wxMemberCardApi.listMemberCardJobBenefits(businessId, memberCardIds))
                .orElse(Collections.emptyList());

        log.info("会员卡权益开始预热，businessId={}，会员卡数量={}，权益数量={}",
                businessId, cards.size(), benefits.size());

        Map<Long, List<WxMemberCardJobDTO>> cardsByBusinessId = cards.stream()
                .collect(Collectors.groupingBy(card -> defaultBusinessId(card.getBusinessId())));
        cardsByBusinessId.values().forEach(list ->
                list.sort(Comparator.comparing(WxMemberCardJobDTO::getMinPointsThreshold, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(WxMemberCardJobDTO::getMemberLevel, Comparator.nullsLast(Integer::compareTo))));

        Map<Long, List<WxMemberCardBenefitJobDTO>> benefitsByCardId = benefits.stream()
                .collect(Collectors.groupingBy(WxMemberCardBenefitJobDTO::getMemberCardId));
        Map<Long, CardBenefitContext> benefitContextByCardId = new HashMap<>();
        benefitsByCardId.forEach((cardId, cardBenefits) -> {
            MemberBenefitIssueContext issueContext = buildMemberBenefitIssueContext(businessId, cardBenefits);
            List<WxMemberCardBenefitJobDTO> memberBenefits = cardBenefits.stream()
                    .filter(item -> Objects.equals(item.getBenefitScene(), MEMBER_BENEFIT_SCENE))
                    .toList();
            List<WxMemberCardBenefitJobDTO> birthdayBenefits = cardBenefits.stream()
                    .filter(item -> Objects.equals(item.getBenefitScene(), BIRTHDAY_BENEFIT_SCENE))
                    .toList();
            List<WxMemberCardBenefitJobDTO> upgradeBenefits = cardBenefits.stream()
                    .filter(item -> Objects.equals(item.getBenefitScene(), UPGRADE_BENEFIT_SCENE))
                    .toList();
            List<WxMemberCardBenefitJobDTO> recurringBenefits = memberBenefits.stream()
                    .filter(this::isRecurringBenefitMatchedToday)
                    .toList();
            benefitContextByCardId.put(cardId, new CardBenefitContext(upgradeBenefits, birthdayBenefits, recurringBenefits, issueContext));

            long couponBenefitCount = cardBenefits.stream()
                    .filter(item -> Objects.equals(item.getCouponType(), COUPON_TYPE_COUPON))
                    .count();
            int couponCount = issueContext.couponMap().size();
            log.info("会员卡权益预热明细，businessId={}，会员卡ID={}，权益数={}，单券权益数={}，预热券数={}",
                    businessId, cardId, cardBenefits.size(), couponBenefitCount, couponCount);
        });
        log.info("会员卡权益预热完成，businessId={}，业务分组数={}，会员卡数量={}，已预热权益卡数量={}",
                businessId, cardsByBusinessId.size(), cards.size(), benefitContextByCardId.size());
        return new MemberCardJobConfig(cardsByBusinessId, benefitContextByCardId, memberCardIds);
    }

    private MemberCardBenefitMemberWhitelist snapshotMemberCardBenefitMemberWhitelist() {
        boolean enabled = Boolean.TRUE.equals(memberCardBenefitMemberWhitelistEnabled);
        if (!enabled || !StringUtils.hasText(memberCardBenefitMemberWhitelistIds)) {
            return new MemberCardBenefitMemberWhitelist(enabled, Collections.emptySet());
        }

        Set<Long> memberIds = new LinkedHashSet<>();
        List<String> invalidValues = new ArrayList<>();
        for (String value : memberCardBenefitMemberWhitelistIds.trim().split("[,\\s]+")) {
            try {
                memberIds.add(Long.parseLong(value));
            } catch (NumberFormatException e) {
                invalidValues.add(value);
            }
        }
        if (!invalidValues.isEmpty()) {
            log.warn("会员卡权益每日发放任务会员白名单存在无效会员ID，无效值={}", invalidValues);
        }
        return new MemberCardBenefitMemberWhitelist(true, Set.copyOf(memberIds));
    }

    private boolean isMemberCardBenefitMemberAllowed(WxMemberBenefitJobDTO member,
                                                      MemberCardBenefitMemberWhitelist memberWhitelist) {
        return !memberWhitelist.enabled()
                || member != null && member.getMemberId() != null
                && memberWhitelist.memberIds().contains(member.getMemberId());
    }

    private long processMemberCardBenefitShard(Long businessId, int shard, MemberCardJobConfig config,
                                               MemberCardBenefitMemberWhitelist memberWhitelist) {
        Long lastCursorId = 0L;
        long shardInsertedCount = 0L;
        while (true) {
            List<WxMemberBenefitJobDTO> members;
            try {
                // 每个分片使用 member_id 游标分页，避免 offset 翻页在大表上变慢。
                members = wxMemberApi.listMemberCardBenefitJobMembers(businessId, shard, lastCursorId, MEMBER_CARD_BENEFIT_BATCH_SIZE);
            } catch (Exception e) {
                log.error("会员卡权益每日发放任务查询会员失败，businessId={}，分片={}，游标={}", businessId, shard, lastCursorId, e);
                break;
            }

            if (CollectionUtils.isEmpty(members)) {
                log.info("会员卡权益每日发放任务分片处理完成，businessId={}，分片={}，最后游标={}，写入券数={}",
                        businessId, shard, lastCursorId, shardInsertedCount);
                return shardInsertedCount;
            }

            Long batchStartCursor = lastCursorId;
            Long batchEndCursor = members.stream()
                    .map(WxMemberBenefitJobDTO::getMemberId)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(lastCursorId);
            int matchedMemberCount = 0;
            int pendingCouponCount = 0;

            MemberCardBenefitBatchWriter batchWriter = new MemberCardBenefitBatchWriter(
                    shard, businessId, batchStartCursor, batchEndCursor);
            try {
                long buildStartTime = System.currentTimeMillis();
                for (WxMemberBenefitJobDTO member : members) {
                    if (!isMemberCardBenefitMemberAllowed(member, memberWhitelist)) {
                        continue;
                    }
                    try {
                        MemberBenefitCollectResult result = processMemberCardBenefitMember(businessId, shard, member, config, batchWriter);
                        if (result.matched()) {
                            matchedMemberCount++;
                        }
                        pendingCouponCount += result.pendingCouponCount();
                    } catch (Exception e) {
                        log.error("会员卡权益每日发放任务处理会员失败，businessId={}，分片={}，会员ID={}",
                                businessId, shard, member == null ? null : member.getMemberId(), e);
                    }
                }
                long writeStartTime = System.currentTimeMillis();
                log.info("会员卡权益批次处理开始，businessId={}，分片={}，游标范围={}~{}，批次会员数={}，处理耗时={}ms,需处理条数={}",
                        businessId, shard, batchStartCursor, batchEndCursor, members.size(), System.currentTimeMillis() - buildStartTime,batchWriter.getGeneratedCount());
                MemberCardBenefitWriteResult writeResult = batchWriter.finish();
                long writeCost = System.currentTimeMillis() - writeStartTime;
                shardInsertedCount += writeResult.insertedCount();
                log.info("会员卡权益批次处理完成，businessId={}，分片={}，游标范围={}~{}，批次会员数={}，命中权益会员数={}，待发券总数={}，chunk数={}，批量写库耗时={}ms，写库吞吐={}行/s,已处理券总数={}",
                        businessId, shard, batchStartCursor, batchEndCursor, members.size(), matchedMemberCount,
                        pendingCouponCount, writeResult.chunkCount(), writeCost,
                        calculateRowsPerSecond(writeResult.insertedCount(), writeCost), writeResult.insertedCount());
            } catch (Exception e) {
                batchWriter.shutdownNow();
                log.error("会员卡权益每日发放任务批次处理失败，businessId={}，分片={}，游标范围={}~{}",
                        businessId, shard, batchStartCursor, batchEndCursor, e);
                return shardInsertedCount;
            }

            if (Objects.equals(batchEndCursor, lastCursorId)) {
                log.warn("会员卡权益每日发放任务游标未推进，停止当前分片，businessId={}，分片={}，游标={}", businessId, shard, lastCursorId);
                return shardInsertedCount;
            }
            lastCursorId = batchEndCursor;
        }
        return shardInsertedCount;
    }

    private double calculateRowsPerSecond(long rows, long costMillis) {
        if (rows <= 0 || costMillis <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf(rows)
                .multiply(BigDecimal.valueOf(1000))
                .divide(BigDecimal.valueOf(costMillis), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int getMemberCardBenefitWriteThreadNum() {
        return memberCardBenefitWriteThreadNum == null || memberCardBenefitWriteThreadNum <= 0
                ? MEMBER_CARD_BENEFIT_DEFAULT_WRITE_THREAD_NUM
                : memberCardBenefitWriteThreadNum;
    }

    private int getMemberCardBenefitWriteChunkSize() {
        return memberCardBenefitWriteChunkSize == null || memberCardBenefitWriteChunkSize <= 0
                ? MEMBER_CARD_BENEFIT_DEFAULT_WRITE_CHUNK_SIZE
                : memberCardBenefitWriteChunkSize;
    }

    private MemberBenefitCollectResult processMemberCardBenefitMember(Long businessId, int shard, WxMemberBenefitJobDTO member,
                                                                      MemberCardJobConfig config, MemberCardBenefitBatchWriter batchWriter) {
        if (member == null || member.getMemberId() == null) {
            return new MemberBenefitCollectResult(false, 0);
        }

        // 用会员累计积分命中当前应该所属的会员卡，积分区间由后台配置保证不重叠。
        WxMemberCardJobDTO targetCard = matchMemberCard(config.cardsByBusinessId().get(defaultBusinessId(member.getBusinessId())), member.getIntegralFrozen());
        if (targetCard == null) {
            return new MemberBenefitCollectResult(false, 0);
        }

        CardBenefitContext benefitContext = config.benefitContextByCardId()
                .getOrDefault(targetCard.getMemberCardId(), new CardBenefitContext(
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                        new MemberBenefitIssueContext(Collections.emptyMap())));

        boolean upgradedToday = isUpgrade(member, targetCard);
        int pendingCouponCount = 0;
        if (upgradedToday) {
            // 升级命中后立即更新会员等级，不依赖发券是否成功。
            updateMemberLevelQuietly(businessId, shard, member, targetCard);
            // 升级时只发目标会员卡单独配置的升级权益。
            BenefitIssueCollectResult result = issueBenefitsForMember(
                    shard, member, targetCard, benefitContext.upgradeBenefits(), "upgrade", false,
                    benefitContext.issueContext(), batchWriter);
            pendingCouponCount += result.pendingCouponCount();
        }

        if (isBirthdayTomorrow(member)) {
            // 生日礼和升级互不冲突，当天升级也照常发生日礼。
            BenefitIssueCollectResult result = issueBenefitsForMember(
                    shard, member, targetCard, benefitContext.birthdayBenefits(), "birthday", false,
                    benefitContext.issueContext(), batchWriter);
            pendingCouponCount += result.pendingCouponCount();
        }

        // 周期会员权益和升级权益相互独立；同一天同时命中时两组都发。
        BenefitIssueCollectResult recurringResult = issueBenefitsForMember(
                shard, member, targetCard, benefitContext.recurringBenefits(), "recurring", true,
                benefitContext.issueContext(), batchWriter);
        pendingCouponCount += recurringResult.pendingCouponCount();
        return new MemberBenefitCollectResult(pendingCouponCount > 0, pendingCouponCount);
    }

    private WxMemberCardJobDTO matchMemberCard(List<WxMemberCardJobDTO> cards, Long integralFrozen) {
        if (CollectionUtils.isEmpty(cards) || integralFrozen == null) {
            return null;
        }
        for (WxMemberCardJobDTO card : cards) {
            Integer min = card.getMinPointsThreshold();
            Long max = card.getMaxPointsThreshold();
            if (min == null || max == null) {
                continue;
            }
            if (integralFrozen >= min && integralFrozen <= max) {
                return card;
            }
        }
        return null;
    }

    private boolean isUpgrade(WxMemberBenefitJobDTO member, WxMemberCardJobDTO targetCard) {
        int currentLevel = member.getMemberLevel() == null ? 0 : member.getMemberLevel();
        int targetLevel = targetCard.getMemberLevel() == null ? 0 : targetCard.getMemberLevel();
        return targetLevel > currentLevel;
    }

    private void updateMemberLevelQuietly(Long businessId, int shard, WxMemberBenefitJobDTO member, WxMemberCardJobDTO targetCard) {
        try {
            wxMemberApi.updateMemberLevel(businessId, member.getMemberId(), member.getShardingValue(), targetCard.getMemberLevel());
            log.debug("会员等级更新成功，businessId={}，分片={}，会员ID={}，原等级={}，新等级={}",
                    businessId, shard, member.getMemberId(), member.getMemberLevel(), targetCard.getMemberLevel());
        } catch (Exception e) {
            log.error("会员等级更新失败，businessId={}，分片={}，会员ID={}，原等级={}，新等级={}",
                    businessId, shard, member.getMemberId(), member.getMemberLevel(), targetCard.getMemberLevel(), e);
        }
    }

    private boolean isBirthdayTomorrow(WxMemberBenefitJobDTO member) {
        if (member.getMemberBirthday() == null) {
            return false;
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate birthday = member.getMemberBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int birthdayMonth = birthday.getMonthValue();
        int birthdayDay = birthday.getDayOfMonth();
        // 非闰年没有 2 月 29 日，按 2 月 28 日生日处理，所以任务会在 2 月 27 日发生日礼。
        if (birthdayMonth == 2 && birthdayDay == 29 && !tomorrow.isLeapYear()) {
            birthdayDay = 28;
        }
        return birthdayMonth == tomorrow.getMonthValue() && birthdayDay == tomorrow.getDayOfMonth();
    }

    private boolean isRecurringBenefitMatchedToday(WxMemberCardBenefitJobDTO benefit) {
        if (benefit.getRepeatType() == null || benefit.getIssueValue() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        Integer issueValue = benefit.getIssueValue();

        // 周重复逻辑不变
        if (Objects.equals(benefit.getRepeatType(), REPEAT_TYPE_WEEK)) {
            return today.getDayOfWeek().getValue() == issueValue;
        }

        // 按月重复
        if (Objects.equals(benefit.getRepeatType(), REPEAT_TYPE_MONTH)) {
            // ================== 核心改动开始 ==================
            // 规则1：前端传 29 = 月末 → 当天是当月最后一天就发
            if (issueValue == 29) {
                return today.getDayOfMonth() == today.lengthOfMonth();
            }
            // 规则2：不是29 → 严格匹配日期，是几号就几号发，不做任何适配
            else {
                return today.getDayOfMonth() == issueValue;
            }

        }

        return false;
    }

    private BenefitIssueCollectResult issueBenefitsForMember(int shard, WxMemberBenefitJobDTO member, WxMemberCardJobDTO targetCard,
                                                             List<WxMemberCardBenefitJobDTO> benefits, String triggerType,
                                                             boolean recurringMode, MemberBenefitIssueContext issueContext,
                                                             MemberCardBenefitBatchWriter batchWriter) {
        if (CollectionUtils.isEmpty(benefits)) {
            return new BenefitIssueCollectResult(0);
        }
        List<MemberBenefitCouponGrant> pendingGrants = new ArrayList<>();
        for (WxMemberCardBenefitJobDTO benefit : benefits) {
            try {
                issueSingleBenefit(shard, member, targetCard, benefit, triggerType, recurringMode, pendingGrants);
            } catch (Exception e) {
                log.error("会员卡权益发放失败，分片={}，会员ID={}，会员卡ID={}，触发类型={}，权益ID={}",
                        shard, member.getMemberId(), targetCard.getMemberCardId(), triggerType, benefit.getId(), e);
            }
        }
        int pendingCouponCount = collectPendingBenefitCoupons(shard, member, targetCard, triggerType, recurringMode, issueContext, pendingGrants, batchWriter);
        return new BenefitIssueCollectResult(pendingCouponCount);
    }

    private void issueSingleBenefit(int shard, WxMemberBenefitJobDTO member, WxMemberCardJobDTO targetCard,
                                    WxMemberCardBenefitJobDTO benefit, String triggerType, boolean recurringMode,
                                    List<MemberBenefitCouponGrant> pendingGrants) {
        if (benefit.getCouponId() == null || benefit.getSendNum() == null || benefit.getSendNum() <= 0) {
            log.warn("权益配置无效，跳过发放，分片={}，会员ID={}，权益ID={}，券ID={}，发放数量={}",
                    shard, member.getMemberId(), benefit.getId(), benefit.getCouponId(), benefit.getSendNum());
            return;
        }
        if (Objects.equals(benefit.getCouponType(), COUPON_TYPE_COUPON)) {
            pendingGrants.add(new MemberBenefitCouponGrant(benefit, benefit.getCouponId(), benefit.getSendNum()));
            return;
        }
        log.warn("会员卡权益跳过非优惠券配置，分片={}，会员ID={}，会员卡ID={}，触发类型={}，券类型={}，权益ID={}",
                shard, member.getMemberId(), targetCard.getMemberCardId(), triggerType, benefit.getCouponType(), benefit.getId());
    }


    private int collectPendingBenefitCoupons(int shard, WxMemberBenefitJobDTO member, WxMemberCardJobDTO targetCard,
                                             String triggerType, boolean recurringMode, MemberBenefitIssueContext issueContext,
                                             List<MemberBenefitCouponGrant> pendingGrants, MemberCardBenefitBatchWriter batchWriter) {
        if (CollectionUtils.isEmpty(pendingGrants)) {
            return 0;
        }
        int pendingCouponCount = 0;
        for (MemberBenefitCouponGrant grant : pendingGrants) {
            MemberCardBenefitCouponTemplate couponTemplate = issueContext.couponMap().get(grant.couponId());
            if (couponTemplate == null) {
                log.warn("会员卡权益发券跳过，券不可用，分片={}，会员ID={}，会员卡ID={}，触发类型={}，权益场景={}，券类型={}，券包ID={}，券ID={}，原因=券不存在",
                        shard, member.getMemberId(), targetCard.getMemberCardId(), triggerType,
                        grant.benefit().getBenefitScene(), grant.benefit().getCouponType(), null, grant.couponId());
                continue;
            }

            // 只有目标会员卡等级为 1，当前批次写库成功后才需要把 couponId 记录到归档 Redis Set。
            boolean levelOne = Objects.equals(targetCard.getMemberLevel(), 1);
            for (int i = 0; i < grant.sendNum(); i++) {
                // 先生成待写入的用户券，并把“是否一级会员券”信息交给批量写入器随 chunk 一起保存。
                batchWriter.add(buildMemberCardBenefitUserCoupon(
                        member, couponTemplate, CouponSourceType.AUTOMATIC_DISTRIBUTION.getCode(), null), levelOne);
            }
            pendingCouponCount += grant.sendNum();
        }
        return pendingCouponCount;
    }

    private UserCouponDO buildMemberCardBenefitUserCoupon(WxMemberBenefitJobDTO member, MemberCardBenefitCouponTemplate couponTemplate,
                                                          Integer couponSource, Long storeId) {
        UserCouponDO fixedCoupon = couponTemplate.fixedCoupon();
        UserCouponDO userCoupon = new UserCouponDO();
        copyMemberCardBenefitFixedCouponFields(fixedCoupon, userCoupon);
        userCoupon.setUserId(member.getMemberId());
        userCoupon.setId(null);
        userCoupon.setMemberMobile(member.getMemberMobile());
        userCoupon.setMemberName(member.getMemberNickName());
        userCoupon.setCouponSource(couponSource);
        userCoupon.setStoreId(storeId);
        return userCoupon;
    }

    private MemberBenefitIssueContext buildMemberBenefitIssueContext(Long businessId, List<WxMemberCardBenefitJobDTO> benefits) {
        Set<Long> couponIds = benefits.stream()
                .filter(item -> Objects.equals(item.getCouponType(), COUPON_TYPE_COUPON))
                .map(WxMemberCardBenefitJobDTO::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return new MemberBenefitIssueContext(queryMemberCardBenefitCoupons(businessId, couponIds));
    }

    private Map<Long, MemberCardBenefitCouponTemplate> queryMemberCardBenefitCoupons(Long businessId, Set<Long> couponIds) {
        if (CollectionUtils.isEmpty(couponIds)) {
            return Collections.emptyMap();
        }
        List<GoodCouponDO> couponDOS = DataPermissionUtils.executeIgnore(() ->
                goodCouponMapper.selectList(new LambdaQueryWrapper<GoodCouponDO>()
                        .select(GoodCouponDO::getId,
                                GoodCouponDO::getCouponCode,
                                GoodCouponDO::getCouponName,
                                GoodCouponDO::getCouponType,
                                GoodCouponDO::getCouponNum,
                                GoodCouponDO::getLimitNum,
                                GoodCouponDO::getExchangeFlag,
                                GoodCouponDO::getCouponStartTime,
                                GoodCouponDO::getCouponEndTime,
                                GoodCouponDO::getReceivedNum,
                                GoodCouponDO::getUsedNum,
                                GoodCouponDO::getSingleIds,
                                GoodCouponDO::getFullReduction,
                                GoodCouponDO::getReduceAmount,
                                GoodCouponDO::getDiscount,
                                GoodCouponDO::getIsGround,
                                GoodCouponDO::getDistributionMethod,
                                GoodCouponDO::getCouponExplain,
                                GoodCouponDO::getUseRules,
                                GoodCouponDO::getCouponImageUrl,
                                GoodCouponDO::getUseType,
                                GoodCouponDO::getUseTime,
                                GoodCouponDO::getRemark,
                                GoodCouponDO::getIsCommon,
                                GoodCouponDO::getCommunityQrImage,
                                GoodCouponDO::getUserRestrictions,
                                GoodCouponDO::getIsShare,
                                GoodCouponDO::getDoorsillType,
                                GoodCouponDO::getDoorsill,
                                GoodCouponDO::getIsCommonStore,
                                GoodCouponDO::getReliefOrDiscount,
                                GoodCouponDO::getPayAmount,
                                GoodCouponDO::getShowTime,
                                GoodCouponDO::getVersion,
                                GoodCouponDO::getTotalNum,
                                GoodCouponDO::getMemberLevel,
                                GoodCouponDO::getDayLimit,
                                GoodCouponDO::getCouponNumVisible,
                                GoodCouponDO::getHabit,
                                GoodCouponDO::getDayNumbers,
                                GoodCouponDO::getWeekNumbers,
                                GoodCouponDO::getTimeRange,
                                GoodCouponDO::getIsAllDay,
                                GoodCouponDO::getCouponBgImageUrl,
                                GoodCouponDO::getMiniSortUrl,
                                GoodCouponDO::getH5SortUrl,
                                GoodCouponDO::getClaimTimeLimit,
                                GoodCouponDO::getClaimTimeSlot,
                                GoodCouponDO::getClaimDayNo,
                                GoodCouponDO::getClaimWeekNo,
                                GoodCouponDO::getClaimTime,
                                GoodCouponDO::getStoreLimitNum,
                                GoodCouponDO::getTestAfterDate,
                                GoodCouponDO::getCommunityFlag,
                                GoodCouponDO::getBusinessId,
                                GoodCouponDO::getCreateTime)
                        .eq(GoodCouponDO::getBusinessId, businessId)
                        .in(GoodCouponDO::getId, couponIds)));
        if (CollectionUtils.isEmpty(couponDOS)) {
            return Collections.emptyMap();
        }
        Map<Long, MemberCardBenefitCouponTemplate> couponMap = new HashMap<>(couponDOS.size());
        for (GoodCouponDO couponDO : couponDOS) {
            GoodCouponRespVO respVO = new GoodCouponRespVO();
            BeanUtils.copyProperties(couponDO, respVO);
            if (couponDO.getCouponType() != null) {
                respVO.setCouponType(String.valueOf(couponDO.getCouponType()));
            }
            couponMap.put(couponDO.getId(), buildMemberCardBenefitCouponTemplate(respVO));
        }
        return couponMap;
    }

    private MemberCardBenefitCouponTemplate buildMemberCardBenefitCouponTemplate(GoodCouponRespVO goodCoupon) {
        UserCouponDO fixedCoupon = new UserCouponDO();
        BeanUtils.copyProperties(goodCoupon, fixedCoupon);
        GoodCouponRespVO couponSnapshot = new GoodCouponRespVO();
        BeanUtils.copyProperties(goodCoupon, couponSnapshot);
        fixedCoupon.setUserId(null);
        fixedCoupon.setCouponId(goodCoupon.getId());
        fixedCoupon.setCouponType(Integer.valueOf(goodCoupon.getCouponType()));
        fixedCoupon.setId(null);
        fixedCoupon.setIsUsed(0);
        fixedCoupon.setUseTime(null);
        CouponTimeUtil.parseCouponTime(couponSnapshot, fixedCoupon);
        fixedCoupon.setDistributionMethod(0L);
        fixedCoupon.setMemberMobile(null);
        fixedCoupon.setMemberName(null);
        fixedCoupon.setCouponSource(CouponSourceType.AUTOMATIC_DISTRIBUTION.getCode());
        fixedCoupon.setStoreId(null);
        fixedCoupon.setDeleted(false);
        fixedCoupon.setCouponUseTime(goodCoupon.getUseTime());
        fixedCoupon.setCreateTime(goodCoupon.getCreateTime());
        fixedCoupon.setBusinessId(goodCoupon.getBusinessId());
        return new MemberCardBenefitCouponTemplate(fixedCoupon);
    }

    private void copyMemberCardBenefitFixedCouponFields(UserCouponDO source, UserCouponDO target) {
        target.setCouponCode(source.getCouponCode());
        target.setCouponName(source.getCouponName());
        target.setCouponType(source.getCouponType());
        target.setCouponNum(source.getCouponNum());
        target.setExchangeFlag(source.getExchangeFlag());
        target.setLimitNum(source.getLimitNum());
        target.setCouponStartTime(source.getCouponStartTime());
        target.setCouponEndTime(source.getCouponEndTime());
        target.setReceivedNum(source.getReceivedNum());
        target.setUsedNum(source.getUsedNum());
        target.setSingleIds(source.getSingleIds());
        target.setFullReduction(source.getFullReduction());
        target.setReduceAmount(source.getReduceAmount());
        target.setDiscount(source.getDiscount());
        target.setCouponStatus(source.getCouponStatus());
        target.setIsGround(source.getIsGround());
        target.setDistributionMethod(source.getDistributionMethod());
        target.setCouponExplain(source.getCouponExplain());
        target.setUseRules(source.getUseRules());
        target.setCouponImageUrl(source.getCouponImageUrl());
        target.setDuration(source.getDuration());
        target.setCouponCreateTime(source.getCouponCreateTime());
        target.setInstructions(source.getInstructions());
        target.setUseType(source.getUseType());
        target.setCouponUseTime(source.getCouponUseTime());
        target.setRemark(source.getRemark());
        target.setIsCommon(source.getIsCommon());
        target.setDeptIds(source.getDeptIds());
        target.setUserRestrictions(source.getUserRestrictions());
        target.setIsShare(source.getIsShare());
        target.setDoorsillType(source.getDoorsillType());
        target.setDoorsill(source.getDoorsill());
        target.setIsCommonStore(source.getIsCommonStore());
        target.setReliefOrDiscount(source.getReliefOrDiscount());
        target.setPayAmount(source.getPayAmount());
        target.setShowTime(source.getShowTime());
        target.setVersion(source.getVersion());
        target.setTotalNum(source.getTotalNum());
        target.setIsUsed(source.getIsUsed());
        target.setExpirationTime(source.getExpirationTime());
        target.setVaildStartTime(source.getVaildStartTime());
        target.setUseTime(source.getUseTime());
        target.setIsNew(source.getIsNew());
        target.setCouponId(source.getCouponId());
        target.setPackageId(source.getPackageId());
        target.setCouponNumVisible(source.getCouponNumVisible());
        target.setHabit(source.getHabit());
        target.setDeleted(source.getDeleted());
        target.setCreateTime(source.getCreateTime());
        target.setBusinessId(source.getBusinessId());
    }

    private Long defaultBusinessId(Long businessId) {
        return businessId == null ? 0L : businessId;
    }

    private class MemberCardBenefitBatchWriter {

        private final int shard;
        private final Long businessId;
        private final Long batchStartCursor;
        private final Long batchEndCursor;
        private final int chunkSize;
        private final ThreadPoolExecutor writeExecutor;
        private final List<MemberCardBenefitWriteTask> writeTasks = new ArrayList<>();

        // 当前 2000 会员批次内已经成功写入 Redis 的券 ID，用于减少重复 SADD 调用。
        private final Set<Long> recordedLevelOneCouponIds = new HashSet<>();

        // 当前正在组装的数据库写入 chunk。
        private List<UserCouponDO> currentChunk;

        // 当前 chunk 中属于一级会员的券 ID；chunk 写库成功后才允许写入 Redis。
        private Set<Long> currentChunkLevelOneCouponIds = new HashSet<>();
        private int generatedCount;
        private int chunkIndex;

        private MemberCardBenefitBatchWriter(int shard, Long businessId, Long batchStartCursor, Long batchEndCursor) {
            this.shard = shard;
            this.businessId = businessId;
            this.batchStartCursor = batchStartCursor;
            this.batchEndCursor = batchEndCursor;
            this.chunkSize = getMemberCardBenefitWriteChunkSize();
            int writeThreadNum = getMemberCardBenefitWriteThreadNum();
            this.currentChunk = new ArrayList<>(chunkSize);
            // 当前线程池只服务“当前 shard 当前 2000 会员批次”的 user_coupon 写库。
            // corePoolSize == maximumPoolSize：固定写库并发数，避免批次内线程数抖动。
            // keepAliveTime 对固定线程池基本不生效，保留是 ThreadPoolExecutor 构造参数要求。
            // LinkedBlockingQueue 限制待写 chunk 堆积量；队列满时 CallerRunsPolicy 让计算线程自己写，形成反压。
            this.writeExecutor = new ThreadPoolExecutor(
                    writeThreadNum,
                    writeThreadNum,
                    MEMBER_CARD_BENEFIT_WRITE_KEEP_ALIVE_SECONDS,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(MEMBER_CARD_BENEFIT_WRITE_QUEUE_CAPACITY),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }

        /**
         * 将已经成功写入用户券分表的一级会员 couponId 记录到 Redis。
         *
         * <p>Redis 异常只记录日志，不回滚已经发放成功的用户券；
         * 写入失败时从本地去重集合移除，让后续包含同一券 ID 的成功 chunk 可以再次尝试。</p>
         */
        private void recordLevelOneCouponIds(Set<Long> couponIds) {
            for (Long couponId : couponIds) {
                // 第一步：过滤空值，并在当前会员批次内对 couponId 去重。
                if (couponId == null || !recordedLevelOneCouponIds.add(couponId)) {
                    continue;
                }
                try {
                    // 第二步：使用 Redis Set 全局去重，Key 按 businessId 隔离。
                    memberCardBenefitRedisDAO.addLevelOneCouponId(businessId, couponId);
                } catch (Exception e) {
                    // 第三步：Redis 暂时不可用时保留发券结果，并允许后续 chunk 重试记录。
                    recordedLevelOneCouponIds.remove(couponId);
                    log.error("一级会员卡权益券ID写入Redis失败，businessId={}，分片={}，券ID={}",
                            businessId, shard, couponId, e);
                }
            }
        }

        /**
         * 将一张待发用户券加入当前写库 chunk。
         */
        private void add(UserCouponDO userCoupon, boolean levelOne) {
            // 第一步：加入当前数据库批量写入集合。
            currentChunk.add(userCoupon);

            // 第二步：一级会员券只在当前 chunk 内保存 couponId，不在此时提前写 Redis。
            if (levelOne && userCoupon.getCouponId() != null) {
                currentChunkLevelOneCouponIds.add(userCoupon.getCouponId());
            }

            // 第三步：累计当前 2000 会员批次生成的用户券数量。
            generatedCount++;

            // 第四步：达到配置的 chunk 上限后，提交异步写库任务并创建新的 chunk。
            if (currentChunk.size() >= chunkSize) {
                flushCurrentChunk();
            }
        }

        private int getGeneratedCount() {
            return generatedCount;
        }

        /**
         * 提交最后一个未满 chunk，并等待当前会员批次的全部写库任务完成。
         */
        private MemberCardBenefitWriteResult finish() throws Exception {
            // 第一步：把不足 chunkSize 的尾批数据也提交到写库线程池。
            flushCurrentChunk();
            int totalInserted = 0;
            try {
                // 第二步：按提交顺序等待每个 chunk 完成，统一检查实际写入数量。
                for (MemberCardBenefitWriteTask writeTask : writeTasks) {
                    try {
                        Integer result = writeTask.future().get();
                        int insertedCount = result == null ? 0 : result;

                        // 第三步：批量 INSERT 必须完整成功；数量不一致时终止当前会员分片处理。
                        if (insertedCount != writeTask.chunkSize()) {
                            throw new IllegalStateException(String.format(
                                    "会员卡权益批次分块写入数量不一致，chunkIndex=%d, expected=%d, inserted=%d",
                                    writeTask.chunkIndex(), writeTask.chunkSize(), insertedCount));
                        }
                        totalInserted += insertedCount;

                        // 第四步：只有确认该 chunk 全部写库成功，才把其中的一级会员券 ID 写入 Redis。
                        recordLevelOneCouponIds(writeTask.levelOneCouponIds());
                    } catch (Exception e) {
                        log.error("会员卡权益批次分块写库失败，businessId={}，分片={}，游标范围={}~{}，chunkIndex={}，chunkSize={}",
                                businessId, shard, batchStartCursor, batchEndCursor, writeTask.chunkIndex(),
                                writeTask.chunkSize(), e);
                        throw e;
                    }
                }
                return new MemberCardBenefitWriteResult(generatedCount, totalInserted, writeTasks.size());
            } finally {
                // 第五步：无论成功或异常都关闭当前批次专用线程池，避免线程资源泄漏。
                writeExecutor.shutdown();
            }
        }

        /**
         * 将当前 chunk 封装成异步写库任务。
         */
        private void flushCurrentChunk() {
            if (CollectionUtils.isEmpty(currentChunk)) {
                return;
            }

            // 第一步：冻结当前 chunk 的用户券和一级会员券 ID，避免后续继续追加时修改已提交任务的数据。
            int currentChunkIndex = chunkIndex++;
            List<UserCouponDO> chunkCoupons = currentChunk;
            Set<Long> chunkLevelOneCouponIds = Set.copyOf(currentChunkLevelOneCouponIds);

            // 第二步：立即创建下一批容器，让主线程可以继续生成后续用户券。
            currentChunk = new ArrayList<>(chunkSize);
            currentChunkLevelOneCouponIds = new HashSet<>();

            // 第三步：提交异步写库任务；CallerRunsPolicy 会在队列满时让生成线程执行，形成反压。
            Future<Integer> future = writeExecutor.submit(() -> {
                long chunkStartTime = System.currentTimeMillis();

                // 第四步：在真正写库前写入发放时间，168 小时归档期限从该批实际落库时间开始计算。
                LocalDateTime issuedAt = LocalDateTime.now();
                chunkCoupons.forEach(coupon -> coupon.setCouponCreateTime(issuedAt));

                // 第五步：将当前 chunk 批量写入对应的 user_coupon_N 主库分表。
                Integer insertedCount = userCouponService.insertMemberCardBenefitCouponsShardChunk(shard, chunkCoupons);
                long chunkCost = System.currentTimeMillis() - chunkStartTime;
                log.info("会员卡权益批次分块写库完成，businessId={}，分片={}，游标范围={}~{}，chunkIndex={}，chunkSize={}，写库耗时={}ms，吞吐={}行/s，已处理券总数={}",
                        businessId, shard, batchStartCursor, batchEndCursor, currentChunkIndex, chunkCoupons.size(),
                        chunkCost, calculateRowsPerSecond(insertedCount == null ? 0 : insertedCount, chunkCost), insertedCount);
                return insertedCount;
            });

            // 第六步：保存 Future 和该 chunk 的一级会员券 ID，finish() 中统一校验并记录 Redis。
            writeTasks.add(new MemberCardBenefitWriteTask(
                    currentChunkIndex, chunkCoupons.size(), chunkLevelOneCouponIds, future));
        }

        /**
         * 当前会员批次异常时立即停止尚未执行的写库任务。
         */
        private void shutdownNow() {
            writeExecutor.shutdownNow();
        }
    }

    private record MemberCardJobConfig(
            Map<Long, List<WxMemberCardJobDTO>> cardsByBusinessId,
            Map<Long, CardBenefitContext> benefitContextByCardId,
            List<Long> cardIds
    ) {
    }

    private record MemberCardBenefitMemberWhitelist(
            boolean enabled,
            Set<Long> memberIds
    ) {
    }

    private record CardBenefitContext(
            List<WxMemberCardBenefitJobDTO> upgradeBenefits,
            List<WxMemberCardBenefitJobDTO> birthdayBenefits,
            List<WxMemberCardBenefitJobDTO> recurringBenefits,
            MemberBenefitIssueContext issueContext
    ) {
    }

    private record MemberBenefitIssueContext(
            Map<Long, MemberCardBenefitCouponTemplate> couponMap
    ) {
    }

    private record MemberCardBenefitCouponTemplate(UserCouponDO fixedCoupon) {
    }

    private record MemberBenefitCouponGrant(
            WxMemberCardBenefitJobDTO benefit,
            Long couponId,
            Integer sendNum
    ) {
    }

    private record BenefitIssueCollectResult(
            int pendingCouponCount
    ) {
    }

    private record MemberBenefitCollectResult(
            boolean matched,
            int pendingCouponCount
    ) {
    }

    private record MemberCardBenefitWriteTask(
            int chunkIndex,
            int chunkSize,
            Set<Long> levelOneCouponIds,
            Future<Integer> future
    ) {
    }

    private record MemberCardBenefitWriteResult(
            int generatedCount,
            int insertedCount,
            int chunkCount
    ) {
    }

    // 初始化：会员等级 ↔ 优惠券ID 映射（LV1-LV5对应指定券ID）
    private static final Map<Integer, List<Long>> LEVEL_COUPON_MAP = new HashMap<>();
    static {
        // LV1-LV4：单券
        LEVEL_COUPON_MAP.put(2, Collections.singletonList(2036317720035745793L));
        LEVEL_COUPON_MAP.put(3, Arrays.asList(2036318891420966914l,2036317720035745793L));
        LEVEL_COUPON_MAP.put(4, Arrays.asList(2036317720035745793L,2036318891420966914L,1996871013686685698L));
        // LV5：双券（两个优惠券ID）
        LEVEL_COUPON_MAP.put(5, Arrays.asList(2036317720035745793L,2036318891420966914L,1996871013686685698L,1996871290141646850L));
    }

}
