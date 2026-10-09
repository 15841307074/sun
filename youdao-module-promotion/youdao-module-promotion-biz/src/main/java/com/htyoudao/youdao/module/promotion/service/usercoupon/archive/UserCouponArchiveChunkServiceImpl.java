package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponArchiveMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券单批归档服务实现。
 *
 * <p>一个方法调用就是一个独立小事务，事务内严格按照以下顺序执行：</p>
 * <ol>
 *     <li>锁定一批满足条件的在线用户券。</li>
 *     <li>完整写入对应的备份分表。</li>
 *     <li>校验实际写入数量。</li>
 *     <li>删除在线表中的同一批来源记录，并校验删除数量。</li>
 *     <li>任一步异常都会整体回滚。</li>
 * </ol>
 */
@Service
@DS(DsNameConstants.MASTER)
public class UserCouponArchiveChunkServiceImpl implements UserCouponArchiveChunkService {

    private static final int MIN_SHARD = 0;
    private static final int MAX_SHARD = 9;

    private final UserCouponArchiveMapper userCouponArchiveMapper;

    public UserCouponArchiveChunkServiceImpl(UserCouponArchiveMapper userCouponArchiveMapper) {
        this.userCouponArchiveMapper = userCouponArchiveMapper;
    }

    @Override
    @DataPermission(enable = false)
    @Transactional(rollbackFor = Exception.class)
    public int archiveChunk(Long businessId, Integer shard, Long couponId,
                            LocalDateTime cutoff, Integer batchSize) {
        // 第一步：校验所有查询条件，特别是动态表名使用的 shard 必须限制在 0~9。
        validateArguments(businessId, shard, couponId, cutoff, batchSize);

        /*
         * 第二步：从在线分表中选取并锁定一批待归档记录。
         * SQL 使用 FOR UPDATE，保证当前事务完成前其他归档事务不能同时处理同一批记录。
         */
        List<Long> sourceIds = userCouponArchiveMapper.selectEligibleSourceIds(
                shard, businessId, couponId, cutoff, batchSize);
        if (sourceIds == null || sourceIds.isEmpty()) {
            return 0;
        }

        // 第三步：将来源记录的全部字段写入备份表，并额外保存 source_user_coupon_id。
        int expectedCount = sourceIds.size();
        int insertedCount = userCouponArchiveMapper.insertBackupBySourceIds(shard, sourceIds);

        // 第四步：先校验 INSERT 返回的写入数量，数量不一致时立即抛错并回滚，绝不删除来源记录。
        if (insertedCount != expectedCount) {
            throw new IllegalStateException(String.format(
                    "用户券归档写入数量不一致，shard=%d, couponId=%d, expected=%d, inserted=%d",
                    shard, couponId, expectedCount, insertedCount));
        }

        // 第五步：删除在线表中的同一批记录；同时重复业务线、券 ID 和时间条件，避免扩大删除范围。
        int deletedCount = userCouponArchiveMapper.deleteSourceByIds(
                shard, businessId, couponId, cutoff, sourceIds);

        // 第六步：校验删除数量。只要少删或多删，整个小事务都回滚，备份和删除不会出现半完成状态。
        if (deletedCount != expectedCount) {
            throw new IllegalStateException(String.format(
                    "用户券归档删除数量不一致，shard=%d, couponId=%d, expected=%d, deleted=%d",
                    shard, couponId, expectedCount, deletedCount));
        }
        return deletedCount;
    }

    @Override
    @DataPermission(enable = false)
    @Transactional(rollbackFor = Exception.class)
    public int archiveExpiredChunk(Integer shard, LocalDateTime expirationTime, Integer batchSize) {
        validateCommonArguments(shard, expirationTime, batchSize);

        List<Long> sourceIds = userCouponArchiveMapper.selectExpiredSourceIds(
                shard, expirationTime, batchSize);
        if (sourceIds == null || sourceIds.isEmpty()) {
            return 0;
        }

        int expectedCount = sourceIds.size();
        int insertedCount = userCouponArchiveMapper.insertBackupBySourceIds(shard, sourceIds);
        if (insertedCount != expectedCount) {
            throw new IllegalStateException(String.format(
                    "过期用户券归档写入数量不一致，shard=%d, expected=%d, inserted=%d",
                    shard, expectedCount, insertedCount));
        }

        int deletedCount = userCouponArchiveMapper.deleteExpiredSourceByIds(
                shard, expirationTime, sourceIds);
        if (deletedCount != expectedCount) {
            throw new IllegalStateException(String.format(
                    "过期用户券归档删除数量不一致，shard=%d, expected=%d, deleted=%d",
                    shard, expectedCount, deletedCount));
        }
        return deletedCount;
    }

    /**
     * 校验单批归档参数，防止非法业务条件和动态表名进入 Mapper。
     */
    private static void validateArguments(Long businessId, Integer shard, Long couponId,
                                          LocalDateTime cutoff, Integer batchSize) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("用户券归档 businessId 必须为正数");
        }
        if (shard == null || shard < MIN_SHARD || shard > MAX_SHARD) {
            throw new IllegalArgumentException("用户券归档分片必须在 0-9 范围内");
        }
        if (couponId == null || couponId <= 0) {
            throw new IllegalArgumentException("用户券归档 couponId 必须为正数");
        }
        if (cutoff == null) {
            throw new IllegalArgumentException("用户券归档截止时间不能为空");
        }
        if (batchSize == null || batchSize <= 0) {
            throw new IllegalArgumentException("用户券归档批次大小必须为正数");
        }
    }

    private static void validateCommonArguments(Integer shard, LocalDateTime expirationTime,
                                                Integer batchSize) {
        if (shard == null || shard < MIN_SHARD || shard > MAX_SHARD) {
            throw new IllegalArgumentException("用户券归档分片必须在 0-9 范围内");
        }
        if (expirationTime == null) {
            throw new IllegalArgumentException("用户券归档过期截止时间不能为空");
        }
        if (batchSize == null || batchSize <= 0 || batchSize > 5000) {
            throw new IllegalArgumentException("用户券归档批次大小必须在 1-5000 范围内");
        }
    }
}
