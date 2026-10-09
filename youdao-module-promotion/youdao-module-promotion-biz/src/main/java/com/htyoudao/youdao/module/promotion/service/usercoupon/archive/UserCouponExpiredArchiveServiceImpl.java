package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveRespVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 历史过期用户券归档编排。
 *
 * <p>仅在不同物理表之间有限并发；同一张表内每次处理一个固定小批次，
 * 每批由 {@link UserCouponArchiveChunkService} 开启独立事务完成备份、校验和删除。</p>
 */
@Slf4j
@Service
public class UserCouponExpiredArchiveServiceImpl implements UserCouponExpiredArchiveService {

    private static final Pattern TABLE_PATTERN = Pattern.compile("^user_coupon_([0-9])$");
    private static final String ARCHIVE_LOCK_KEY = "promotion:user-coupon:expired-archive:lock";
    private static final int DEFAULT_BATCH_SIZE = 2000;
    private static final int DEFAULT_CONCURRENCY = 2;
    private static final long DEFAULT_BATCH_INTERVAL_MILLIS = 100L;

    private final UserCouponArchiveChunkService chunkService;
    private final RedissonClient redissonClient;

    public UserCouponExpiredArchiveServiceImpl(UserCouponArchiveChunkService chunkService,
                                               RedissonClient redissonClient) {
        this.chunkService = chunkService;
        this.redissonClient = redissonClient;
    }

    @Override
    public UserCouponExpiredArchiveRespVO archive(UserCouponExpiredArchiveReqVO reqVO) {
        ArchiveArguments arguments = validateAndNormalize(reqVO);
        RLock lock = redissonClient.getLock(ARCHIVE_LOCK_KEY);
        if (!lock.tryLock()) {
            throw new IllegalStateException("已有历史用户券归档任务正在执行，请勿重复启动");
        }

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(arguments.concurrency(), arguments.shards().size()));
        try {
            AtomicBoolean stopRequested = new AtomicBoolean(false);
            List<ShardTask> tasks = new ArrayList<>(arguments.shards().size());
            for (Integer shard : arguments.shards()) {
                Future<Long> future = executor.submit(() -> {
                    try {
                        return archiveShard(shard, arguments, stopRequested);
                    } catch (Exception e) {
                        // 失败线程立即通知其他分表，无需等待主线程按 Future 顺序发现异常。
                        stopRequested.set(true);
                        throw e;
                    }
                });
                tasks.add(new ShardTask(shard, future));
            }

            Map<String, Long> archivedCounts = new LinkedHashMap<>();
            long total = 0L;
            IllegalStateException firstFailure = null;
            for (ShardTask task : tasks) {
                try {
                    long count = task.future().get();
                    archivedCounts.put(tableName(task.shard()), count);
                    total += count;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tasks.forEach(item -> item.future().cancel(true));
                    throw new IllegalStateException("历史用户券归档被中断", e);
                } catch (ExecutionException e) {
                    // 通知其他分表在当前小事务结束后停止，并继续等待其退出，再释放全局锁。
                    stopRequested.set(true);
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    if (firstFailure == null) {
                        firstFailure = new IllegalStateException(
                                "历史用户券归档失败，table=" + tableName(task.shard()), cause);
                    }
                }
            }
            if (firstFailure != null) {
                throw firstFailure;
            }
            return new UserCouponExpiredArchiveRespVO(
                    archivedCounts, total, System.currentTimeMillis() - startTime);
        } finally {
            executor.shutdownNow();
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private long archiveShard(int shard, ArchiveArguments arguments, AtomicBoolean stopRequested) {
        String tableName = tableName(shard);
        long archivedTotal = 0L;
        long batchNumber = 0L;
        log.info("历史用户券归档分表开始，table={}，expirationTime={}，batchSize={}",
                tableName, arguments.expirationTime(), arguments.batchSize());

        while (!Thread.currentThread().isInterrupted() && !stopRequested.get()) {
            int archivedCount = chunkService.archiveExpiredChunk(
                    shard, arguments.expirationTime(), arguments.batchSize());
            archivedTotal += archivedCount;
            batchNumber++;

            // 避免每2000条产生一条INFO日志，只按固定批次数输出进度。
            if (batchNumber % 20 == 0) {
                log.info("历史用户券归档分表进度，table={}，archivedCount={}", tableName, archivedTotal);
            }
            if (archivedCount < arguments.batchSize()) {
                log.info("历史用户券归档分表完成，table={}，archivedCount={}", tableName, archivedTotal);
                return archivedTotal;
            }
            sleepBetweenBatches(arguments.batchIntervalMillis());
        }
        log.warn("历史用户券归档分表提前停止，table={}，archivedCount={}", tableName, archivedTotal);
        return archivedTotal;
    }

    private static void sleepBetweenBatches(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ArchiveArguments validateAndNormalize(UserCouponExpiredArchiveReqVO reqVO) {
        if (reqVO == null || reqVO.getExpirationTime() == null) {
            throw new IllegalArgumentException("过期截止时间不能为空");
        }
        if (reqVO.getTableNames() == null || reqVO.getTableNames().isEmpty()) {
            throw new IllegalArgumentException("归档表名不能为空");
        }

        LinkedHashSet<Integer> shards = new LinkedHashSet<>();
        for (String tableName : reqVO.getTableNames()) {
            Matcher matcher = TABLE_PATTERN.matcher(tableName == null ? "" : tableName.trim());
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "非法用户券物理表，只允许 user_coupon_0 至 user_coupon_9：" + tableName);
            }
            shards.add(Integer.parseInt(matcher.group(1)));
        }

        int batchSize = reqVO.getBatchSize() == null ? DEFAULT_BATCH_SIZE : reqVO.getBatchSize();
        int concurrency = reqVO.getConcurrency() == null ? DEFAULT_CONCURRENCY : reqVO.getConcurrency();
        long interval = reqVO.getBatchIntervalMillis() == null
                ? DEFAULT_BATCH_INTERVAL_MILLIS : reqVO.getBatchIntervalMillis();
        if (batchSize < 100 || batchSize > 5000) {
            throw new IllegalArgumentException("单批数量必须在100-5000范围内");
        }
        if (concurrency < 1 || concurrency > 3) {
            throw new IllegalArgumentException("分表并发数必须在1-3范围内");
        }
        if (interval < 0 || interval > 1000) {
            throw new IllegalArgumentException("批次间隔必须在0-1000毫秒范围内");
        }
        return new ArchiveArguments(List.copyOf(shards), reqVO.getExpirationTime(),
                batchSize, concurrency, interval);
    }

    private static String tableName(int shard) {
        return "user_coupon_" + shard;
    }

    private record ArchiveArguments(List<Integer> shards,
                                    java.time.LocalDateTime expirationTime,
                                    int batchSize,
                                    int concurrency,
                                    long batchIntervalMillis) {
    }

    private record ShardTask(int shard, Future<Long> future) {
    }
}
