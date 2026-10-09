package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import com.htyoudao.youdao.module.promotion.dal.redis.MemberCardBenefitRedisDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveConstants.ARCHIVE_BATCH_SIZE;
import static com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveConstants.ARCHIVE_DAILY_STOP_TIME;
import static com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveConstants.ARCHIVE_RETENTION_DAYS;
import static com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveConstants.ARCHIVE_TIME_ZONE;
import static com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveConstants.USER_COUPON_SHARD_COUNT;

/**
 * 一级会员卡用户券归档编排服务实现。
 *
 * <p>本服务只负责任务编排，不直接执行备份和删除 SQL：</p>
 * <ol>
 *     <li>从 Redis 读取一级会员实际发放过的优惠券 ID。</li>
 *     <li>按 Asia/Shanghai 自然日计算“七个自然日前整天及更早”的归档截止时间。</li>
 *     <li>根据 Pod 数量分配 user_coupon_0 ~ user_coupon_9，并让大表 1、3、5 优先单独运行。</li>
 *     <li>同一 Pod 内并行处理分配到的物理表，单表内部逐个 couponId 分批归档。</li>
 *     <li>每天达到 05:30 后结束本次任务，剩余数据由第二天继续处理。</li>
 * </ol>
 */
@Slf4j
@Service
public class MemberCardBenefitCouponArchiveServiceImpl implements MemberCardBenefitCouponArchiveService {

    private final MemberCardBenefitRedisDAO memberCardBenefitRedisDAO;

    private final UserCouponArchiveChunkService userCouponArchiveChunkService;

    private final MemberCardBenefitCouponArchiveProperties archiveProperties;

    public MemberCardBenefitCouponArchiveServiceImpl(
            MemberCardBenefitRedisDAO memberCardBenefitRedisDAO,
            UserCouponArchiveChunkService userCouponArchiveChunkService,
            MemberCardBenefitCouponArchiveProperties archiveProperties) {
        this.memberCardBenefitRedisDAO = memberCardBenefitRedisDAO;
        this.userCouponArchiveChunkService = userCouponArchiveChunkService;
        this.archiveProperties = archiveProperties;
    }

