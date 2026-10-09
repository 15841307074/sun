package com.htyoudao.youdao.module.member.service.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TtlRunnable;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wecom.WecomGroupMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.enums.EventType;
import com.htyoudao.youdao.module.member.service.crowd.CustomCrowdService;
import com.htyoudao.youdao.module.member.service.crowd.WxMemberCrowdRefService;
import com.htyoudao.youdao.module.member.service.pointsLog.IPointsLogService;
import com.htyoudao.youdao.module.member.service.wxmember.IEventService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.util.DateUtils;
import com.htyoudao.youdao.module.member.util.StringUtils;
import com.htyoudao.youdao.module.member.util.redis.RedisCache;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.xxl.job.core.context.XxlJobHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.elasticsearch.client.ResponseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class JobServiceImpl implements JobService{

    // 1. 定义全局业务时区（东八区），避免跨环境偏差
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private static final Integer OUT_COMMUNITY_FLAG = 1;

    private static final Integer IN_COMMUNITY_FLAG = 2;

    private static final DateTimeFormatter ES_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final int MEMBER_HISTORY_METRICS_ES_PAGE_SIZE = 1000;

    private static final int MEMBER_HISTORY_METRICS_ES_MAX_RETRY = 5;

    private static final long MEMBER_HISTORY_METRICS_ES_RETRY_BASE_SLEEP_MS = 1000L;

    private static final long MEMBER_HISTORY_METRICS_ES_PAGE_SLEEP_MS = 100L;

    private static final int MEMBER_HISTORY_METRICS_REBUILD_THREAD_COUNT = 6;

    private static final long MEMBER_HISTORY_METRICS_PROGRESS_STEP = 5000L;


    @Resource
    private PointsLogMapper pointsLogMapper;
    @Resource
    private WxMemberCardMapper wxMemberCardMapper;
    @Resource
    private WxMemberMapper wxMemberMapper;

    @Resource
    private IPointsLogService pointsLogService;

    @Resource
    private WxMemberService wxMemberService;

    @Resource
    private WxMemberCrowdRefService wxMemberCrowdRefService;

    @Resource
    private CustomCrowdService customCrowdService;

    @Resource
    private IEventService eventService;

    @Resource
    private ElasticsearchClient esClient;

    @Resource
    private RedisCache redisCache;

    @DubboReference
    private BzOrderApi bzOrderApi;

    @DubboReference
    private GoodCouponApi goodCouponApi;

    @Resource
    private WecomGroupMemberMapper wecomGroupMemberMapper;

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    @DataPermission(enable = false)
    public void updateMemberPointTask(int start, int end, long memberId, long businessId) {
        String busId = null;
        if(businessId != 0){
            busId = String.valueOf(businessId);
        }else{
            busId = XxlJobHelper.getJobParam();
        }

        if(StringUtils.isBlank(busId)){
            log.error("释放冻结积分定时任务，获取项目失败");
            return;
        }

        int unit = 24;
        start = Math.max(start, 2);
        int minusHours1 = unit * start;
        int minusHours2 = unit * end;
        // 使用分页查询，每次处理一部分数据
        int pageSize = 10000;
        int pageNum = 1;
        boolean hasMore = true;
        // 查询用户冻结积分
        QueryWrapperX<PointsLogDO> baseLqw = new QueryWrapperX<>();
        LambdaQueryWrapper<PointsLogDO> baseExpiredPointsQuery = new LambdaQueryWrapper<>();
        baseLqw.select("member_id", "SUM(points_change) as points_change");
        if(memberId != 0){
            baseLqw.eq("member_id", memberId);
            baseExpiredPointsQuery.eq(PointsLogDO::getMemberId, memberId);
        }
        baseLqw.eq("points_type", 4)
                .le("create_time", LocalDateTime.now().minusHours(minusHours1 == 0 ? 48 : minusHours1))
                .ge("create_time", LocalDateTime.now().minusHours(minusHours2 == 0 ? 72 : minusHours2))
                .eq("business_id", busId)
                .groupBy("member_id");
        baseExpiredPointsQuery.eq(PointsLogDO::getPointsType, 4)
                .le(PointsLogDO::getCreateTime, LocalDateTime.now().minusHours(minusHours1 == 0 ? 48 : minusHours1))
                .ge(PointsLogDO::getCreateTime, LocalDateTime.now().minusHours(minusHours2 == 0 ? 72 : minusHours2))
                .eq(PointsLogDO::getBusinessId, busId);
        while(hasMore){
            QueryWrapper<PointsLogDO> lqw = baseLqw.clone();
            lqw.last("limit " + (pageNum - 1) * pageSize + "," + pageSize);
            log.info("lqw where条件语句：{}", lqw.getSqlSegment());
            List<PointsLogDO> pointsLogDOS = pointsLogMapper.selectList(lqw);
            Map<Long, Long> memberPointsMap = pointsLogDOS.stream()
                    .collect(Collectors.toMap(PointsLogDO::getMemberId, PointsLogDO::getPointsChange));
            //  需清理的积分
            if (memberPointsMap.isEmpty()) {
                hasMore = false;
                continue;
            }
            List<WxMemberDO> memberList = memberPointsMap.entrySet().stream()
                    .filter(entry -> memberPointsMap.containsKey(entry.getKey()))
                    .map(entry -> {
                        WxMemberDO member = new WxMemberDO();
                        member.setMemberId(entry.getKey());
                        member.setMemberIntegral(entry.getValue().intValue());
                        return member;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            log.info(">>> 开始解冻积分，解冻积分={}", memberList);
            updateWxMemberPointsBatch(memberList, busId);
            // 页码增加
            pageNum++;
        }
        // 批量更新积分记录
        // 查询过期积分记录
        pageNum = 1;
        hasMore = true;
        while(hasMore){
            LambdaQueryWrapper<PointsLogDO> expiredPointsQuery = baseExpiredPointsQuery.clone();
            expiredPointsQuery.last("limit " + (pageNum - 1) * pageSize + "," + pageSize);
            log.info("expiredPointsQuery where条件语句：{}", expiredPointsQuery.getSqlSegment());
            List<PointsLogDO> pointsLogs = pointsLogMapper.selectList(expiredPointsQuery);
            pointsLogs.forEach(pointsLog -> pointsLog.setPointsType(1));
            log.info(">>> 开始更新积分记录，解冻积分={}", pointsLogs);
            if (pointsLogs.isEmpty()) {
                hasMore = false;
                continue;
            }
            // 更新积分记录
            for (PointsLogDO pointsLog : pointsLogs) {
                LambdaQueryWrapper<PointsLogDO> qw = new LambdaQueryWrapper<>();
                qw.eq(PointsLogDO::getPointsLogId, pointsLog.getPointsLogId());
                qw.eq(PointsLogDO::getShardingValue, pointsLog.getMemberId() % 10);
                pointsLogMapper.update(pointsLog, qw);
            }
            pageNum ++;
        }



    }

    public void updateWxMemberPointsBatch(List<WxMemberDO> batch, String businessId) {
        // 缓存会员卡等级信息
        Map<Long, WxMemberCardDO> memberCardCache = new HashMap<>();
        for (WxMemberDO wxMember : batch) {
            Long memberIntegral = wxMember.getMemberIntegral().longValue();
            if (!memberCardCache.containsKey(memberIntegral)) {
                LambdaQueryWrapper<WxMemberCardDO> qw = new LambdaQueryWrapper<>();
                qw.gt(WxMemberCardDO::getMaxPointsThreshold, memberIntegral);
                qw.lt(WxMemberCardDO::getMinPointsThreshold, memberIntegral);
                qw.eq(WxMemberCardDO::getCardStatus, 1);
                qw.eq(WxMemberCardDO::getBusinessId, businessId);
                WxMemberCardDO wxMemberCard = wxMemberCardMapper.selectOne(qw);
                memberCardCache.put(memberIntegral, wxMemberCard);
            }
        }

        // 处理每个会员
        for (WxMemberDO wxMember : batch) {
//            LambdaQueryWrapper<WxMemberDO> wxQw = new LambdaQueryWrapper<>();
//            wxQw.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
//            wxQw.eq(WxMemberDO::getShardingValue, wxMember.getMemberId() % 10);
//            WxMemberDO existingMember = wxMemberMapper.selectById(wxMember.getMemberId());
            WxMemberDO existingMember =wxMemberService.selectByIdAndBusinessId(wxMember.getMemberId(), businessId);
            if (existingMember != null && existingMember.getFreezePoints() > 0 && existingMember.getFreezePoints() - wxMember.getMemberIntegral() >= 0&&existingMember.getMemberIntegral() + wxMember.getMemberIntegral()>0) {
                // 用户对象的可用积分
                existingMember.setMemberIntegral(existingMember.getMemberIntegral() + wxMember.getMemberIntegral());
                // 用户对象的总积分
                existingMember.setIntegralFrozen(existingMember.getIntegralFrozen() + wxMember.getMemberIntegral());
                // 冻结积分更新
                existingMember.setFreezePoints(existingMember.getFreezePoints() - wxMember.getMemberIntegral());

                // 更新会员卡等级
                WxMemberCardDO wxMemberCard = memberCardCache.get(wxMember.getMemberIntegral());
                if (wxMemberCard != null) {
                    existingMember.setMemberLevel(wxMemberCard.getMemberLevel());
                }

                // 更新用户积分
//                wxMemberMapper.update(existingMember, wxQw);
                wxMemberService.updateMemberByJustIdAndBusinessId(existingMember.getMemberId(), businessId, existingMember);
//                wxMemberMapper.updateById(existingMember);
            } else {
                log.warn(">>> 未找到memberId={}的用户", wxMember.getMemberId());
            }
        }


    }

    @Override
    public void clearExpiredPoints(Long businessId) {
        pointsLogService.clearExpiredPoints(businessId);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public void updateHistoryOrderData() {

        Map<Long, BzOrderDTO> oneYearMap = toOrderMetricMap(
                bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(366), DateUtils.getDaysBeforeEnd(366))
        );
        Map<Long, BzOrderDTO> halfYearMap = toOrderMetricMap(
                bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(181), DateUtils.getDaysBeforeEnd(181))
        );
        Map<Long, BzOrderDTO> thirtyDaysMap = toOrderMetricMap(
                bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(31), DateUtils.getDaysBeforeEnd(31))
        );
        Map<Long, BzOrderDTO> sevenDaysMap = toOrderMetricMap(
                bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(8), DateUtils.getDaysBeforeEnd(8))
        );
        Map<Long, BzOrderDTO> yesterdayMap = toOrderMetricMap(
                bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(1), DateUtils.getDaysBeforeEnd(1))
        );

        Set<Long> memberIds = new HashSet<>(
                oneYearMap.size() + halfYearMap.size() + thirtyDaysMap.size() + sevenDaysMap.size() + yesterdayMap.size()
        );
        memberIds.addAll(oneYearMap.keySet());
        memberIds.addAll(halfYearMap.keySet());
        memberIds.addAll(thirtyDaysMap.keySet());
        memberIds.addAll(sevenDaysMap.keySet());
        memberIds.addAll(yesterdayMap.keySet());

        if (memberIds.isEmpty()) {
            log.info("updateHistoryOrderData 无需处理，本次没有命中的会员数据");
            return;
        }

        final int batchSize = 1000;
        List<Long> batch = new ArrayList<>(batchSize);

        for (Long memberId : memberIds) {
            batch.add(memberId);
            if (batch.size() >= batchSize) {
                processHistoryOrderBatch(batch, oneYearMap, halfYearMap, thirtyDaysMap, sevenDaysMap, yesterdayMap);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            processHistoryOrderBatch(batch, oneYearMap, halfYearMap, thirtyDaysMap, sevenDaysMap, yesterdayMap);
        }
    }

    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public void rebuildHistoryOrderMetricsByEs(Long businessId, boolean resetTotalOrderNum) {
        // 第 1 步：先清空当前业务下会员的历史订单统计字段。
        // 这样做是为了避免数据库中残留旧版本计算结果；后续 ES 聚合会重新写入正确值。
        // 注意：此方法没有总事务，如果后续 ES 查询失败，已经清零的数据不会自动回滚。
        clearMemberHistoryOrderMetrics(businessId, resetTotalOrderNum);

        // 第 2 步：单独查询符合条件的会员数量，仅用于日志展示总量和计算进度百分比。
        // 这是 cardinality 估算值，不参与实际分页和数据更新，因此 totalMemberCount 可能存在少量误差。
        long totalMemberCount = estimateMemberHistoryOrderMetricsTotalFromEs(businessId, resetTotalOrderNum);
        AtomicLong successCounter = new AtomicLong(0);
        AtomicLong failCounter = new AtomicLong(0);
        long processedCounter = 0L;
        long nextLogThreshold = MEMBER_HISTORY_METRICS_PROGRESS_STEP;
        long startTime = System.currentTimeMillis();

        // resetTotalOrderNum=false：只重建 7 天、30 天、半年、一年的订单统计，外层 ES 查询只扫描一年内订单。
        // resetTotalOrderNum=true：在上述统计之外重建 total_order_num，外层 ES 查询需要扫描全部历史订单，成本更高。
        log.info("会员历史订单重建开始，businessId={}, resetTotalOrderNum={}, total≈{}",
                businessId, resetTotalOrderNum, totalMemberCount);
        XxlJobHelper.log("会员历史订单重建开始，businessId={}, resetTotalOrderNum={}, total≈{}",
                businessId, resetTotalOrderNum, totalMemberCount);

        // 第 3 步：创建固定大小线程池。
        // ES 查询仍由当前线程串行分页执行；只有每一页中的会员数据库更新使用线程池并发执行。
        // 每页任务都会等待完成后才继续下一页，避免一次性把所有会员任务放入内存。
        ExecutorService executor = Executors.newFixedThreadPool(MEMBER_HISTORY_METRICS_REBUILD_THREAD_COUNT);

        try {
            // composite 聚合使用 after_key 分页，不能使用传统 from/size。
            // 第一页 afterKey=null；后续循环使用上一页返回的 after_key 继续查询。
            Map<String, FieldValue> afterKey = null;

            while (true) {
                SearchResponse<Void> response;
                try {
                    // 第 4 步：从 ES 查询一页会员聚合结果。
                    // 每个 bucket 对应一个会员，bucket 内包含各时间窗口的订单数、订单金额及可选的总订单数。
                    response = searchMemberOrderMetricsFromEsWithRetry(businessId, resetTotalOrderNum, afterKey);
                } catch (IOException e) {
                    // 非 429 异常，或 429 重试次数耗尽后，会终止整个重建流程。
                    throw new RuntimeException("基于ES重建会员历史订单统计失败", e);
                }

                // ES 没有返回聚合结果时，认为没有更多数据可处理。
                if (response == null || response.aggregations() == null) {
                    break;
                }

                Aggregate usersAggregate = response.aggregations().get("users");
                if (usersAggregate == null || usersAggregate.composite() == null) {
                    break;
                }

                CompositeAggregate composite = usersAggregate.composite();
                List<CompositeBucket> buckets = composite.buckets().array();
                // 当前页没有会员 bucket，说明已经到达分页末尾。
                if (CollectionUtil.isEmpty(buckets)) {
                    break;
                }

                LocalDateTime now = LocalDateTime.now();

                // 第 5 步：并发更新当前页的会员数据。
                // 每个会员只更新自己的 wx_member 记录，会员之间没有业务依赖，因此可以并发执行。
                // processHistoryOrderMetricsPage 会等待当前页所有更新任务结束后才返回。
                processHistoryOrderMetricsPage(businessId, buckets, resetTotalOrderNum, now, executor, successCounter, failCounter);
                processedCounter += buckets.size();

                // 第 6 步：每处理 MEMBER_HISTORY_METRICS_PROGRESS_STEP 条会员记录，输出一次进度。
                nextLogThreshold = logMemberHistoryRebuildProgress(
                        businessId,
                        totalMemberCount,
                        processedCounter,
                        nextLogThreshold,
                        successCounter.get(),
                        failCounter.get()
                );

                // 第 7 步：保存本页 after_key，作为下一次 ES composite 聚合的起点。
                afterKey = composite.afterKey();
                if (afterKey == null || afterKey.isEmpty()) {
                    // 没有 after_key，表示当前页已经是最后一页。
                    break;
                }

                // 第 8 步：页与页之间短暂休眠，降低连续聚合查询对 ES 的瞬时压力。
                if (MEMBER_HISTORY_METRICS_ES_PAGE_SLEEP_MS > 0) {
                    sleepQuietly(MEMBER_HISTORY_METRICS_ES_PAGE_SLEEP_MS);
                }
            }
        } finally {
            // 第 9 步：无论 ES 查询正常结束还是异常退出，都关闭线程池。
            executor.shutdown();
            try {
                // 等待已经提交的会员更新任务完成，最多等待 10 分钟。
                if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                    // 超时仍未结束时强制中断线程，避免任务线程长期占用资源。
                    executor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                // 当前任务线程被中断时，同时中断线程池任务并恢复中断标记。
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("等待会员历史订单重建线程池关闭被中断，businessId={}", businessId, ex);
            }
        }

        // 第 10 步：输出最终处理结果。
        // processed 是从 ES 读到的会员 bucket 数；success/fail 是数据库更新任务的成功/失败数量。
        long costMs = System.currentTimeMillis() - startTime;
        log.info("会员历史订单重建完成，businessId={}, total≈{}, processed={}, success={}, fail={}, costMs={}",
                businessId, totalMemberCount, processedCounter, successCounter.get(), failCounter.get(), costMs);
        XxlJobHelper.log("会员历史订单重建完成，businessId={}, total≈{}, processed={}, success={}, fail={}, costMs={}",
                businessId, totalMemberCount, processedCounter, successCounter.get(), failCounter.get(), costMs);
    }

    private void processHistoryOrderMetricsPage(Long businessId,
                                                List<CompositeBucket> buckets,
                                                boolean resetTotalOrderNum,
                                                LocalDateTime now,
                                                ExecutorService executor,
                                                AtomicLong successCounter,
                                                AtomicLong failCounter) {
        // 一个 bucket 代表一个会员。本方法只处理当前 ES 分页，不保存跨页数据。
        // 当前页的所有数据库更新任务提交后会统一等待，确保主流程进入下一页时本页已经处理完成。
        List<CompletableFuture<Void>> futures = new ArrayList<>(buckets.size());
        for (CompositeBucket bucket : buckets) {
            // 每个会员独立更新自己的 wx_member 记录，会员之间没有依赖关系，因此可以并发执行。
            Runnable task = () -> {
                // 子线程不会自动继承动态数据源上下文，这里显式指定分片数据源。
                DynamicDataSourceContextHolder.push(DsNameConstants.SHARDING);
                try {
                    // 从当前会员 bucket 中读取 ES 聚合值，并更新数据库中的统计字段。
                    updateSingleMemberHistoryMetrics(bucket, resetTotalOrderNum, now);
                    successCounter.incrementAndGet();
                } catch (Exception ex) {
                    // 单个会员更新失败只记录失败数量，不影响同一页其他会员继续处理。
                    failCounter.incrementAndGet();
                    log.warn("会员历史订单重建失败，businessId={}, memberId={}", businessId, extractMemberId(bucket), ex);
                } finally {
                    // 无论更新成功还是失败，都必须清理当前线程的数据源上下文，避免污染线程池复用线程。
                    DynamicDataSourceContextHolder.poll();
                }
            };
            futures.add(CompletableFuture.runAsync(TtlRunnable.get(task), executor));
        }
        // 等待当前页所有会员更新结束；异常已经在任务内部捕获，因此这里主要承担同步屏障作用。
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private long logMemberHistoryRebuildProgress(Long businessId,
                                                 long totalMemberCount,
                                                 long processedCounter,
                                                 long nextLogThreshold,
                                                 long successCount,
                                                 long failCount) {
        // 只有处理数量达到下一个 5000 的倍数时才打印，避免每个会员都打印日志影响性能。
        while (processedCounter >= nextLogThreshold) {
            if (totalMemberCount > 0) {
                // totalMemberCount 是 cardinality 估算值，因此这里的百分比仅用于观察执行进度。
                BigDecimal progress = BigDecimal.valueOf(processedCounter)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalMemberCount), 2, RoundingMode.HALF_UP);
                log.info("会员历史订单重建进度，businessId={}, processed={}/{} ({}%), success={}, fail={}",
                        businessId, processedCounter, totalMemberCount, progress, successCount, failCount);
                XxlJobHelper.log("会员历史订单重建进度，businessId={}, processed={}/{} ({}%), success={}, fail={}",
                        businessId, processedCounter, totalMemberCount, progress, successCount, failCount);
            } else {
                // 总量查询失败或没有符合条件的会员时，不计算百分比，只记录已处理数量。
                log.info("会员历史订单重建进度，businessId={}, processed={}, success={}, fail={}",
                        businessId, processedCounter, successCount, failCount);
                XxlJobHelper.log("会员历史订单重建进度，businessId={}, processed={}, success={}, fail={}",
                        businessId, processedCounter, successCount, failCount);
            }
            // 使用 while 而不是 if，防止一次跨过多个日志阈值时漏打印中间进度。
            nextLogThreshold += MEMBER_HISTORY_METRICS_PROGRESS_STEP;
        }
        return nextLogThreshold;
    }

    private long extractMemberId(CompositeBucket bucket) {
        // composite bucket 的 key 结构由 memberId source 生成；异常或字段缺失时返回 0，便于日志记录。
        if (bucket == null || bucket.key() == null || bucket.key().get("memberId") == null) {
            return 0L;
        }
        return bucket.key().get("memberId").longValue();
    }

    private long estimateMemberHistoryOrderMetricsTotalFromEs(Long businessId, boolean resetTotalOrderNum) {
        // 只做总量估算，不拉取文档明细：size=0 且使用 cardinality 聚合，结果仅用于进度日志。
        // 查询条件必须与实际分页查询保持一致，否则日志中的总量会与实际处理量不匹配。
        SearchRequest request = SearchRequest.of(s -> s
                .index("bz_order")
                .query(buildMemberHistoryOrderMetricsQuery(businessId, resetTotalOrderNum))
                .aggregations("member_count", Aggregation.of(a -> a
                        .cardinality(c -> c.field("memberId").precisionThreshold(40000))))
                .size(0)
                .trackTotalHits(t -> t.enabled(false))
                .timeout("60s"));

        try {
            SearchResponse<Void> response = esClient.search(request, Void.class);
            if (response == null || response.aggregations() == null) {
                // ES 返回为空时按 0 处理，不阻断真正的分页重建。
                return 0L;
            }
            Aggregate aggregate = response.aggregations().get("member_count");
            if (aggregate == null || aggregate.cardinality() == null) {
                // 聚合缺失时按 0 处理；该方法只影响进度显示，不影响后续分页。
                return 0L;
            }
            return Math.round(aggregate.cardinality().value());
        } catch (Exception e) {
            // 总量估算失败不终止重建，后续分页查询仍会继续执行。
            log.warn("查询会员历史订单重建总量失败，businessId={}", businessId, e);
            return 0L;
        }
    }

    private void updateSingleMemberHistoryMetrics(CompositeBucket bucket,
                                                  boolean resetTotalOrderNum,
                                                  LocalDateTime now) {
        // 本方法负责把一个 ES member bucket 转换为一次 wx_member 更新。
        // ES 中的订单数使用 orderId cardinality，避免同一订单重复索引或重复记录导致数量放大。
        long memberId = extractMemberId(bucket);
        int shardingValue = (int) (memberId % 10);

        // 会员表按 memberId % 10 分片，memberId 和 shardingValue 同时作为条件，避免更新错误分片的数据。
        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, shardingValue);

        long q7Count = getFilterCardinalityValue(bucket, "seven_day", "order_count");
        BigDecimal q7Sum = getFilterSumValue(bucket, "seven_day", "order_sum");
        // 写入 7 天订单数、金额和平均金额；平均金额由本地根据聚合后的总金额和订单数计算。
        updateWrapper.set(WxMemberDO::getSevenDayOrderCount, (int) q7Count)
                .set(WxMemberDO::getSevenDayOrderSum, q7Sum)
                .set(WxMemberDO::getSevenDayOrderAvg, calculateAvg(q7Sum, q7Count));

        long q30Count = getFilterCardinalityValue(bucket, "thirty_day", "order_count");
        BigDecimal q30Sum = getFilterSumValue(bucket, "thirty_day", "order_sum");
        // 写入 30 天订单数、金额和平均金额。
        updateWrapper.set(WxMemberDO::getThirtyDayOrderCount, (int) q30Count)
                .set(WxMemberDO::getThirtyDayOrderSum, q30Sum)
                .set(WxMemberDO::getThirtyDayOrderAvg, calculateAvg(q30Sum, q30Count));

        long hYearCount = getFilterCardinalityValue(bucket, "half_year", "order_count");
        BigDecimal hYearSum = getFilterSumValue(bucket, "half_year", "order_sum");
        // 写入半年订单数、金额和平均金额。
        updateWrapper.set(WxMemberDO::getHalfYearOrderCount, (int) hYearCount)
                .set(WxMemberDO::getHalfYearOrderSum, hYearSum)
                .set(WxMemberDO::getHalfYearOrderAvg, calculateAvg(hYearSum, hYearCount));

        long yearCount = getFilterCardinalityValue(bucket, "one_year", "order_count");
        BigDecimal yearSum = getFilterSumValue(bucket, "one_year", "order_sum");
        // 写入一年订单数、金额和平均金额。
        updateWrapper.set(WxMemberDO::getOneYearOrderCount, (int) yearCount)
                .set(WxMemberDO::getOneYearOrderSum, yearSum)
                .set(WxMemberDO::getOneYearOrderAvg, calculateAvg(yearSum, yearCount));

        if (resetTotalOrderNum) {
            // 只有明确开启重建总订单数时才覆盖 total_order_num，避免影响只重建时间窗口统计的场景。
            updateWrapper.set(WxMemberDO::getTotalOrderNum, (int) getCardinalityValue(bucket, "total_order_num"));
        }

        // 当前页使用同一个 now，保证这一页会员的更新时间基本一致。
        updateWrapper.set(WxMemberDO::getUpdateTime, now);

        // 不开启事务；每个会员的更新独立提交，单个会员失败不会回滚其他会员。
        wxMemberMapper.update(null, updateWrapper);
    }


    private SearchResponse<Void> searchMemberOrderMetricsFromEsWithRetry(Long businessId,
                                                                          boolean resetTotalOrderNum,
                                                                          Map<String, FieldValue> afterKey) throws IOException {
        // 仅对 ES 过载常见的 429 做重试，其他异常直接抛出，避免无意义地重复查询。
        int retryCount = 0;
        while (true) {
            try {
                // afterKey 为 null 时查询第一页，否则查询指定游标之后的下一页。
                return searchMemberOrderMetricsFromEs(businessId, resetTotalOrderNum, afterKey);
            } catch (IOException e) {
                if (!isEsTooManyRequests(e) || retryCount >= MEMBER_HISTORY_METRICS_ES_MAX_RETRY) {
                    // 非 429 或达到最大重试次数，交由上层结束本次重建。
                    throw e;
                }
                retryCount++;
                long sleepMs = MEMBER_HISTORY_METRICS_ES_RETRY_BASE_SLEEP_MS * retryCount;
                log.warn("会员历史订单重建触发 ES 429，businessId={}, retryCount={}, sleepMs={}", businessId, retryCount, sleepMs, e);
                // 线性退避，给 ES 释放线程和堆内存的机会。
                sleepQuietly(sleepMs);
            }
        }
    }

    private void clearMemberHistoryOrderMetrics(Long businessId, boolean resetTotalOrderNum) {
        // 按 0~9 十个分片逐个清零，避免对分表场景使用不带分片条件的全表更新。
        for (int shard = 0; shard < 10; shard++) {
            UpdateWrapper<WxMemberDO> wrapper = new UpdateWrapper<>();
            wrapper.set("seven_day_order_count", 0)
                    .set("seven_day_order_avg", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("seven_day_order_sum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("thirty_day_order_count", 0)
                    .set("thirty_day_order_avg", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("thirty_day_order_sum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("half_year_order_count", 0)
                    .set("half_year_order_avg", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("half_year_order_sum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("one_year_order_count", 0)
                    .set("one_year_order_avg", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("one_year_order_sum", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .set("update_time", LocalDateTime.now());
            if (resetTotalOrderNum) {
                // 与重建逻辑保持一致：只有 true 时才清零并重建 total_order_num。
                wrapper.set("total_order_num", 0);
            }
            wrapper.lambda()
                    .eq(WxMemberDO::getBusinessId, businessId)
                    .eq(WxMemberDO::getShardingValue, shard);
            wxMemberMapper.update(wrapper);
        }
    }

    private SearchResponse<Void> searchMemberOrderMetricsFromEs(Long businessId,
                                                                boolean resetTotalOrderNum,
                                                                Map<String, FieldValue> afterKey) throws IOException {
        // 所有时间窗口都采用左闭右开区间 [start, todayStart)，不统计当天 00:00 之后尚未结束的一天。
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDayStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime thirtyDayStart = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime halfYearStart = LocalDate.now().minusDays(180).atStartOfDay();
        LocalDateTime oneYearStart = LocalDate.now().minusDays(365).atStartOfDay();

        Map<String, Aggregation> subAggregations = new LinkedHashMap<>();
        if (resetTotalOrderNum) {
            // 总订单数不受一年窗口限制，因此只有开启该选项时才额外计算全部历史 orderId cardinality。
            subAggregations.put("total_order_num", Aggregation.of(a -> a
                    .cardinality(c -> c.field("orderId").precisionThreshold(40000))));
        }
        subAggregations.put("seven_day", buildHistoryWindowAggregation(sevenDayStart, todayStart));
        subAggregations.put("thirty_day", buildHistoryWindowAggregation(thirtyDayStart, todayStart));
        subAggregations.put("half_year", buildHistoryWindowAggregation(halfYearStart, todayStart));
        subAggregations.put("one_year", buildHistoryWindowAggregation(oneYearStart, todayStart));

        // composite 按 memberId 聚合并分页，每页最多返回固定数量会员，避免一次性在 ES 和应用内构造全部会员结果。
        CompositeAggregation.Builder compositeBuilder = new CompositeAggregation.Builder()
                .size(MEMBER_HISTORY_METRICS_ES_PAGE_SIZE)
                .sources(Collections.singletonMap("memberId",
                        CompositeAggregationSource.of(s -> s.terms(t -> t.field("memberId").valueType(ValueType.Long)))));
        if (afterKey != null && !afterKey.isEmpty()) {
            // composite 分页必须使用上一页 ES 返回的 after_key，不能自行用最后一个 memberId 替代。
            compositeBuilder.after(afterKey);
        }

        // size=0 表示不返回订单文档，只返回聚合结果；查询超时时间为 120 秒。
        SearchRequest request = SearchRequest.of(s -> s
                .index("bz_order")
                .query(buildMemberHistoryOrderMetricsQuery(businessId, resetTotalOrderNum))
                .aggregations("users", Aggregation.of(a -> a
                        .composite(compositeBuilder.build())
                        .aggregations(subAggregations)))
                .size(0)
                .trackTotalHits(t -> t.enabled(false))
                .timeout("120s"));

        return esClient.search(request, Void.class);
    }

    private Query buildMemberHistoryOrderMetricsQuery(Long businessId, boolean resetTotalOrderNum) {
        // 构造 ES 外层过滤条件：业务、有效订单状态、必要字段以及会员 ID 非 0。
        // resetTotalOrderNum=false 时追加一年时间范围，以减少 ES 扫描数据量；true 时保留全部历史订单用于计算总订单数。
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime oneYearStart = LocalDate.now().minusDays(365).atStartOfDay();

        List<Query> mustQueries = new ArrayList<>();
        mustQueries.add(TermQuery.of(t -> t.field("businessId").value(businessId))._toQuery());
        mustQueries.add(TermsQuery.of(t -> t.field("orderState")
                .terms(ts -> ts.value(Arrays.asList(FieldValue.of(40), FieldValue.of(60), FieldValue.of(80), FieldValue.of(200)))))._toQuery());
        mustQueries.add(ExistsQuery.of(e -> e.field("memberId"))._toQuery());
        mustQueries.add(ExistsQuery.of(e -> e.field("orderId"))._toQuery());
        mustQueries.add(ExistsQuery.of(e -> e.field("payAmount"))._toQuery());
        mustQueries.add(ExistsQuery.of(e -> e.field("createTime"))._toQuery());
        if (!resetTotalOrderNum) {
            // 时间范围使用 [oneYearStart, todayStart)，与一年聚合窗口保持一致。
            mustQueries.add(RangeQuery.of(r -> r.date(d -> d
                    .field("createTime")
                    .gte(oneYearStart.format(ES_DATE_TIME_FORMATTER))
                    .lt(todayStart.format(ES_DATE_TIME_FORMATTER))
                    .format("strict_date_optional_time")))._toQuery());
        }

        return BoolQuery.of(b -> b
                .must(mustQueries)
                .mustNot(TermQuery.of(t -> t.field("memberId").value(0))._toQuery())
        )._toQuery();
    }

    private Aggregation buildHistoryWindowAggregation(LocalDateTime start, LocalDateTime endExclusive) {
        // 为一个时间窗口构造 filter 聚合，并在窗口内分别统计去重订单数和付款金额总和。
        // 外层已经按会员分桶，因此这里的结果天然是“某会员在该窗口内”的统计值。
        return Aggregation.of(a -> a
                .filter(RangeQuery.of(r -> r.date(d -> d
                        .field("createTime")
                        .gte(start.format(ES_DATE_TIME_FORMATTER))
                        .lt(endExclusive.format(ES_DATE_TIME_FORMATTER))
                        .format("strict_date_optional_time")))._toQuery())
                .aggregations(Map.of(
                        "order_count", Aggregation.of(x -> x.cardinality(c -> c.field("orderId").precisionThreshold(40000))),
                        "order_sum", Aggregation.of(x -> x.sum(sum -> sum.field("payAmount").missing(0)))
                )));
    }

    private long getCardinalityValue(CompositeBucket bucket, String aggName) {
        // 读取 bucket 级 cardinality 聚合；聚合缺失时返回 0，避免异常中断整页处理。
        Aggregate aggregate = bucket.aggregations().get(aggName);
        return aggregate == null || aggregate.cardinality() == null ? 0L : Math.round(aggregate.cardinality().value());
    }

    private long getFilterCardinalityValue(CompositeBucket bucket, String filterAggName, String subAggName) {
        // 先找到时间窗口 filter，再读取其中的订单数 cardinality 聚合。
        Aggregate aggregate = bucket.aggregations().get(filterAggName);
        if (aggregate == null || aggregate.filter() == null) {
            return 0L;
        }
        Aggregate subAggregate = aggregate.filter().aggregations().get(subAggName);
        return subAggregate == null || subAggregate.cardinality() == null ? 0L : Math.round(subAggregate.cardinality().value());
    }

    private BigDecimal getFilterSumValue(CompositeBucket bucket, String filterAggName, String subAggName) {
        // 先找到时间窗口 filter，再读取其中的付款金额 sum 聚合，并统一保留两位小数。
        Aggregate aggregate = bucket.aggregations().get(filterAggName);
        if (aggregate == null || aggregate.filter() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        Aggregate subAggregate = aggregate.filter().aggregations().get(subAggName);
        if (subAggregate == null || subAggregate.sum() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(subAggregate.sum().value()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAvg(BigDecimal sum, long count) {
        // 没有订单时平均金额按 0.00 处理；有订单时按两位小数四舍五入。
        if (count <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private boolean isEsTooManyRequests(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ResponseException responseException
                    && responseException.getResponse() != null
                    && responseException.getResponse().getStatusLine() != null
                    && responseException.getResponse().getStatusLine().getStatusCode() == 429) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepQuietly(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("线程休眠被中断", ex);
        }
    }

    private void processHistoryOrderBatch(List<Long> batch,
                                          Map<Long, BzOrderDTO> oneYearMap,
                                          Map<Long, BzOrderDTO> halfYearMap,
                                          Map<Long, BzOrderDTO> thirtyDaysMap,
                                          Map<Long, BzOrderDTO> sevenDaysMap,
                                          Map<Long, BzOrderDTO> yesterdayMap) {

        for (Long memberId : batch) {
            int yestCount = getCount(yesterdayMap, memberId);
            BigDecimal yestSum = getSum(yesterdayMap, memberId);

            int oneYearOutCount = getCount(oneYearMap, memberId);
            BigDecimal oneYearOutSum = getSum(oneYearMap, memberId);

            int halfYearOutCount = getCount(halfYearMap, memberId);
            BigDecimal halfYearOutSum = getSum(halfYearMap, memberId);

            int thirtyDayOutCount = getCount(thirtyDaysMap, memberId);
            BigDecimal thirtyDayOutSum = getSum(thirtyDaysMap, memberId);

            int sevenDayOutCount = getCount(sevenDaysMap, memberId);
            BigDecimal sevenDayOutSum = getSum(sevenDaysMap, memberId);

            UpdateWrapper<WxMemberDO> wrapper = new UpdateWrapper<>();
            wrapper.lambda()
                    .eq(WxMemberDO::getShardingValue, memberId % 10)
                    .eq(WxMemberDO::getMemberId, memberId);

            if (yestCount > 0) {
                wrapper.setSql("total_order_num = total_order_num + " + yestCount);
            }

            appendRollingMetricSql(wrapper, "one_year_order", oneYearOutSum, oneYearOutCount, yestSum, yestCount);
            appendRollingMetricSql(wrapper, "half_year_order", halfYearOutSum, halfYearOutCount, yestSum, yestCount);
            appendRollingMetricSql(wrapper, "thirty_day_order", thirtyDayOutSum, thirtyDayOutCount, yestSum, yestCount);
            appendRollingMetricSql(wrapper, "seven_day_order", sevenDayOutSum, sevenDayOutCount, yestSum, yestCount);

            try {
                wxMemberMapper.update(wrapper);
            } catch (Exception e) {
                log.warn("updateHistoryOrderData 更新失败，memberId={}", memberId, e);
            }
        }
    }


    private void appendRollingMetricSql(UpdateWrapper<WxMemberDO> wrapper,
                                        String prefix,
                                        BigDecimal outSum,
                                        int outCount,
                                        BigDecimal inSum,
                                        int inCount) {

        String outSumValue = safeAmount(outSum).toPlainString();
        String inSumValue = safeAmount(inSum).toPlainString();
        String newSumExpr = prefix + "_sum - " + outSumValue + " + " + inSumValue;
        String newCountExpr = prefix + "_count - " + outCount + " + " + inCount;

        wrapper.setSql(prefix + "_sum = " + newSumExpr)
                .setSql(prefix + "_count = " + newCountExpr)
                .setSql(prefix + "_avg = CASE " +
                        "WHEN " + prefix + "_count <= 0 THEN 0 " +
                        "ELSE ROUND(" + prefix + "_sum / " + prefix + "_count, 2) END");
    }

    private Map<Long, BzOrderDTO> toOrderMetricMap(List<BzOrderDTO> orderList) {
        if (CollectionUtil.isEmpty(orderList)) {
            return Collections.emptyMap();
        }
        return orderList.stream().collect(Collectors.toMap(
                BzOrderDTO::getMemberId,
                item -> item,
                (a, b) -> a
        ));
    }

    private int getCount(Map<Long, BzOrderDTO> map, Long memberId) {
        if (CollectionUtil.isEmpty(map)) {
            return 0;
        }
        BzOrderDTO dto = map.get(memberId);
        return dto == null || dto.getCount() == null ? 0 : dto.getCount();
    }

    private BigDecimal getSum(Map<Long, BzOrderDTO> map, Long memberId) {
        if (CollectionUtil.isEmpty(map)) {
            return BigDecimal.ZERO;
        }
        BzOrderDTO dto = map.get(memberId);
        return dto == null || dto.getSum() == null ? BigDecimal.ZERO : dto.getSum();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }


//
//    @Override
//    @DS(DsNameConstants.SHARDING)
//    @DSTransactional
//    @DataPermission(enable = false)
//    public void updateHistoryOrderData() {
//
//        List<BzOrderDTO> oneYear = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(366), DateUtils.getDaysBeforeEnd(366));
//        // 查询180days订单数据
//        List<BzOrderDTO> halfYear = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(181), DateUtils.getDaysBeforeEnd(181));
//
//        List<BzOrderDTO> thirtyDays = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(31), DateUtils.getDaysBeforeEnd(31));
//
//        List<BzOrderDTO> sevenDays = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(8), DateUtils.getDaysBeforeEnd(8));
//
//        List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(1), DateUtils.getDaysBeforeEnd(1));
//
//        Map<Long, BzOrderDTO> yesterdayMap = yesterday.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
//        Map<Long, BzOrderDTO> oneYearMap = oneYear.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
//        Map<Long, BzOrderDTO> halfYearMap = halfYear.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
//        Map<Long, BzOrderDTO> thirtyDaysMap = thirtyDays.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
//        Map<Long, BzOrderDTO> sevenDaysMap = sevenDays.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
//
//        List<BzOrderDTO> listAll = new ArrayList<>();
//        listAll.addAll(oneYear);
//        listAll.addAll(halfYear);
//        listAll.addAll(thirtyDays);
//        listAll.addAll(sevenDays);
//        listAll.addAll(yesterday);
//        List<Long> memberIds = listAll.stream().map(BzOrderDTO::getMemberId).distinct().collect(Collectors.toList());
//        System.gc();
//        List<List<Long>> split = ListUtil.split(memberIds, 10);
//        Date date = new Date();
//
//        for (List<Long> longs : split) {
////            threadPoolTaskExecutor.submit(() -> {
//            UpdateWrapper<WxMemberDO> wxMemberUpdateChain = new UpdateWrapper<>();
//            for (Long memberId : longs) {
//                wxMemberUpdateChain.lambda().eq(WxMemberDO::getUpdateTime, date)
//                        .eq(WxMemberDO::getShardingValue, memberId % 10)
//                        .eq(WxMemberDO::getMemberId, memberId);
//
//                int yestCount = 0;
//                BigDecimal yestSum = BigDecimal.ZERO;
//                if (ObjectUtil.isNotEmpty(yesterday)) {
//                    if (yesterdayMap.containsKey(memberId)) {
////                        yestCount = yesterdayMap.get(memberId).getCount();
////                        yestSum = yesterdayMap.get(memberId).getSum();
////                        wxMemberUpdateChain.setSql("total_order_num = total_order_num +" + yestCount);
//                        // one_year_order_sum
//
//                        BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
//                        if (ObjectUtil.isEmpty(BzOrderDTO)) {
//                            BzOrderDTO = new BzOrderDTO();
//                            BzOrderDTO.setSum(BigDecimal.ZERO);
//                            BzOrderDTO.setCount(0);
//                        }
//                        wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
//                                .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
//                                .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
//                                        + "(one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
//
//                        // half_year_order_sum
//                        BzOrderDTO BzOrderDTO1 = halfYearMap.get(memberId);
//                        if (ObjectUtil.isEmpty(BzOrderDTO1)) {
//                            BzOrderDTO1 = new BzOrderDTO();
//                            BzOrderDTO1.setSum(BigDecimal.ZERO);
//                            BzOrderDTO1.setCount(0);
//                        }
//                        wxMemberUpdateChain.setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO1.getSum() + "+" + yestSum)
//                                .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO1.getCount() + "+" + yestCount)
//                                .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO1.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(half_year_order_sum -" + BzOrderDTO1.getSum() + "+" + yestSum + ") / "
//                                        + "(half_year_order_count -" + BzOrderDTO1.getCount() + "+" + yestCount + "), 2) END");
//
//                        // thirty_day_order_sum
//
//                        BzOrderDTO BzOrderDTO2 = thirtyDaysMap.get(memberId);
//                        if (ObjectUtil.isEmpty(BzOrderDTO2)) {
//                            BzOrderDTO2 = new BzOrderDTO();
//                            BzOrderDTO2.setSum(BigDecimal.ZERO);
//                            BzOrderDTO2.setCount(0);
//                        }
//                        wxMemberUpdateChain.setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO2.getSum() + "+" + yestSum)
//                                .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO2.getCount() + "+" + yestCount)
//                                .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO2.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(thirty_day_order_sum -" + BzOrderDTO2.getSum() + "+" + yestSum + ") / "
//                                        + "(thirty_day_order_count -" + BzOrderDTO2.getCount() + "+" + yestCount + "), 2) END");
//
//                        // seven_day_order_sum
//
//                        BzOrderDTO BzOrderDTO3 = thirtyDaysMap.get(memberId);
//                        if (ObjectUtil.isEmpty(BzOrderDTO3)) {
//                            BzOrderDTO3 = new BzOrderDTO();
//                            BzOrderDTO3.setSum(BigDecimal.ZERO);
//                            BzOrderDTO3.setCount(0);
//                        }
//                        wxMemberUpdateChain.setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO3.getSum() + "+" + yestSum)
//                                .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO3.getCount() + "+" + yestCount)
//                                .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO3.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(seven_day_order_sum -" + BzOrderDTO3.getSum() + "+" + yestSum + ") / "
//                                        + "(seven_day_order_count -" + BzOrderDTO3.getCount() + "+" + yestCount + "), 2) END");
//
//                    } else {
//                        yestCount = 0;
//                        yestSum = BigDecimal.ZERO;
//                        // one_year_order_sum
//                        if(oneYearMap.containsKey(memberId)){
//                            BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
//                            wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum)
//                                    .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount)
//                                    .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + "+ yestSum +"= 0 THEN 0 " +
//                                            "ELSE ROUND("+ "(one_year_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum +") / "
//                                            +"(one_year_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount +"), 2) END");
//                        }
//                        // half_year_order_sum
//                        if(halfYearMap.containsKey(memberId)){
//                            BzOrderDTO BzOrderDTO = halfYearMap.get(memberId);
//                            wxMemberUpdateChain .setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum)
//                                    .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount)
//                                    .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO.getCount() + " + "+ yestSum +"= 0 THEN 0 " +
//                                            "ELSE ROUND("+ "(half_year_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum +") / "
//                                            +"(half_year_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount +"), 2) END");
//                        }
//                        // thirty_day_order_sum
//                        if(thirtyDaysMap.containsKey(memberId)){
//                            BzOrderDTO BzOrderDTO = thirtyDaysMap.get(memberId);
//                            wxMemberUpdateChain .setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum)
//                                    .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount)
//                                    .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO.getCount() + " + "+ yestSum +"= 0 THEN 0 " +
//                                            "ELSE ROUND("+ "(thirty_day_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum +") / "
//                                            +"(thirty_day_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount +"), 2) END");
//                        }
//                        // seven_day_order_sum
//                        if(sevenDaysMap.containsKey(memberId)){
//                            BzOrderDTO BzOrderDTO = sevenDaysMap.get(memberId);
//                            wxMemberUpdateChain .setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum)
//                                    .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount)
//                                    .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO.getCount() + " + "+ yestSum +"= 0 THEN 0 " +
//                                            "ELSE ROUND("+ "(seven_day_order_sum -" + BzOrderDTO.getSum()+ "+" + yestSum +") / "
//                                            +"(seven_day_order_count -" + BzOrderDTO.getCount()+ "+" + yestCount +"), 2) END");
//                        }
//                    }
//                } else {
//                    yestCount = 0;
//                    yestSum = BigDecimal.ZERO;
//                    // one_year_order_sum
//                    if (oneYearMap.containsKey(memberId)) {
//                        BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
//                        wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
//                                .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
//                                .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
//                                        + "(one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
//                    }
//                    // half_year_order_sum
//                    if (halfYearMap.containsKey(memberId)) {
//                        BzOrderDTO BzOrderDTO = halfYearMap.get(memberId);
//                        wxMemberUpdateChain.setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
//                                .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
//                                .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
//                                        + "(half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
//                    }
//                    // thirty_day_order_sum
//                    if (thirtyDaysMap.containsKey(memberId)) {
//                        BzOrderDTO BzOrderDTO = thirtyDaysMap.get(memberId);
//                        wxMemberUpdateChain.setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
//                                .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
//                                .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
//                                        + "(thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
//                    }
//                    // seven_day_order_sum
//                    if (sevenDaysMap.containsKey(memberId)) {
//                        BzOrderDTO BzOrderDTO = sevenDaysMap.get(memberId);
//                        wxMemberUpdateChain.setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
//                                .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
//                                .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
//                                        "ELSE ROUND(" + "(seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
//                                        + "(seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
//                    }
//                }
//                wxMemberMapper.update(wxMemberUpdateChain);
//            }
//        }
//    }

    @Override
    @DS(DsNameConstants.SHARDING)
    //@DSTransactional
    @DataPermission(enable = false)
    public void updateMemberLabel() {
        for (int i = 0; i < 10; i++) {
            final int index = i;
            //threadPoolTaskExecutor.submit(() -> {
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .setSql("member_label = CASE  WHEN  TIMESTAMPDIFF(DAY,fourth_login_time, NOW()) <= 7 THEN 1 " +
                            " WHEN TIMESTAMPDIFF(DAY,third_order_finish_time, NOW()) <= 7 THEN 2 " +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 7 AND 14 THEN 3" +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 14 AND 30 THEN 4" +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 30 AND 90 THEN 5" +
                            "        ELSE 6" +
                            " END")
                    .lambda().eq(WxMemberDO::getShardingValue, index);
            wxMemberMapper.update(updateWrapper);
            //});
        }
    }


    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    @DataPermission(enable = false)
    public void updateFirstOrderStoreId() {
        LocalDateTime yesterdayStart = LocalDate.now()
                .minusDays(1)
                .atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now()
                .minusDays(1)
                .atTime(23, 59, 59);
        //List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderStoreData(DateUtils.getAfterDayDateToString(1), DateUtils.getAfterDayDate(1));
        List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderStoreData(yesterdayStart, yesterdayEnd);
        Map<Long, Long> firstStoreId = yesterday.stream()
                .collect(Collectors.groupingBy(BzOrderDTO::getMemberId,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(BzOrderDTO::getCreateTime)),
                                list -> list.map(BzOrderDTO::getStoreId).orElse(null)
                        )
                ));

        for (Map.Entry<Long, Long> entry : firstStoreId.entrySet()) {
            Long key = entry.getKey();
            Long value = entry.getValue();
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("first_order_store_id = case when first_order_store_id = 0 then " + value + " else first_order_store_id end");
            updateWrapper.lambda().eq(WxMemberDO::getMemberId, key).eq(WxMemberDO::getShardingValue, key % 10);
            wxMemberMapper.update(updateWrapper);
        }
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    @DataPermission(enable = false)
    public void nocHistoryOrderNum() {
        List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(1), DateUtils.getDaysBeforeEnd(1));
        Map<Long, BzOrderDTO> yesterdayMap = yesterday.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
        for (Map.Entry<Long, BzOrderDTO> entry : yesterdayMap.entrySet()) {
            Long key = entry.getKey();
            BzOrderDTO value = entry.getValue();
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("total_order_num = total_order_num +" + value.getCount());
            updateWrapper.lambda().eq(WxMemberDO::getMemberId, key)
                    .eq(WxMemberDO::getShardingValue, key % 10);
            wxMemberMapper.update(updateWrapper);
        }
    }

    @Override
    public void automaticDistributionOnMemberDaysJobHandler() {

    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    @DataPermission(enable = false)
    public void createMemberCrowdTask(long businessId) throws IOException {

        String busId = null;
        if(businessId != 0){
            busId = String.valueOf(businessId);
        }else{
            busId = XxlJobHelper.getJobParam();
        }

        if(StringUtils.isBlank(busId)){
            log.error("释放冻结积分定时任务，获取项目失败");
            return;
        }

        // 清空人群会员关系表数据
        wxMemberCrowdRefService.trunc();

        // 读取人群表数据
        List<CustomCrowdDO> customCrowdDOList = customCrowdService.getAll(busId);

        // todo 读取社群
        List<Long> communityList = wxMemberService.getCommunityList(busId);
        List<Long> notIncommunityList = wxMemberService.getNotInCommunityList(busId);
        // 读取全部member表 会员信息
        List<Long> allMemberList = new ArrayList<>();
        // 根据人群表数量 分别获取不同人群的会员ID
        for(CustomCrowdDO customCrowdDO : customCrowdDOList){

            // 如果基础信息，客户行为，客户分析 都没有填写 不进行人群关系定时任务执行
            if (isAllNullOrZero(customCrowdDO)) {
                continue;
            }

            log.info("定时任务开始人群 ({}) 会员信息写入]", customCrowdDO.getCrowdName());

            // 1 读取人群基本信息(基本信息+客户分析中的回购周期与末次下单时间)
            List<Long> memberBasicList = new ArrayList<>();
            // customCrowdDO.getBasicInfo() != null && (customCrowdDO.getBasicInfo() == 1 || customCrowdDO.getCustomAnalysis() == 1)
            boolean basicFlag = isBooleanBasicInfo(customCrowdDO);
            if( basicFlag ||
                    (customCrowdDO.getCommunityFlag() != null &&
                            (customCrowdDO.getCommunityFlag() == 1 || customCrowdDO.getCommunityFlag() == 2)
                    )
            ){
                if (!isOnlyCommunityFlag(customCrowdDO)){
                    // 配置了基础信息
                    memberBasicList = wxMemberService.getMemberWithCrowd(customCrowdDO);
                    // 没在社群 不在社群集合
                    if (Objects.equals(OUT_COMMUNITY_FLAG,customCrowdDO.getCommunityFlag())){

                        if(CollectionUtil.isNotEmpty(communityList)){
                            memberBasicList = getListSetDifference(memberBasicList,communityList);
                        }
                    }
                    // 在社群  求交集
                    if (Objects.equals(IN_COMMUNITY_FLAG,customCrowdDO.getCommunityFlag())){
                        if(CollectionUtil.isEmpty(communityList)){
                            continue;
                        } else {
                            memberBasicList = findCommonElements(memberBasicList,null,communityList);
                        }
                    }
                } else {
                    // 只配置了基础信息中的社群选项
                    // 没在社群 不在社群集合
                    if (Objects.equals(OUT_COMMUNITY_FLAG,customCrowdDO.getCommunityFlag())){
                        memberBasicList = notIncommunityList;
                    }
                    // 在社群  求交集
                    if (Objects.equals(IN_COMMUNITY_FLAG,customCrowdDO.getCommunityFlag())){
                        memberBasicList = communityList;
                    }
                }



                if (CollectionUtil.isEmpty(memberBasicList)) {
                    log.info("定时任务开始人群 ({}) 会员信息,基础信息数据为空]", customCrowdDO.getCrowdName());
                    continue;
                }
            }
            //
            List<Long> memberBehaviorList = new ArrayList<>();
            // 3 读取客户分析
            List<Long> memberAnalysisList = new ArrayList<>();
            // customCrowdDO.getCustomAnalysis() != null && customCrowdDO.getCustomAnalysis() == 1
            if(isBooleanCustomAnalysis(customCrowdDO)){
                memberAnalysisList = getAnalysisList(customCrowdDO);
                if (CollectionUtil.isEmpty(memberAnalysisList)) {
                    log.info("定时任务开始人群 ({}) 会员信息,客户分析数据为空]", customCrowdDO.getCrowdName());
                    continue;
                }
            }
            List<Long> result = findCommonElements(memberBasicList, memberBehaviorList, memberAnalysisList);
            // 2 读取人群客户行为

            if(customCrowdDO.getCustomBehavior() != null && customCrowdDO.getCustomBehavior() == 1){
                if (CollectionUtil.isEmpty(allMemberList)){
                    allMemberList = wxMemberService.getAllMemberId(customCrowdDO.getBusinessId());
                }
                result = getBehaviorList(customCrowdDO, result, allMemberList);
            }

            // 4 分段写入
            List<List<Long>> resultGroup = splitIntoGroupsWithStream(result);
            crowdMemberRefWrite(resultGroup, customCrowdDO.getId());

            // 5.数据置空
            memberBasicList = null;
            memberAnalysisList = null;
            result = null;
        }
        // 6.数据置空
        allMemberList =null;
     }

    private boolean isOnlyCommunityFlag(CustomCrowdDO customCrowdDO) {
        if ((customCrowdDO.getGender() == null || customCrowdDO.getGender() == 1 || customCrowdDO.getGender() == 0 ) &&
                // 生日没选
                (customCrowdDO.getBirthdayType() == null || customCrowdDO.getBirthdayType() == 0) &&
                // 会员等级没选
                (customCrowdDO.getMemberLevel() == null || "0".equals(customCrowdDO.getMemberLevel())) &&
                // 会员标签没选
                (customCrowdDO.getMemberTag() == null || customCrowdDO.getMemberTag() == 0) &&
                // 注册平台没选
                (customCrowdDO.getMemberCategory() == null || "0".equals(customCrowdDO.getMemberCategory())) &&
                // 会员积分没选
                (customCrowdDO.getMemberIntegralType() == null || customCrowdDO.getMemberIntegralType() == 0) &&
                // 所属门店没选
                (customCrowdDO.getBelongStore() == null || customCrowdDO.getBelongStore() == 0) &&
                // 回购周期没选
                (customCrowdDO.getRepurchaseType() == null || customCrowdDO.getRepurchaseType() == 0) &&
                // 末次距今下单时间没选
                (customCrowdDO.getFinalRepurchaseType() == null || customCrowdDO.getFinalRepurchaseType() == 0) &&

                (customCrowdDO.getCommunityFlag() != null &&
                        (customCrowdDO.getCommunityFlag() == 1 ||  customCrowdDO.getCommunityFlag() == 2))
        ){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private boolean isBooleanCustomAnalysis(CustomCrowdDO customCrowdDO) {
        if (customCrowdDO.getCustomAnalysis() != null && customCrowdDO.getCustomAnalysis() == 1){
            if (StringUtils.isBlank(customCrowdDO.getOrderFrequencyValue()) &&
                    StringUtils.isBlank(customCrowdDO.getOrderAmountValue()) &&
                    StringUtils.isBlank(customCrowdDO.getAvgAmountValue()) &&
                    (StringUtils.isBlank(customCrowdDO.getPurchaseCategory()) || "0".equals(customCrowdDO.getPurchaseCategory()))
            ){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private boolean isBooleanBasicInfo(CustomCrowdDO customCrowdDO) {
        if(customCrowdDO.getBasicInfo() != null && customCrowdDO.getBasicInfo() == 1){
            // 性别是全部  只选了性别并且是全部  跳过基础信息筛选
            if (customCrowdDO.getGender() != null && customCrowdDO.getGender() == 1 &&
                    // 生日没选
                    (customCrowdDO.getBirthdayType() == null || customCrowdDO.getBirthdayType() == 0) &&
                    // 会员等级没选
                    (customCrowdDO.getMemberLevel() == null || "0".equals(customCrowdDO.getMemberLevel())) &&
                    // 会员标签没选
                    (customCrowdDO.getMemberTag() == null || customCrowdDO.getMemberTag() == 0) &&
                    // 注册平台没选
                    (customCrowdDO.getMemberCategory() == null || "0".equals(customCrowdDO.getMemberCategory())) &&
                    // 会员积分没选
                    (customCrowdDO.getMemberIntegralType() == null || customCrowdDO.getMemberIntegralType() == 0) &&
                    // 所属门店没选
                    (customCrowdDO.getBelongStore() == null || customCrowdDO.getBelongStore() == 0) &&
                    // 回购周期没选
                    (customCrowdDO.getRepurchaseType() == null || customCrowdDO.getRepurchaseType() == 0) &&
                    // 末次距今下单时间没选
                    (customCrowdDO.getFinalRepurchaseType() == null || customCrowdDO.getFinalRepurchaseType() == 0)
            ){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        if(customCrowdDO.getCustomAnalysis() != null && (
                (customCrowdDO.getRepurchaseType() != null && customCrowdDO.getRepurchaseType() != 0) ||
                        (customCrowdDO.getFinalRepurchaseType() != null && customCrowdDO.getFinalRepurchaseType() != 0)
                )
        ){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private List<Long> getBehaviorList(CustomCrowdDO customCrowdDO, List<Long> result, List<Long> allMemberList) {
        List<Long> visitList = null;
        List<Long> memberBehaviorVisitList = null;
        List<Long> memberBehaviorCarList = null;
        List<Long> memberBehaviorShareList = null;
        // 1.访问小程序
        if (customCrowdDO.getVisitType() != null && customCrowdDO.getVisitType() != 0){
            // 访问过
            memberBehaviorVisitList = buildMemberBehaviorVisit(customCrowdDO);
            if (customCrowdDO.getVisitType() == 1) {
                // 访问过 ES查询无数据
                if (CollectionUtil.isEmpty(memberBehaviorVisitList)){
                    return memberBehaviorVisitList;
                }
                // 访问过 ES查询有数据
                result = findCommonElements(result, memberBehaviorVisitList, null);
            }
            if (customCrowdDO.getVisitType() == 2) {
                // 未访问过
                if (CollectionUtil.isEmpty(result)){
                    // 基础信息与客户分析未配置 只配置了客户行为 查询所有客户信息
                    result = allMemberList;
                }
                memberBehaviorVisitList = getListSetDifference(result, memberBehaviorVisitList);
                result = memberBehaviorVisitList;
            }
            if (CollectionUtil.isEmpty(result)) {
                return result;
            }
        }
        // 2.加入购物车
        if (customCrowdDO.getTrolleyType() != null && customCrowdDO.getTrolleyType() != 0){
            // 访问过
            memberBehaviorCarList = buildMemberBehaviorCar(customCrowdDO);
            if (customCrowdDO.getTrolleyType() == 1) {
                // 访问过 ES查询无数据
                if (CollectionUtil.isEmpty(memberBehaviorCarList)){
                    return memberBehaviorCarList;
                }
                // 访问过 ES查询有数据
                result = findCommonElements(result, memberBehaviorCarList, null);
            }
            if (customCrowdDO.getTrolleyType() == 2) {
                // 未访问过
                if (CollectionUtil.isEmpty(result)){
                    // 基础信息与客户分析未配置 只配置了客户行为 查询所有客户信息
                    result = allMemberList;
                }
                memberBehaviorCarList = getListSetDifference(result, memberBehaviorCarList);
                result = memberBehaviorCarList;
            }
            if (CollectionUtil.isEmpty(result)) {
                return result;
            }
        }
        // 3.分享小程序
        if (customCrowdDO.getShareType() != null && customCrowdDO.getShareType() != 0){
            // 访问过
            memberBehaviorShareList = buildMemberBehaviorShare(customCrowdDO);
              if (customCrowdDO.getShareType() == 1) {
                  // 访问过 ES查询无数据
                  if (CollectionUtil.isEmpty(memberBehaviorShareList)){
                      return memberBehaviorShareList;
                  }
                  // 访问过 ES查询有数据
                  result = findCommonElements(result, memberBehaviorShareList, null);
            }
            if (customCrowdDO.getShareType() == 2) {
                // 未访问过
                if (CollectionUtil.isEmpty(result)){
                    // 基础信息与客户分析未配置 只配置了客户行为 查询所有客户信息
                    result = allMemberList;
                }
                memberBehaviorShareList = getListSetDifference(result, memberBehaviorShareList);
                result = memberBehaviorShareList;
            }
        }
        return result;
    }

    private List<Long> buildMemberBehaviorShare(CustomCrowdDO customCrowdDO) {
        Integer shareValue = customCrowdDO.getShareValue();
        if (shareValue == null) {
            return null;
        }
        return getMemeberIdsByEvent(shareValue,EventType.SHARE,customCrowdDO.getBusinessId());
    }

    private List<Long> buildMemberBehaviorCar(CustomCrowdDO customCrowdDO) {
        Integer trolleyValue = customCrowdDO.getTrolleyValue();
        if (trolleyValue == null) {
            return null;
        }
        return getMemeberIdsByEvent(trolleyValue,EventType.ADD_CART,customCrowdDO.getBusinessId());
    }

    private List<Long> buildMemberBehaviorVisit(CustomCrowdDO customCrowdDO) {
        Integer visitValue = customCrowdDO.getVisitValue();
        if (visitValue == null) {
            return null;
        }
        return getMemeberIdsByEvent(visitValue,EventType.IN_STORE,customCrowdDO.getBusinessId());


    }

    private List<Long> getMemeberIdsByEvent(Integer visitValue, EventType eventType, Long businessId) {
        Set<Long> result = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        // 起始时间：7天前的00:00:00（如今天是2025-08-14，则start为2025-08-08 00:00:00）
        LocalDateTime end = now.minusDays(0)  // 减6天 = 包含今天在内的第7天
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // 结束时间：今天的23:59:59
        LocalDateTime start = now.minusDays(visitValue)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        List<Long> longs = eventService.queryMemberIDs(eventType, start, end, businessId);
        result.addAll(longs);
        return new ArrayList<>(result);
    }

    /**
     * 计算memberBasicList中不包含在memberBehaviorVisitSet中的元素
     * @param memberBasicList 源列表
     * @param memberBehaviorVisitSet 排除集合
     * @return 差集结果（memberBasicList - memberBehaviorVisitSet）
     */
    public static List<Long> getListSetDifference(List<Long> memberBasicList, List<Long> memberBehaviorVisitSet) {
        // 源列表为空时，返回空列表
        if (CollectionUtil.isEmpty(memberBasicList)) {
            return new ArrayList<>();
        }

        // 排除集合为空时，直接返回源列表的副本
        if (CollectionUtil.isEmpty(memberBehaviorVisitSet)) {
            return new ArrayList<>(memberBasicList);
        }

        // 将排除列表转换为HashSet以提高contains()方法的性能
        Set<Long> excludeSet = new HashSet<>(memberBehaviorVisitSet);

        // 过滤出源列表中不在排除集合中的元素
        return memberBasicList.stream()
                .filter(id -> !excludeSet.contains(id))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAllNullOrZero(CustomCrowdDO crowdDO) {
        // 基础信息、行为、分析均为null
        boolean allNull = crowdDO.getBasicInfo() == null
                && crowdDO.getCustomBehavior() == null
                && crowdDO.getCustomAnalysis() == null;

        // 基础信息、行为、分析均为0（假设类型为数字类型，如Integer）
        boolean allZero = crowdDO.getBasicInfo() != null && crowdDO.getBasicInfo() == 0
                && crowdDO.getCustomBehavior() != null && crowdDO.getCustomBehavior() == 0
                && crowdDO.getCustomAnalysis() != null && crowdDO.getCustomAnalysis() == 0;

        return allNull || allZero;
    }


    private void crowdMemberRefWrite(List<List<Long>> resultGroup, Long crowdId) {
        if (resultGroup == null || resultGroup.isEmpty()) {
            return;
        }
        // 清除会员人群redis
        redisCache.deleteObject("member_crowd_"+crowdId);

        //        redisCache.deleteObject("member_crowd_"+crowdId);
        // 1. 使用 UNLINK 异步删除所有 10 个 shard key（不阻塞 Redis 主线程）
        List<String> shardKeys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            shardKeys.add("member_crowd:" + crowdId + "_shard_" + i);
        }
        redisCache.batchUnlinkKeys(shardKeys);

        // 数据库与redis 人群信息写入
        for(List<Long> eles : resultGroup){
            if (eles == null || eles.isEmpty()) {
                return;
            }
            List<MemberCrowdRefDO> memberCrowdRefList = buildList(eles, crowdId);

            Map<String, String> MemberCrowdRefMap = convertToMap(memberCrowdRefList);

            // mysql 人群会员关系 批量写入
            wxMemberCrowdRefService.batchWrite(memberCrowdRefList);

            // redis 人群会员关系 批量写入
            // redisCache.batchHash(String.valueOf("member_crowd_"+crowdId), MemberCrowdRefMap);
            // 3. 按 memberId % 10 分组，写入对应 shard
            Map<Integer, Map<String, String>> shardMaps = new HashMap<>();
            for (MemberCrowdRefDO ref : memberCrowdRefList) {
                int shardIndex = (int) (ref.getMemberId() % 10);
                shardMaps.computeIfAbsent(shardIndex, k -> new HashMap<>())
                        .put(ref.getMemberId().toString(), "1");
            }
            // 写入各 shard
            for (Map.Entry<Integer, Map<String, String>> entry : shardMaps.entrySet()) {
                String shardKey = "member_crowd:" + crowdId + "_shard_" + entry.getKey();
                redisCache.batchHash(shardKey, entry.getValue());
            }
        }
    }

    /**
     * 将MemberCrowdRefDO列表转换为Map<memberId, 1>
     * 若存在重复的memberId，保留第一个
     *
     * @param memberCrowdRefList 源列表
     * @return 转换后的Map，key为memberId，value固定为1
     */
    public static Map<String, String> convertToMap(List<MemberCrowdRefDO> memberCrowdRefList) {
        if (memberCrowdRefList == null || memberCrowdRefList.isEmpty()) {
            return new HashMap<>(); // 返回空Map（可修改）
            // 若需不可修改的空Map，可使用 Collections.emptyMap()
        }

        // 流式处理：提取memberId并转为字符串作为key，value固定为"1"
        return memberCrowdRefList.stream()
                .collect(Collectors.toMap(
                        // key：将memberId转换为字符串
                        ref -> ref.getMemberId().toString(),
                        // value：固定为字符串"1"
                        ref -> "1",
                        // 处理重复key：保留第一个出现的记录
                        (existingValue, newValue) -> existingValue
                ));
    }

    /**
     * 将memberId列表与固定crowdId组合，构建MemberCrowdRefDO列表
     * @param eles 成员ID列表
     * @param crowdId 固定的人群ID
     * @return 组合后的实体列表
     */
    public List<MemberCrowdRefDO> buildList(List<Long> eles, Long crowdId) {
        // 处理空列表，避免NPE
        if (eles == null || eles.isEmpty()) {
            return List.of(); // 返回空列表（Java 9+，不可修改）
            // 若需兼容旧版本，可返回 new ArrayList<>()
        }

        // 流处理：遍历每个memberId，构建MemberCrowdRefDO对象
        return eles.stream()
                .map(memberId -> {
                    MemberCrowdRefDO refDO = new MemberCrowdRefDO();
                    // id通常由数据库生成，这里可不设置（或根据业务需求设置）
                    refDO.setMemberId(memberId);
                    refDO.setCrowdId(crowdId);
                    return refDO;
                })
                .toList();
    }

    /**
     *
     *
     * @param sourceList
     * @return
     */
    public static List<List<Long>> splitIntoGroupsWithStream(List<Long> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        int groupSize = 2000;
        // 按索引分组：每个元素的索引 / 2000 即为组编号
        return IntStream.range(0, sourceList.size())
                .boxed()
                .collect(Collectors.groupingBy(
                        index -> index / groupSize, // 分组键：索引除以2000取整数
                        Collectors.mapping(
                                sourceList::get, // 映射为具体元素
                                Collectors.toList() // 收集为子列表
                        )
                ))
                .values() // 提取所有分组的子列表
                .stream().toList();
    }

    /**
     * 求三个列表的交集，空集合不参与计算
     * 若所有列表都为空，返回空列表
     * 若只有一个非空列表，返回该列表
     * 若有两个或三个非空列表，返回它们的交集
     */
    public static List<Long> findCommonElements(
            List<Long> memberBasicList,
            List<Long> memberBehaviorList,
            List<Long> memberAnalysisList) {

        // 过滤空列表，只保留非空集合
        List<List<Long>> nonEmptyLists = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(memberBasicList)) {
            nonEmptyLists.add(memberBasicList);
        }
        if (CollectionUtil.isNotEmpty(memberBehaviorList)) {
            nonEmptyLists.add(memberBehaviorList);
        }
        if (CollectionUtil.isNotEmpty(memberAnalysisList)) {
            nonEmptyLists.add(memberAnalysisList);
        }

        // 处理特殊情况
        if (nonEmptyLists.isEmpty()) {
            return new ArrayList<>(); // 所有列表都为空
        }
        if (nonEmptyLists.size() == 1) {
            return new ArrayList<>(nonEmptyLists.get(0)); // 只有一个非空列表，直接返回其副本
        }

        // 对非空列表按大小排序（从小到大），优化交集计算效率
        nonEmptyLists.sort((list1, list2) -> Integer.compare(list1.size(), list2.size()));

        // 以最小的列表为基础，逐步与其他列表求交集
        Set<Long> resultSet = new HashSet<>(nonEmptyLists.get(0));
        for (int i = 1; i < nonEmptyLists.size(); i++) {
            Set<Long> currentSet = new HashSet<>(nonEmptyLists.get(i));
            resultSet.retainAll(currentSet); // 与当前列表取交集

            // 提前退出：如果中间结果为空，后续计算无需进行
            if (resultSet.isEmpty()) {
                break;
            }
        }

        return new ArrayList<>(resultSet);
    }



    /**
     * 按照客户分析ES结果，获取memberId
     *
     * @return
     * @throws IOException
     */
    private List<Long> getAnalysisList(CustomCrowdDO customCrowdDO) throws IOException {
        Set<Long> memberIds = new HashSet<>();
        Long lastMemberId = null; // 记录上一页最后一个memberId，作为下一页的after参数
        while (true) {
            // 1. 执行单次查询（传入上一页的最后一个memberId作为after参数）
            SearchResponse<Object> response = searchOrders(lastMemberId,customCrowdDO);
            if(response == null){
                break;
            }
            Map<String, Aggregate> topLevelAggs = response.aggregations();
            if (topLevelAggs == null || !topLevelAggs.containsKey("users")) {
                break; // 无聚合结果，退出循环
            }

            // 2. 解析当前页的composite聚合结果
            CompositeAggregate compositeAgg = topLevelAggs.get("users").composite();
            List<CompositeBucket> buckets = compositeAgg.buckets().array();
            if (buckets.isEmpty()) {
                break; // 没有更多桶，退出循环
            }

            // 3. 处理当前页的结果
            for (CompositeBucket bucket : buckets) {
                // 提取memberId
                Long memberId = bucket.key().get("memberId").longValue();
                // 封装结果
                memberIds.add(memberId);
            }

            // 4. 更新下一页的after参数（当前页最后一个memberId）
            CompositeBucket lastBucket = buckets.get(buckets.size() - 1);
            lastMemberId = lastBucket.key().get("memberId").longValue();

            // 5. 若没有下一页的游标，退出循环（afterKey为null表示已到末尾）
            if (compositeAgg.afterKey() == null) {
                break;
            }
        }

        return new ArrayList<>(memberIds);
    }

    /**
     * 直接使用原生客户端构建查询，避免Spring Data聚合容器的类型问题
     */
    public SearchResponse<Object> searchOrders(Long afterMemberId,CustomCrowdDO customCrowdDO) throws IOException {
        // 分析人群中客户分析 构建ES
        if (customCrowdDO.getCustomAnalysis() == null || customCrowdDO.getCustomAnalysis() == 0) {
            return null;
        }
        BoolQuery boolQuery;

        // 统计日期
        DateTimeFormatter rangeQueryFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        // 定义输入格式（yyyy/MM/dd）
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // 定义输出格式（yyyy-MM-dd'T'HH:mm:ss）
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        Integer censusDateType = customCrowdDO.getCensusDateType();
        String start = null;
        String end = null;
        if(censusDateType != null && censusDateType != 0){
            String censusDateValue = customCrowdDO.getCensusDateValue();
            if (censusDateType == 1) {
                // 指定时间 0 近7天    1 近30天  2 近180天半年
                int count = switch (censusDateValue) {
                    case "0" -> 7;
                    case "1" -> 30;
                    case "2" -> 180;
                    default -> 0;
                };
                if (count>0) {
                    start = toRangeQueryFormat(count, rangeQueryFormatter);
                    end = toRangeQueryFormat(0, rangeQueryFormatter);
                }
            }else if (censusDateType == 2){
                // 自定义时间 如2025/05/01 转格式yyyy-MM-dd'T'HH:mm:ss
                String censusDateValueStart = censusDateValue.split("-")[0];
                String censusDateValueEnd = censusDateValue.split("-")[1];
                start = convertToStartDateTime(censusDateValueStart, inputFormatter, outputFormatter);
                end = convertToEndDateTime(censusDateValueEnd, inputFormatter, outputFormatter);

            }

        }

        // 购买品类
        String purchaseCategory = customCrowdDO.getPurchaseCategory();
        // 订单ES:产品为单品（isSingle=1） 2 套餐    人群 1单品 2套餐
        String isSingle;
        if (purchaseCategory != null) {
            isSingle = "1".equals(purchaseCategory)?"1":"2".equals(purchaseCategory)?"2":null;
        } else {
            isSingle = null;
        }
        boolQuery = buildBoolQuery(start, end, isSingle, customCrowdDO.getBusinessId());


        // 2. 定义统计聚合（用于计算订单指标）
        // 2.1 订单总数（value_count）
        Aggregation totalOrdersAgg = Aggregation.of(a -> a
                .cardinality(c -> c
                        .field("orderId") // 对订单ID去重计数
                        .precisionThreshold(10000) // 小数量级（≤100次）计数误差可忽略
                        .missing(0)
                )
        );
        // 2.2 总金额（sum）
        //
        Aggregation totalAmountAgg = Aggregation.of(a -> a
                .sum(s -> s
                        .field("orderAmount")
                        .missing(0)
                        /*.script(Script.of(s1 -> s1 // 新增：脚本过滤，仅累加金额>0的订单
                                .lang("painless")
                                .source("doc['orderAmount'].value > 0 ? doc['orderAmount'].value : 0")
                        ))*/
                )
        );
        // 2.3 平均金额（avg）
        /*Aggregation avgAmountAgg = Aggregation.of(a -> a
                .avg(avg -> avg.field("orderAmount").missing(0))
        );*/

        // 3. 构建filtered_orders聚合（过滤订单并绑定统计指标）
        // 3.1 过滤条件（与顶层查询条件部分重复，用于聚合内二次筛选）
        Map<String, Aggregation> filteredSubAggs = new HashMap<>();
        BoolQuery filterBool = buildFilterBool(start, end, isSingle, filteredSubAggs, customCrowdDO.getBusinessId());

        // 3.3 处理聚合：isSingle场景下，删除商品维度的金额统计，改用订单级统计
        if (StringUtils.isNotBlank(isSingle) && ("1".equals(isSingle) || "2".equals(isSingle))) {
            Aggregation nestedAgg = Aggregation.of(a -> a
                    .nested(n -> n.path("product"))
                    .aggregations(Map.of(
                            "product_filter", Aggregation.of(f -> f
                                    .filter(TermQuery.of(t -> t
                                            .field("product.isSingle")
                                            .value(isSingle)
                                    )._toQuery())
                                    .aggregations(Map.of(
                                            "total_orders", Aggregation.of(c -> c
                                                    .cardinality(card -> card
                                                            .field("orderId")
                                                            .precisionThreshold(10000) // 同步提升精度，与非isSingle场景一致
                                                            .missing(0)
                                                    )
                                            ),
                                            "total_amount", Aggregation.of(s -> s
                                                    .sum(sum -> sum
                                                            .field("orderAmount")
                                                            .missing(0)
                                                            // 新增：脚本过滤，仅累加金额>0的订单（与非isSingle场景一致）
                                                            /*.script(Script.of(s1 -> s1
                                                                    .lang("painless")
                                                                    .source("doc['orderAmount'].value > 0 ? doc['orderAmount'].value : 0")
                                                            ))*/
                                                    )
                                            )
                                    ))
                            )
                    ))
            );
            filteredSubAggs.put("nested_filter", nestedAgg);
        } else {
            filteredSubAggs.put("total_orders", totalOrdersAgg);
            filteredSubAggs.put("total_amount", totalAmountAgg);
        }
        // 3.4 最终构建filtered_orders聚合
        Aggregation filteredOrdersAgg = Aggregation.of(a -> a
                .filter(filterBool._toQuery())
                .aggregations(filteredSubAggs) // 绑定子聚合（含或不含nested_filter）
        );

        // 4. 构建having聚合（bucket_selector，动态调整路径）
        Aggregation havingAgg = null;
        String scriptSource = buildScriptSource(customCrowdDO);
        // 4. 构建having聚合时，修正isSingle场景的totalAmount路径
        if (StringUtils.isNotBlank(scriptSource)) {
            Map<String, String> bucketsPathMap = new HashMap<>();
            if (StringUtils.isNotBlank(isSingle) && ("1".equals(isSingle) || "2".equals(isSingle))) {
                // 订单数：来自nested筛选后的订单（含目标商品的订单）
                bucketsPathMap.put("totalOrders", "filtered_orders>nested_filter>product_filter>total_orders");
                bucketsPathMap.put("totalAmount", "filtered_orders>nested_filter>product_filter>total_amount"); // 改为nested内的总金额
            } else {
                // 非isSingle场景路径不变
                bucketsPathMap.put("totalOrders", "filtered_orders>total_orders");
                bucketsPathMap.put("totalAmount", "filtered_orders>total_amount");
            }

            havingAgg = Aggregation.of(a -> a
                    .bucketSelector(bs -> bs
                            .bucketsPath(bp -> bp.dict(bucketsPathMap))
                            .script(Script.of(s -> s
                                    .lang("painless")
                                    .source(scriptSource)
                            ))
                    )
            );
        }

        // 5. 构建composite聚合（按memberId分组，实现分页查询）
        CompositeAggregationSource memberSource = CompositeAggregationSource.of(s -> s
                .terms(t -> t
                        .field("memberId")  // 按memberId字段分组
                        .missingBucket(false)  // 排除无memberId的记录
                        .valueType(ValueType.Long)
                )
        );
        Map<String, CompositeAggregationSource> sources = Collections.singletonMap("memberId", memberSource);

        // 5.1 绑定子聚合到composite（filtered_orders统计 + having过滤）
        Map<String, Aggregation> compositeSubAggs = new HashMap<>();
        compositeSubAggs.put("filtered_orders", filteredOrdersAgg);  // 统计聚合
        if (havingAgg != null) {
            compositeSubAggs.put("having", havingAgg);                   // 桶过滤
        }



        Aggregation usersAgg;
        // 区分第一次
        if(null == afterMemberId){
            usersAgg = Aggregation.of(a -> a
                    .composite(c -> c
                            .size(1000)
                            .sources(sources)
                    )
                    .aggregations(compositeSubAggs)  // 绑定子聚合
            );
        }else{
            usersAgg = Aggregation.of(a -> a
                    .composite(c -> c
                            .size(1000)
                            .sources(sources)
                            .after(
                                    Map.of("memberId", FieldValue.of(afterMemberId)) // 分页游标
                            )
                    )
                    .aggregations(compositeSubAggs)  // 绑定子聚合
            );
        }


        // 6. 构建顶级聚合
        Map<String, Aggregation> topLevelAggs = new HashMap<>();
        topLevelAggs.put("users", usersAgg);  // 顶级聚合名为users

        // 7. 构建查询请求
        SearchRequest request = SearchRequest.of(s -> s
                .index("bz_order")  // 目标索引
                .query(boolQuery._toQuery())  // 应用基础查询条件
                .aggregations(topLevelAggs)   // 应用顶级聚合
                .size(0)  // 不需要返回文档，只需要聚合结果
                .trackTotalHits(t -> t.enabled(false))  // 禁用总命中数计算（提升性能）
                .timeout("120s")  // 设置超时时间
        );

        // 8. 执行查询并返回结果
        return esClient.search(request, Object.class);
    }

    /**
     * 构建聚合脚本
     *
     * @param customCrowdDO
     * @return
     */
    private String buildScriptSource(CustomCrowdDO customCrowdDO) {
        String orderScript = "";
        String orderFrequencyScript = null;
        String orderAmountScript = null;
        String orderAvgScript = null;
        List<String> basicScripts = new ArrayList<>();

        // 新增：所有查询共享的“有效用户”基础条件（必须添加，确保口径一致）
        basicScripts.add("params.totalOrders >= 1"); // 至少1个有效订单
        basicScripts.add("params.totalAmount > 0");  // 总金额>0（与顶层过滤呼应）
        // 1.下单频次
        Integer orderFrequencyType = customCrowdDO.getOrderFrequencyType();
        if (orderFrequencyType != null && orderFrequencyType != 0) {
            String orderFrequencyValue = customCrowdDO.getOrderFrequencyValue();
            if (orderFrequencyType == 1) {
                // 区间
                int min = Integer.parseInt(orderFrequencyValue.split("-")[0]);
                int max = Integer.parseInt(orderFrequencyValue.split("-")[1]);
                int minFrequency = Math.min(min, max);
                int maxFrequency = Math.max(min, max);
                orderFrequencyScript = "params.totalOrders >= " + minFrequency + " && params.totalOrders <= "+ maxFrequency;

            } else if (orderFrequencyType ==2 ) {
                // 小于
                orderFrequencyScript = "params.totalOrders < "+ orderFrequencyValue;
            } else if (orderFrequencyType == 3) {
                // 大于
                orderFrequencyScript = "params.totalOrders > "+ orderFrequencyValue;
            } else {
                orderFrequencyScript = null;
            }
        }
        // 2.购买金额
        Integer orderAmountType = customCrowdDO.getOrderAmountType();
        if (orderAmountType != null && orderAmountType != 0) {
            String orderAmountValue = customCrowdDO.getOrderAmountValue();
            if (orderAmountType == 1) {
                // 区间
                double min = Double.parseDouble(orderAmountValue.split("-")[0]);
                double max = Double.parseDouble(orderAmountValue.split("-")[1]);
                double minAmount = Math.min(min, max);
                double maxAmount= Math.max(min, max);
                orderAmountScript = "params.totalAmount >= " + minAmount + " && params.totalAmount <= "+ maxAmount;

            } else if (orderAmountType ==2 ) {
                // 小于
                orderAmountScript = "params.totalAmount < "+ orderAmountValue;
            } else if (orderAmountType == 3) {
                // 大于
                orderAmountScript = "params.totalAmount > "+ orderAmountValue;
            } else {
                orderAmountScript = null;
            }
        }
        // 3.单均消费金额（动态计算：totalAmount / totalOrders，而非依赖avg聚合）
        Integer avgAmountType = customCrowdDO.getAvgAmountType();
        if (avgAmountType != null && avgAmountType != 0) {
            String avgAmountValue = customCrowdDO.getAvgAmountValue();
            // 关键：用 (params.totalAmount / params.totalOrders) 计算单均金额
            // 单均金额 = 订单级总金额 / 订单数（与商品数量无关）
            if (avgAmountType == 1) {
                double min = Double.parseDouble(avgAmountValue.split("-")[0]);
                double max = Double.parseDouble(avgAmountValue.split("-")[1]);
                double minAvg = Math.min(min, max);
                double maxAvg = Math.max(min, max);
                orderAvgScript = "params.totalOrders != 0 && params.totalOrders >= 1 && (params.totalAmount / params.totalOrders) >= " + minAvg + " && (params.totalAmount / params.totalOrders) <= " + maxAvg;
            } else if (avgAmountType == 2) {
                orderAvgScript = "params.totalOrders != 0 && params.totalOrders >= 1 && (params.totalAmount / params.totalOrders) < " + avgAmountValue;
            } else if (avgAmountType == 3) {
                orderAvgScript = "params.totalOrders != 0 && params.totalOrders >= 1 && (params.totalAmount / params.totalOrders) > " + avgAmountValue;
            }
        }
        // "params.totalOrders > 1 && params.totalAmount > 1 && params.avgAmount > 1"
        return joinScriptsWithAnd(orderFrequencyScript, orderAmountScript, orderAvgScript, basicScripts);
    }

    private BoolQuery buildFilterBool(String start, String end, String isSingle, Map<String, Aggregation> filteredSubAggs, Long businessId) {
        List<Query> filterQueries = new ArrayList<>();

        // 新增：businessId=10过滤条件
        filterQueries.add(TermQuery.of(t -> t
                .field("businessId")
                .value(businessId)
        )._toQuery());
        // 仅保留“金额>0”的聚合内过滤（顶层查询已处理其他条件，避免双重过滤）
        /*filterQueries.add(RangeQuery.of(r -> r
                .number(n -> n
                        .field("orderAmount")
                        .gt(0.0) // 仅聚合金额>0的订单，与totalAmountAgg脚本对齐
                )
        )._toQuery());*/
        // 新增：核心字段存在性校验（与顶层对齐）
        filterQueries.add(ExistsQuery.of(e -> e.field("orderId"))._toQuery());
        filterQueries.add(ExistsQuery.of(e -> e.field("orderAmount"))._toQuery());
        filterQueries.add(ExistsQuery.of(e -> e.field("createTime"))._toQuery());
        filterQueries.add(ExistsQuery.of(e -> e.field("orderState"))._toQuery());

        // 1. 新增：排除无效memberId（与顶层buildBoolQuery完全对齐）
        filterQueries.add(BoolQuery.of(innerBool -> innerBool
                .must(ExistsQuery.of(e -> e.field("memberId"))._toQuery())
                .mustNot(
                        BoolQuery.of(orBool -> orBool
                                .should(TermQuery.of(t -> t.field("memberId").value("0"))._toQuery())
                                .should(TermQuery.of(t -> t.field("memberId").value(0))._toQuery())
                                .minimumShouldMatch("1")
                        )._toQuery()
                )
        )._toQuery());

        // 2. 原有过滤逻辑（订单状态、时间、isSingle）保持不变
        filterQueries.add(TermsQuery.of(t -> t
                .field("orderState")
                .terms(ts -> ts.value(Arrays.asList(
                        FieldValue.of(20), FieldValue.of(30), FieldValue.of(40),
                        FieldValue.of(50), FieldValue.of(60), FieldValue.of(80), FieldValue.of(200)
                )))
        )._toQuery());

        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            filterQueries.add(RangeQuery.of(r -> r
                    .date(d -> d
                            .field("createTime")
                            .gte(start)
                            .lte(end)
                            .format("strict_date_optional_time")
                    )
            )._toQuery());
        }

        if (StringUtils.isNotBlank(isSingle) && ("1".equals(isSingle) || "2".equals(isSingle))) {
            filterQueries.add(NestedQuery.of(n -> n
                    .path("product")
                    .query(TermQuery.of(t -> t
                            .field("product.isSingle")
                            .value(isSingle)
                    )._toQuery())
            )._toQuery());
        }

        return BoolQuery.of(fb -> fb.must(filterQueries));
    }

    // 在 buildBoolQuery 的所有分支中，统一添加 memberId 过滤
    private BoolQuery buildBoolQuery(String start, String end, String isSingle, Long businessId) {
        List<Query> baseQueries = new ArrayList<>();
        // 新增：businessId=10过滤条件
        baseQueries.add(TermQuery.of(t -> t
                .field("businessId")
                .value(businessId)
        )._toQuery());
        // 新增：核心字段存在性校验（排除脏数据）
        baseQueries.add(ExistsQuery.of(e -> e.field("orderId"))._toQuery()); // 必须有订单ID
        baseQueries.add(ExistsQuery.of(e -> e.field("orderAmount"))._toQuery()); // 必须有金额
        baseQueries.add(ExistsQuery.of(e -> e.field("createTime"))._toQuery()); // 必须有创建时间
        baseQueries.add(ExistsQuery.of(e -> e.field("orderState"))._toQuery()); // 必须有订单状态

        // 原有逻辑：订单状态、memberId过滤、时间范围、isSingle筛选...
        baseQueries.add(TermsQuery.of(t -> t
                .field("orderState")
                .terms(ts -> ts.value(Arrays.asList(
                        FieldValue.of(20), FieldValue.of(30), FieldValue.of(40),
                        FieldValue.of(50), FieldValue.of(60), FieldValue.of(80), FieldValue.of(200)
                )))
        )._toQuery());
        // 排除无 memberId 或 memberId 为 null 的订单（统一添加）
        baseQueries.add(BoolQuery.of(innerBool -> innerBool
                .must(ExistsQuery.of(e -> e.field("memberId"))._toQuery())
                // 同时排除“字符串0”和“数字0”
                .mustNot(
                        BoolQuery.of(orBool -> orBool
                                .should(TermQuery.of(t -> t.field("memberId").value("0"))._toQuery())
                                .should(TermQuery.of(t -> t.field("memberId").value(0))._toQuery())
                                .minimumShouldMatch("1")
                        )._toQuery()
                )
        )._toQuery());

        // 2. 追加时间筛选（若有）
        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            baseQueries.add(RangeQuery.of(r -> r
                    .date(d -> d
                            .field("createTime")
                            .gte(start)
                            .lte(end)
                            .format("strict_date_optional_time")
                    )
            )._toQuery());
        }

        // 3. 追加 isSingle 筛选（若有，通过 nested 查询）
        if (StringUtils.isNotBlank(isSingle) && ("1".equals(isSingle) || "2".equals(isSingle))) {
            // 若筛选商品类型，还需校验product字段存在
            baseQueries.add(ExistsQuery.of(e -> e.field("product"))._toQuery());
            baseQueries.add(NestedQuery.of(n -> n
                    .path("product")
                    .query(TermQuery.of(t -> t
                            .field("product.isSingle")
                            .value(isSingle)
                    )._toQuery())
            )._toQuery());
        }

        return BoolQuery.of(b -> b.must(baseQueries));
    }

    /**
     * 将计算后的LocalDateTime转换为RangeQuery所需的格式
     * @param count 天数参数（用于计算count-1天前的0点）
     * @return 格式为yyyy-MM-dd'T'HH:mm:ss的字符串
     */
    public static String toRangeQueryFormat(Integer count, DateTimeFormatter formatter) {
        // 计算目标时间：count-1天前的0点0分0秒
        LocalDateTime targetTime = LocalDateTime.now()
                .minusDays(count)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // 转换为strict_date_optional_time格式（如2025-08-11T00:00:00）
        return targetTime.format(formatter);
    }

    /**
     * 将yyyy/MM/dd格式的日期转换为yyyy-MM-dd'T'00:00:00
     * @param inputDate 输入日期字符串，如"2025/05/01"
     * @return 转换后的日期时间字符串，如"2025-05-01T00:00:00"
     */
    public static String convertToStartDateTime(String inputDate, DateTimeFormatter inputFormatter, DateTimeFormatter outFormatter) {
        // 解析为日期
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
        // 转换为当天开始时间（00:00:00）
        LocalDateTime startOfDay = date.atStartOfDay();
        // 格式化为目标字符串
        return startOfDay.format(outFormatter);
    }

    /**
     * 将yyyy/MM/dd格式的日期转换为yyyy-MM-dd'T'23:59:59
     * @param inputDate 输入日期字符串，如"2025/08/31"
     * @return 转换后的日期时间字符串，如"2025-08-31T23:59:59"
     */
    public static String convertToEndDateTime(String inputDate, DateTimeFormatter inputFormatter, DateTimeFormatter outFormatter) {
        // 解析为日期
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
        // 转换为当天结束时间（23:59:59）
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        // 格式化为目标字符串
        return endOfDay.format(outFormatter);
    }

    public static String joinScriptsWithAnd(String orderFrequencyScript, String orderAmountScript, String orderAvgScript, List<String> basicScripts) {
        // 1. 先复制基础条件（确保有效用户基础规则必生效）
        List<String> allScripts = new ArrayList<>(basicScripts);

        // 2. 追加非空的业务条件（确保各查询的筛选规则生效）
        if (orderFrequencyScript != null && !orderFrequencyScript.trim().isEmpty()) {
            allScripts.add(orderFrequencyScript);
        }
        if (orderAmountScript != null && !orderAmountScript.trim().isEmpty()) {
            allScripts.add(orderAmountScript);
        }
        if (orderAvgScript != null && !orderAvgScript.trim().isEmpty()) {
            allScripts.add(orderAvgScript);
        }

        // 3. 用"&&"拼接所有条件（若全为空则返回空字符串，避免语法错误）
        return allScripts.isEmpty() ? "" : String.join(" && ", allScripts);
    }


    @Override
    public void updateMemberGroup() {
        for(int i = 0; i < 10; i++){
            // 重置为都没加入
            wxMemberService.resetCommunityFlag(i);
            // 根据关系表设置是否加入
            wxMemberService.setCommunityFlag(i);
        }
    }
}
