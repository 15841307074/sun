package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 一级会员卡用户券归档常量。
 *
 * <p>自然日保留范围、任务停止时间、单批事务大小和物理分表数量统一放在这里，
 * 后续调整策略时不需要修改任务流程代码。</p>
 */
public final class MemberCardBenefitCouponArchiveConstants {

    /**
     * 在线表保留最近的自然日数量。
     *
     * <p>当前值为 7 天。归档任务会清理“当前日期减 7 天”当天及更早的数据：
     * 例如 2026-08-17 执行时，清理 2026-08-10 全天及更早的数据。</p>
     */
    public static final long ARCHIVE_RETENTION_DAYS = 7L;

    /**
     * 归档任务判断每日停止时间时使用的时区。
     */
    public static final ZoneId ARCHIVE_TIME_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 归档任务每天允许运行到的时间。
     *
     * <p>达到 05:30 后，本次任务停止继续归档；未处理的数据由第二天的 XXL-Job 继续处理。</p>
     */
    public static final LocalTime ARCHIVE_DAILY_STOP_TIME = LocalTime.of(5, 30);

    /**
     * 单次备份并删除的用户券数量。
     *
     * <p>每 2000 条开启一个独立小事务，兼顾归档吞吐量和单次事务大小。</p>
     */
    public static final int ARCHIVE_BATCH_SIZE = 2000;

    /**
     * 用户券在线表和备份表的物理分表数量。
     *
     * <p>对应 user_coupon_0 ~ user_coupon_9 和 user_coupon_bak_0 ~ user_coupon_bak_9。</p>
     */
    public static final int USER_COUPON_SHARD_COUNT = 10;

    private MemberCardBenefitCouponArchiveConstants() {
    }
}