    @Override
    public long archiveExpiredCoupons(Long businessId, int shardIndex, int shardTotal) {
        // 第一步：先校验业务线和 XXL-Job 分片参数，避免非法分片导致动态表名越界。
        validateArguments(businessId, shardIndex, shardTotal);

        /*
         * 第二步：读取 Nacos 动态开关并判断任务启动时间。
         * 开关开启且已经达到每日 05:30 停止时间时，本次不再读取 Redis 或访问数据库；
         * 开关关闭时忽略时间限制，方便测试环境在白天继续验证归档功能。
         */
        boolean dailyStopEnabled = archiveProperties.isDailyStopEnabled();
        LocalTime taskStartTime = LocalTime.now(ARCHIVE_TIME_ZONE);
        if (dailyStopEnabled && hasReachedDailyStopTime(taskStartTime)) {
            log.info("一级会员卡用户券归档已达到每日停止时间，本次任务不再执行，businessId={}，"
                            + "分片索引={}，当前时间={}，停止时间={}，时区={}",
                    businessId, shardIndex, taskStartTime, ARCHIVE_DAILY_STOP_TIME, ARCHIVE_TIME_ZONE);
            return 0L;
        }

        /*
         * 第三步：分配当前 Pod 负责的物理表。
         * 1~5 个 Pod 保留原 memberCardBenefitJob 的 extracted 规则；
         * 6 个及以上 Pod 优先让数据量最大的 1、3、5 表各自单独运行，
         * 其余物理表均匀分配给剩余 Pod，最多使用前 10 个 Pod。
         */
        List<Integer> currentShards = resolvePhysicalShards(shardTotal, shardIndex);
        if (currentShards.isEmpty()) {
            log.info("一级会员卡用户券归档当前Pod无分配表，直接结束，businessId={}，分片索引={}，分片总数={}",
                    businessId, shardIndex, shardTotal);
            return 0L;
        }

        // 第四步：Redis Set 只保存一级会员实际发放成功过的 couponId，这里统一转换为 Long 并排序。
        List<Long> couponIds = parseCouponIds(memberCardBenefitRedisDAO.getLevelOneCouponIds(businessId));
        if (couponIds.isEmpty()) {
            log.info("一级会员卡用户券归档跳过，Redis 中没有券ID，businessId={}", businessId);
            return 0L;
        }

        /*
         * 第五步：按 Asia/Shanghai 自然日计算归档截止时间，并且在任务开始时只计算一次。
         * 先取“当前日期减 7 天”作为最后允许归档的日期，再取下一天零点作为 SQL 严格小于的截止值。
         * 例如 2026-08-17 执行时，cutoff 为 2026-08-11 00:00:00，
         * 因此会归档 2026-08-10 全天及更早的数据。
         */
        LocalDate archiveThroughDate = LocalDate.now(ARCHIVE_TIME_ZONE).minusDays(ARCHIVE_RETENTION_DAYS);
        LocalDateTime cutoff = archiveThroughDate.plusDays(1).atStartOfDay();
        log.info("一级会员卡用户券归档当前Pod分配表={}，线程数={}，businessId={}，分片索引={}",
                currentShards, currentShards.size(), businessId, shardIndex);

        /*
         * 第六步：创建当前 Pod 的共享停止标记。
         * 任一物理表线程达到每日停止时间后会设置该标记，
         * 其他物理表线程会在下一批事务开始前读取并停止。
         */
        AtomicBoolean stopRequested = new AtomicBoolean(false);

        /*
         * 第七步：每张物理表提交一个独立线程并行处理。
         * 当前 Pod 分到几张表就创建几个线程，例如分到 3 张表就是 3 个线程；
         * 线程内部仍按 couponId 和每批 ARCHIVE_BATCH_SIZE 条串行归档，
         * 不会在同一张物理表内产生批次并发。
         */
        ExecutorService shardExecutor = Executors.newFixedThreadPool(currentShards.size());
        List<PhysicalShardArchiveTask> archiveTasks = new ArrayList<>(currentShards.size());
        try {
            for (Integer physicalShard : currentShards) {
                Future<Long> future = shardExecutor.submit(() -> archivePhysicalShard(
                        businessId, shardIndex, physicalShard, currentShards, couponIds,
                        cutoff, stopRequested));
                archiveTasks.add(new PhysicalShardArchiveTask(physicalShard, future));
            }

            // 第八步：等待当前 Pod 的全部物理表完成，并汇总成功归档数量。
            long totalArchivedCount = 0L;
            IllegalStateException firstFailure = null;
            for (PhysicalShardArchiveTask archiveTask : archiveTasks) {
                try {
                    totalArchivedCount += archiveTask.future().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    archiveTasks.forEach(task -> task.future().cancel(true));
                    throw new IllegalStateException(String.format(
                            "一级会员卡用户券归档等待分片被中断，businessId=%d, physicalShard=%d",
                            businessId, archiveTask.physicalShard()), e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    log.error("一级会员卡用户券归档物理表执行失败，businessId={}，物理分片={}",
                            businessId, archiveTask.physicalShard(), cause);
                    if (firstFailure == null) {
                        firstFailure = new IllegalStateException(String.format(
                                "一级会员卡用户券归档物理表执行失败，businessId=%d, physicalShard=%d",
                                businessId, archiveTask.physicalShard()), cause);
                    }
                }
            }
            if (firstFailure != null) {
                throw firstFailure;
            }
            return totalArchivedCount;
        } finally {
            // 第九步：无论成功、达到停止时间或异常，都关闭当前 Pod 的物理表线程池。
            shardExecutor.shutdown();
        }
    }

    /**
     * 根据 XXL-Job 分片总数为当前 Pod 分配物理分表。
     *
     * <p>1~5 个 Pod 保留原 extracted 规则。6 个及以上 Pod 使用倾斜分配：</p>
     * <ol>
     *     <li>每个活跃 Pod 先处理与自身分片索引相同的物理表。</li>
     *     <li>剩余物理表依次补给非大表 Pod，数据量最大的 1、3、5 表始终单独运行。</li>
     *     <li>Pod 数量不超过 10 时全部参与；超过 10 时，多余 Pod 不分配物理表。</li>
     * </ol>
     *
     * <p>例如：</p>
     * <ul>
     *     <li>6 Pod：[0,6,9]、[1]、[2,7]、[3]、[4,8]、[5]</li>
     *     <li>7 Pod：[0,7]、[1]、[2,8]、[3]、[4,9]、[5]、[6]</li>
     *     <li>8 Pod：[0,8]、[1]、[2,9]、[3]、[4]、[5]、[6]、[7]</li>
     * </ul>
     */
    private static List<Integer> resolvePhysicalShards(int shardTotal, int shardIndex) {
        List<Integer> currentShards = new ArrayList<>();
        if (shardTotal == 1) {
            for (int shard = 0; shard < USER_COUPON_SHARD_COUNT; shard++) {
                currentShards.add(shard);
            }
        } else if (shardTotal == 2) {
            if (shardIndex == 0) {
                addShardRange(currentShards, 0, 4);
            } else if (shardIndex == 1) {
                addShardRange(currentShards, 5, 9);
            }
        } else if (shardTotal == 3) {
            if (shardIndex == 0) {
                addShardRange(currentShards, 0, 3);
            } else if (shardIndex == 1) {
                addShardRange(currentShards, 4, 6);
            } else if (shardIndex == 2) {
                addShardRange(currentShards, 7, 9);
            }
        } else if (shardTotal == 4) {
            if (shardIndex == 0) {
                addShardRange(currentShards, 0, 2);
            } else if (shardIndex == 1) {
                addShardRange(currentShards, 3, 5);
            } else if (shardIndex == 2) {
                addShardRange(currentShards, 6, 7);
            } else if (shardIndex == 3) {
                addShardRange(currentShards, 8, 9);
            }
        } else if (shardTotal == 5) {
            int start = shardIndex * 2;
            currentShards.add(start);
            currentShards.add(start + 1);
        } else {
            int activePodCount = Math.min(shardTotal, USER_COUPON_SHARD_COUNT);
            if (shardIndex >= activePodCount) {
                return currentShards;
            }

            // 第一步：每个活跃 Pod 先处理与分片索引相同的物理表。
            currentShards.add(shardIndex);

            // 数据量最大的 1、3、5 表不再追加其他物理表，确保它们各自单独运行。
            if (isLargePhysicalShard(shardIndex)) {
                return currentShards;
            }

            /*
             * 第二步：将尚未分配的物理表从小到大轮询补给非大表 Pod。
             * 例如 8 Pod 时，8、9 表分别追加到 Pod 0、Pod 2；
             * 分配结果中的每张物理表只出现一次，且 6~10 个 Pod 时所有 Pod 都会参与。
             */
            List<Integer> expandablePodIndexes = new ArrayList<>();
            for (int podIndex = 0; podIndex < activePodCount; podIndex++) {
                if (!isLargePhysicalShard(podIndex)) {
                    expandablePodIndexes.add(podIndex);
                }
            }
            for (int physicalShard = activePodCount;
                 physicalShard < USER_COUPON_SHARD_COUNT;
                 physicalShard++) {
                int ownerIndex = expandablePodIndexes.get(
                        (physicalShard - activePodCount) % expandablePodIndexes.size());
                if (ownerIndex == shardIndex) {
                    currentShards.add(physicalShard);
                }
            }
        }
        return currentShards;
    }

    /**
     * 判断物理表是否属于需要单独占用一个 Pod 的大表。
     */
    private static boolean isLargePhysicalShard(int physicalShard) {
        return physicalShard == 1 || physicalShard == 3 || physicalShard == 5;
    }

    /**
     * 将闭区间内的物理分表编号加入当前 Pod 的任务列表。
     */
    private static void addShardRange(List<Integer> currentShards, int start, int end) {
        for (int shard = start; shard <= end; shard++) {
            currentShards.add(shard);
        }
    }

    private long archivePhysicalShard(
            Long businessId, int shardIndex, int physicalShard, List<Integer> currentShards,
            List<Long> couponIds, LocalDateTime cutoff, AtomicBoolean stopRequested) {
        long shardArchivedCount = 0L;

        // 第一步：一张物理分表内逐个处理 Redis 中记录的 couponId，避免一次 SQL 同时扫描大量券。
        archiveLoop:
        for (Long couponId : couponIds) {
            /*
             * 第二步：每次只归档 ARCHIVE_BATCH_SIZE 条。
             * 返回数量等于批次上限，说明可能还有下一批，继续循环；
             * 返回数量小于批次上限，说明当前物理表、当前 couponId 已处理完。
             */
            while (true) {
                /*
                 * 每次执行归档事务前检查时间。
                 * 任一物理表发现已经达到 05:30 后设置当前 Pod 的共享停止标记，
                 * 其他物理表线程也会在各自下一次检查时结束，剩余数据留到第二天。
                 */
                if (shouldStopArchive(
                        businessId, shardIndex, currentShards, stopRequested)) {
                    break archiveLoop;
                }

                int archivedCount = userCouponArchiveChunkService.archiveChunk(
                        businessId, physicalShard, couponId, cutoff, ARCHIVE_BATCH_SIZE);

                /*
                 * archiveChunk 使用独立事务，成功返回表示本批数据已经完整写入备份表并从原表删除。
                 * 只有成功返回的数据才会进入当前物理表和 Job 最终归档数量。
                 */
                shardArchivedCount += archivedCount;
                if (archivedCount < ARCHIVE_BATCH_SIZE) {
                    break;
                }
            }
        }
        return shardArchivedCount;
    }

    /**
     * 判断当前 Pod 是否应该因达到每日 05:30 停止时间而结束。
     *
     * <p>每批事务开始前都会读取 Nacos 动态开关：</p>
     * <ol>
     *     <li>开关为 false 时忽略 05:30，任务继续处理。</li>
     *     <li>开关为 true 且达到 05:30 时设置共享停止标记。</li>
     *     <li>停止标记由当前 Pod 的全部物理表线程共享，首次停止时只输出一条 Pod 汇总日志。</li>
     * </ol>
     */
    private boolean shouldStopArchive(
            Long businessId, int shardIndex, List<Integer> currentShards,
            AtomicBoolean stopRequested) {
        if (stopRequested.get()) {
            return true;
        }

        // 动态配置关闭时完全忽略每日停止时间；下一批会再次读取，Nacos 修改后无需重启 Pod。
        if (!archiveProperties.isDailyStopEnabled()) {
            return false;
        }

        LocalTime currentTime = LocalTime.now(ARCHIVE_TIME_ZONE);
        if (!hasReachedDailyStopTime(currentTime)) {
            return false;
        }

        if (stopRequested.compareAndSet(false, true)) {
            log.info("一级会员卡用户券归档已达到每日停止时间，结束当前Pod本次任务，businessId={}，"
                            + "分片索引={}，负责物理表={}，当前时间={}，停止时间={}",
                    businessId, shardIndex, currentShards, currentTime,
                    ARCHIVE_DAILY_STOP_TIME);
        }
        return true;
    }

    /**
     * 当前时间达到或超过每日停止时间时返回 true。
     */
    private static boolean hasReachedDailyStopTime(LocalTime currentTime) {
        return !currentTime.isBefore(ARCHIVE_DAILY_STOP_TIME);
    }

    /**
     * 将 Redis Set 中的字符串券 ID 转换为可用于数据库查询的 Long 列表。
     *
     * <p>Redis 中的异常值只记录错误并跳过，不能因为一个脏值阻断其他正常券的归档。</p>
     */
    private List<Long> parseCouponIds(Set<String> redisCouponIds) {
        if (redisCouponIds == null || redisCouponIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> couponIds = new ArrayList<>(redisCouponIds.size());
        for (String redisCouponId : redisCouponIds) {
            try {
                Long couponId = Long.valueOf(redisCouponId);
                if (couponId > 0) {
                    couponIds.add(couponId);
                }
            } catch (NumberFormatException e) {
                log.error("一级会员卡用户券归档忽略非法 Redis 券ID，value={}", redisCouponId, e);
            }
        }
        couponIds.sort(Long::compareTo);
        return couponIds;
    }

    /**
     * 校验任务入口参数。
     *
     * <p>物理分表编号最终会拼接到 SQL 表名中，因此必须先保证 XXL-Job 分片参数合法。</p>
     */
    private void validateArguments(Long businessId, int shardIndex, int shardTotal) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("一级会员卡用户券归档 businessId 必须为正数");
        }
        if (shardTotal <= 0) {
            throw new IllegalArgumentException("一级会员卡用户券归档分片总数必须大于 0");
        }
        if (shardIndex < 0 || shardIndex >= shardTotal) {
            throw new IllegalArgumentException("一级会员卡用户券归档分片索引无效");
        }
    }

    /**
     * 保存物理分表编号及其异步执行结果，便于汇总数量和定位失败分表。
     */
    private record PhysicalShardArchiveTask(int physicalShard, Future<Long> future) {
    }
}
