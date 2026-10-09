package com.htyoudao.youdao.module.promotion.job.activitymz;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzExpiredLockReconciler;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 满赠库存定时补偿任务。 */
@Slf4j
@Component
public class ActivityMzInventoryJob {

    @Resource
    private ActivityMzExpiredLockReconciler reconciler;

    /** XXL-JOB 建议 Cron：0 * * * * ?，即每分钟执行一次。 */
    @XxlJob("activityMzExpiredInventoryReconcileJobHandler")
    @DataPermission(enable = false)
    public void reconcileExpiredInventory() {
        ActivityMzExpiredLockReconciler.ReconcileResult result = reconciler.reconcileExpiredLocks();
        log.info("满赠过期库存补偿完成 result={}", result);
        XxlJobHelper.log("满赠过期库存补偿完成 result={}", result);
    }
}
