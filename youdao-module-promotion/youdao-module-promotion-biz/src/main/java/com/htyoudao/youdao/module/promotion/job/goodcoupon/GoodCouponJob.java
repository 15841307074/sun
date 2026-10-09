package com.htyoudao.youdao.module.promotion.job.goodcoupon;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.archive.MemberCardBenefitCouponArchiveService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author dht
 */
@Slf4j
@Component
public class GoodCouponJob {

    @Resource
    private JobService jobService;

    @Resource
    private MemberCardBenefitCouponArchiveService memberCardBenefitCouponArchiveService;


    @XxlJob("scheduledSendMessage")
    public void memberDayCouponJobHandler() throws Exception {
        // 任务执行逻辑
        System.out.println("scheduledSendMessage is running.");
        jobService.scheduledSendMessage();
        XxlJobHelper.log("scheduledSendMessage is running.");
    }


    @XxlJob("nocCouponDataToRedis")
    public void nocCouponDataToRedis() throws Exception {
        // 任务执行逻辑
        System.out.println("nocCouponDataToRedis is running.");
        jobService.nocCouponDataToRedis();
        XxlJobHelper.log("nocCouponDataToRedis is running.");
    }



    @XxlJob("xxjobSpecifyDateCoupon")
    public void xxjobSpecifyDateCoupon() throws Exception {
        // 任务执行逻辑
        System.out.println("xxjobSpecifyDateCoupon is running.");
        jobService.xxjobSpecifyDateCoupon();
        XxlJobHelper.log("xxjobSpecifyDateCoupon is running.");
    }

    @XxlJob("memberCardBenefitJob")
    @DataPermission(enable = false)
    public void memberCardBenefitJob() {
        System.out.println("memberCardBenefitJob is running.");
        jobService.memberCardBenefitJob();
        XxlJobHelper.log("memberCardBenefitJob is running.");
    }

    /**
     * 一级会员卡用户券归档任务。
     *
     * <p>XXL-Job 配置说明：</p>
     * <ol>
     *     <li>Handler：memberCardBenefitCouponArchiveJob</li>
     *     <li>任务参数：业务线 ID（businessId）</li>
     *     <li>路由策略：分片广播，由各执行器共同处理 user_coupon_0 ~ user_coupon_9</li>
     * </ol>
     */
    @XxlJob("memberCardBenefitCouponArchiveJob")
    @DataPermission(enable = false)
    public void memberCardBenefitCouponArchiveJob() {
        // 第一步：读取任务参数。每个业务线使用独立的 Redis 券 ID 集合，归档时不能混用。
        String jobParam = XxlJobHelper.getJobParam();
        Long businessId = Long.parseLong(jobParam);

        // 第二步：读取 XXL-Job 分片信息，后续按分片索引分配 10 张 user_coupon 物理表。
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 第三步：记录任务开始信息，便于定位具体业务线和执行器分片。
        long startTime = System.currentTimeMillis();
        log.info("一级会员卡用户券归档任务开始，businessId={}，分片索引={}，分片总数={}",
                businessId, shardIndex, shardTotal);

        /*
         * 第四步：执行归档编排。
         * 服务内部负责读取 Redis 券 ID、按自然日计算七天前的归档截止时间、分配物理表并分批迁移；
         * 默认达到每日 05:30 后正常结束本次任务，测试环境可通过 Nacos 动态开关关闭该限制。
         */
        long archivedCount = memberCardBenefitCouponArchiveService.archiveExpiredCoupons(
                businessId, shardIndex, shardTotal);

        /*
         * 第五步：同时写入应用日志和 XXL-Job 日志。
         * 仅输出最终归档数量和总耗时。
         */
        long costMillis = System.currentTimeMillis() - startTime;
        log.info("一级会员卡用户券归档任务结束，businessId={}，分片索引={}，归档数量={}，耗时={}ms",
                businessId, shardIndex, archivedCount, costMillis);
        XxlJobHelper.log("一级会员卡用户券归档完成，businessId={}，分片索引={}，归档数量={}，耗时={}ms",
                businessId, shardIndex, archivedCount, costMillis);
    }

}
