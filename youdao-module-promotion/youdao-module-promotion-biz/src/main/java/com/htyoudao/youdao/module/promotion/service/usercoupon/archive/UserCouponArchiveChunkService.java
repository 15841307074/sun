package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import java.time.LocalDateTime;

/**
 * 用户优惠券单批归档 Service。
 *
 * <p>每次调用只处理一张物理分表、一个 couponId 和一个有限批次，
 * 以独立事务保证“完整备份、校验成功、删除来源”三者的原子性。</p>
 */
public interface UserCouponArchiveChunkService {

    /**
     * 原子归档一个批次：锁定来源、写入备份并校验写入数量、删除来源并校验删除数量。
     *
     * @param businessId 业务线 ID
     * @param shard 物理分表编号，范围 0-9
     * @param couponId 优惠券 ID
     * @param cutoff 仅归档 coupon_create_time 早于该时间的用户券
     * @param batchSize 本批最大处理数量
     * @return 实际归档并从来源表删除的数量
     */
    int archiveChunk(Long businessId, Integer shard, Long couponId,
                     LocalDateTime cutoff, Integer batchSize);

    /**
     * 按 expiration_time 原子归档一个批次，不限制业务线和券模板。
     *
     * @param shard 物理分表编号，范围 0-9
     * @param expirationTime 归档 expiration_time 小于等于该时间的记录
     * @param batchSize 本批最大处理数量
     * @return 实际归档并从在线表删除的数量
     */
    int archiveExpiredChunk(Integer shard, LocalDateTime expirationTime, Integer batchSize);
}
