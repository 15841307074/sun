package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

/**
 * 一级会员卡用户券归档编排服务。
 *
 * <p>负责 Redis 券 ID、XXL-Job 分片和物理分表之间的整体调度；
 * 实际的单批备份与删除由 {@link UserCouponArchiveChunkService} 完成。</p>
 */
public interface MemberCardBenefitCouponArchiveService {

    /**
     * 将发放超过归档时间的一级会员卡用户券完整备份后，从在线分表删除。
     *
     * @param businessId 业务线 ID，用于读取对应 Redis Set 并限制数据库归档范围
     * @param shardIndex XXL-Job 当前分片索引
     * @param shardTotal XXL-Job 分片总数
     * @return 当前执行器分片本次成功归档并删除的用户券数量
     */
    long archiveExpiredCoupons(Long businessId, int shardIndex, int shardTotal);
}
