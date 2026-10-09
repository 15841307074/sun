package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftStockRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftStockRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 对账并补偿超过锁定期限、仍处于锁定状态的满赠库存。 */
@Slf4j
@Service
public class ActivityMzExpiredLockReconciler {

    private static final int LOCKED = 1;
    private static final int ORDER_CANCELED = 0;
    private static final int ORDER_UNPAID = 10;
    private static final int ORDER_REFUNDED = 70;
    private static final int BATCH_SIZE = 200;
    /** 在15分钟锁定期之后再留一分钟，避开支付回调与超时任务的临界点。 */
    private static final int SAFETY_DELAY_MINUTES = 1;

    @Resource
    private ActivityMzGiftStockRecordMapper stockRecordMapper;
    @Resource
    private ActivityMzOrderService orderService;
    @DubboReference
    private BzOrderApi bzOrderApi;

    /**
     * 单批对账。处理失败的流水仍保持 LOCKED，下次调度会自动重试。
     * 待支付订单不能由营销模块直接释放，否则订单稍后支付会形成赠品无库存占用。
     */
    public ReconcileResult reconcileExpiredLocks() {
        List<ActivityMzGiftStockRecordDO> records = stockRecordMapper.selectList(
                new LambdaQueryWrapper<ActivityMzGiftStockRecordDO>()
                        .eq(ActivityMzGiftStockRecordDO::getStatus, LOCKED)
                        .le(ActivityMzGiftStockRecordDO::getLockExpireTime,
                                LocalDateTime.now().minusMinutes(SAFETY_DELAY_MINUTES))
                        .orderByAsc(ActivityMzGiftStockRecordDO::getLockExpireTime)
                        .last("LIMIT " + BATCH_SIZE));

        // 一个订单可能包含多个赠品流水，订单状态只查一次，库存按 orderSn 整体处理。
        Map<String, ActivityMzGiftStockRecordDO> orders = new LinkedHashMap<>();
        for (ActivityMzGiftStockRecordDO record : records) {
            orders.putIfAbsent(record.getOrderSn(), record);
        }

        int confirmed = 0;
        int released = 0;
        int pending = 0;
        int failed = 0;
        for (ActivityMzGiftStockRecordDO record : orders.values()) {
            Long previousBusinessId = BusinessContextHolder.getBusinessId();
            try {
                BusinessContextHolder.setBusinessId(record.getBusinessId());
                Integer orderState = bzOrderApi.getOrderStateForMzInventory(record.getOrderSn());
                if (orderState == null || orderState == ORDER_CANCELED) {
                    // 订单不存在通常表示提交过程中锁库结果未知后已回滚，锁定库存同样应释放。
                    orderService.release(record.getOrderSn());
                    released++;
                } else if (orderState == ORDER_UNPAID) {
                    pending++;
                } else if (orderState == ORDER_REFUNDED) {
                    // 支付确认曾失败但订单已退款：先补确认，再恢复已消耗库存。
                    orderService.confirm(record.getOrderSn());
                    orderService.refund(record.getOrderSn());
                    confirmed++;
                } else {
                    orderService.confirm(record.getOrderSn());
                    confirmed++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("满赠过期锁定补偿失败，保留锁定等待重试 orderSn={}", record.getOrderSn(), ex);
            } finally {
                if (previousBusinessId == null) BusinessContextHolder.clear();
                else BusinessContextHolder.setBusinessId(previousBusinessId);
            }
        }
        return new ReconcileResult(orders.size(), confirmed, released, pending, failed);
    }

    public record ReconcileResult(int scanned, int confirmed, int released, int pending, int failed) {
    }
}
