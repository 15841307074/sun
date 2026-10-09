package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzParticipationDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftStockRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMz.ActivityMzMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMz.ActivityMzParticipationMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftStockRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;

@Service
@Slf4j
public class ActivityMzOrderService {
    /**
     * 库存/参与状态：提交锁定 → 支付消耗；取消释放锁定；退款恢复已消耗库存。
     */
    private static final int LOCKED = 1, CONSUMED = 2, RELEASED = 3, REFUNDED = 4;

    @Resource
    private ActivityMzCacheService cacheService;
    @Resource
    private ActivityMzMapper mzMapper;
    @Resource
    private ActivityMzGiftMapper giftMapper;
    @Resource
    private ActivityMzGiftStockRecordMapper stockRecordMapper;
    @Resource
    private ActivityMzParticipationMapper participationMapper;

    public boolean canParticipate(Long activityId, Long memberId) {
        // 点餐机不传有效 memberId，直接放行；只有锁定中和已支付记录占用参与次数。
        if (memberId == null || memberId <= 0) return true;

        return canParticipate(activityId, memberId, getParticipationMeta(activityId, memberId));
    }

    /** 一次加载并补齐参与校验所需元数据，锁库流程直接复用回源结果。 */
    private ActivityMzCacheService.ActivityMzMeta getParticipationMeta(Long activityId, Long memberId) {
        ActivityMzCacheService.ActivityMzMeta meta = cacheService.getActivityMetaPublic(activityId);
        if (meta == null || meta.getUserLimitType() == null) {
            log.error("canParticipate 缓存查询失效, activityId = {}, memberId = {}", activityId, memberId);

            // 兼容未初始化缓存及旧版本只包含库存类型的元数据，回源后补齐 Redis。
            ActivityMzDO config = mzMapper.selectOne(ActivityMzDO::getActivityId, activityId);
            if (config == null) return meta;
            cacheService.cacheActivityMeta(activityId, config.getGiftInventoryType(),
                    config.getUserLimitType(), config.getUserLimitValue());
            meta = new ActivityMzCacheService.ActivityMzMeta();
            meta.setActivityId(activityId);
            meta.setGiftInventoryType(config.getGiftInventoryType());
            meta.setUserLimitType(config.getUserLimitType() == null ? 0 : config.getUserLimitType());
            meta.setUserLimitValue(config.getUserLimitValue());
        }
        return meta;
    }

    private boolean canParticipate(Long activityId, Long memberId, ActivityMzCacheService.ActivityMzMeta meta) {
        if (meta == null || meta.getUserLimitType() == null || meta.getUserLimitType() == 0) return true;

        LambdaQueryWrapper<ActivityMzParticipationDO> query = new LambdaQueryWrapper<ActivityMzParticipationDO>()
                .eq(ActivityMzParticipationDO::getActivityId, activityId)
                .eq(ActivityMzParticipationDO::getMemberId, memberId)
                .in(ActivityMzParticipationDO::getStatus, LOCKED, CONSUMED);
        // 1=每天限制，只统计当天；2=活动期间总次数，统计活动下全部有效记录。
        if (meta.getUserLimitType() == 1) query.eq(ActivityMzParticipationDO::getParticipationDate, LocalDate.now());
        int limit = meta.getUserLimitValue() == null ? 0 : meta.getUserLimitValue();
        return limit <= 0 || participationMapper.selectCount(query) < limit;
    }

    @Transactional(rollbackFor = Exception.class)
    public int lock(Long activityId, Long storeId, Long memberId, String orderSn, Long giftCommodityId,
                    Long giftSkuId, int quantity, boolean checkUserLimit) {
        // orderSn + activityId + giftCommodityId 唯一，重复提交直接返回第一次实际锁定数量。
        ActivityMzGiftStockRecordDO existing = stockRecordMapper.selectOne(new LambdaQueryWrapper<ActivityMzGiftStockRecordDO>()
                .eq(ActivityMzGiftStockRecordDO::getOrderSn, orderSn)
                .eq(ActivityMzGiftStockRecordDO::getActivityId, activityId)
                .eq(ActivityMzGiftStockRecordDO::getGiftCommodityId, giftCommodityId));

        if (existing != null) return existing.getQuantity();
        boolean needsParticipationCheck = checkUserLimit && memberId != null && memberId > 0;
        ActivityMzCacheService.ActivityMzMeta meta = needsParticipationCheck
                ? getParticipationMeta(activityId, memberId)
                : cacheService.getActivityMetaPublic(activityId);
        if (needsParticipationCheck && !canParticipate(activityId, memberId, meta)) return 0;
        if (meta == null) return 0;
        // Redis Lua 原子扣减并按剩余库存截断，不会因为库存少于请求量而抛“库存不足”。
        ActivityMzCacheService.InventoryChangeResult changed = cacheService.changeGiftInventory(
                activityId, storeId, giftCommodityId, -quantity, meta);
        int actual = changed.isSuccess() ? changed.getRemainingInventory() : 0;
        if (actual <= 0) return 0;

        ActivityMzGiftDO gift = findGift(activityId, storeId, giftCommodityId, meta.getGiftInventoryType());
        if (gift == null && !Integer.valueOf(2).equals(meta.getGiftInventoryType())) {
            cacheService.changeGiftInventory(activityId, storeId, giftCommodityId, actual, meta);
            return 0;
        }
        // Redis负责高并发可用量，MySQL同时维护可用/锁定/消耗汇总；不限库存不维护库存汇总量。
        if (gift != null && gift.getActivityInventory() != null) {
            int updated = giftMapper.update(null, new LambdaUpdateWrapper<ActivityMzGiftDO>()
                    .setSql("remaining_inventory = GREATEST(COALESCE(remaining_inventory, activity_inventory) - " + actual + ", 0)")
                    .setSql("locked_inventory = COALESCE(locked_inventory, 0) + " + actual)
                    .eq(ActivityMzGiftDO::getId, gift.getId()));
            if (updated == 0) {
                cacheService.changeGiftInventory(activityId, storeId, giftCommodityId, actual, meta);
                return 0;
            }
        }

        ActivityMzGiftStockRecordDO record = new ActivityMzGiftStockRecordDO();
        record.setActivityId(activityId);
        // 动态门店仅维护 Redis 库存，流水不关联其他门店的赠品配置，支付/返还跳过数据库汇总。
        record.setActivityMzGiftId(gift != null ? gift.getId() : null);
        record.setStoreId(storeId);
        record.setOrderSn(orderSn);
        record.setMemberId(memberId);
        record.setGiftCommodityId(giftCommodityId);
        record.setGiftSkuId(giftSkuId);
        record.setQuantity(actual);
        record.setInventoryType(meta.getGiftInventoryType());
        record.setStatus(LOCKED);
        record.setLockExpireTime(LocalDateTime.now().plusMinutes(15));
        try {
            stockRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            // 并发重复提交：释放本次多锁的库存，返回已经成功的锁定结果。
            cacheService.changeGiftInventory(activityId, storeId, giftCommodityId, actual, meta);
            if (gift != null && gift.getActivityInventory() != null) giftMapper.update(null, new LambdaUpdateWrapper<ActivityMzGiftDO>()
                    .setSql("remaining_inventory = COALESCE(remaining_inventory,0) + " + actual)
                    .setSql("locked_inventory = GREATEST(COALESCE(locked_inventory,0) - " + actual + ",0)")
                    .eq(ActivityMzGiftDO::getId, gift.getId()));
            ActivityMzGiftStockRecordDO concurrent = stockRecordMapper.selectOne(new LambdaQueryWrapper<ActivityMzGiftStockRecordDO>()
                    .eq(ActivityMzGiftStockRecordDO::getOrderSn, orderSn).eq(ActivityMzGiftStockRecordDO::getActivityId, activityId)
                    .eq(ActivityMzGiftStockRecordDO::getGiftCommodityId, giftCommodityId));
            return concurrent == null ? 0 : concurrent.getQuantity();
        }

        // 同一订单无论循环赠送多少件都只创建一条参与记录，即只算一次参与。
        if (checkUserLimit && memberId != null && memberId > 0 && participationMapper.selectCount(
                new LambdaQueryWrapper<ActivityMzParticipationDO>().eq(ActivityMzParticipationDO::getActivityId, activityId)
                        .eq(ActivityMzParticipationDO::getMemberId, memberId).eq(ActivityMzParticipationDO::getOrderSn, orderSn)) == 0) {
            ActivityMzParticipationDO p = new ActivityMzParticipationDO();
            p.setActivityId(activityId);
            p.setMemberId(memberId);
            p.setOrderSn(orderSn);
            p.setParticipationDate(LocalDate.now());
            p.setStatus(LOCKED);
            participationMapper.insert(p);
        }
        return actual;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(String orderSn) {
        // 提交时可用库存已经减少；支付成功只把“锁定”转成“已消耗”，不能再次扣可用库存。
        List<ActivityMzGiftStockRecordDO> records = records(orderSn, LOCKED);
        for (ActivityMzGiftStockRecordDO r : records) {
            int updated = stockRecordMapper.update(null, new LambdaUpdateWrapper<ActivityMzGiftStockRecordDO>()
                    .set(ActivityMzGiftStockRecordDO::getStatus, CONSUMED).set(ActivityMzGiftStockRecordDO::getPaidTime, LocalDateTime.now())
                    .eq(ActivityMzGiftStockRecordDO::getId, r.getId()).eq(ActivityMzGiftStockRecordDO::getStatus, LOCKED));
            // 支付回调可能并发或重复到达，只有抢到状态转换的线程才能结转汇总库存。
            if (updated == 0) continue;
            if (r.getActivityMzGiftId() != null) {
                // 不限库存不维护锁定量和消耗量；直接在 UPDATE 中过滤，避免新增查询。
                giftMapper.update(null, new LambdaUpdateWrapper<ActivityMzGiftDO>()
                        .setSql("locked_inventory = GREATEST(COALESCE(locked_inventory,0) - " + r.getQuantity() + ", 0)")
                        .setSql("consumed_inventory = COALESCE(consumed_inventory,0) + " + r.getQuantity())
                        .isNotNull(ActivityMzGiftDO::getActivityInventory)
                        .eq(ActivityMzGiftDO::getId, r.getActivityMzGiftId()));
            }
        }
        participationMapper.update(null, new LambdaUpdateWrapper<ActivityMzParticipationDO>()
                .set(ActivityMzParticipationDO::getStatus, CONSUMED).eq(ActivityMzParticipationDO::getOrderSn, orderSn)
                .eq(ActivityMzParticipationDO::getStatus, LOCKED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean release(String orderSn) {
        return restore(orderSn, LOCKED, RELEASED, false);
    }

    @Transactional(rollbackFor = Exception.class)
    // 普通退款无论部分还是整单都恢复整笔赠品库存；只退赏金不会调用该入口。
    public boolean refund(String orderSn) {
        return restore(orderSn, CONSUMED, REFUNDED, true);
    }

    private boolean restore(String orderSn, int from, int to, boolean consumed) {
        for (ActivityMzGiftStockRecordDO r : records(orderSn, from)) {
            LambdaUpdateWrapper<ActivityMzGiftStockRecordDO> update = new LambdaUpdateWrapper<ActivityMzGiftStockRecordDO>()
                    .set(ActivityMzGiftStockRecordDO::getStatus, to)
                    .eq(ActivityMzGiftStockRecordDO::getId, r.getId()).eq(ActivityMzGiftStockRecordDO::getStatus, from);
            if (to == REFUNDED) {
                update.set(ActivityMzGiftStockRecordDO::getRefundedTime, LocalDateTime.now());
            } else {
                update.set(ActivityMzGiftStockRecordDO::getReleasedTime, LocalDateTime.now());
            }

            // 状态条件更新保证取消/退款重复通知时只恢复一次库存。
            int updated = stockRecordMapper.update(null, update);
            if (updated == 0) continue;
            cacheService.changeGiftInventory(r.getActivityId(), r.getStoreId(), r.getGiftCommodityId(), r.getQuantity());
            if (r.getActivityMzGiftId() != null) {
                // 与提交、支付确认保持一致，不限库存取消/退款时也不回写库存汇总。
                giftMapper.update(null, new LambdaUpdateWrapper<ActivityMzGiftDO>()
                        .setSql("remaining_inventory = COALESCE(remaining_inventory,0) + " + r.getQuantity())
                        .setSql((consumed ? "consumed_inventory" : "locked_inventory") + " = GREATEST(COALESCE(" +
                                (consumed ? "consumed_inventory" : "locked_inventory") + ",0) - " + r.getQuantity() + ",0)")
                        .isNotNull(ActivityMzGiftDO::getActivityInventory)
                        .eq(ActivityMzGiftDO::getId, r.getActivityMzGiftId()));
            }
        }
        participationMapper.update(null, new LambdaUpdateWrapper<ActivityMzParticipationDO>()
                .set(ActivityMzParticipationDO::getStatus, to).eq(ActivityMzParticipationDO::getOrderSn, orderSn)
                .eq(ActivityMzParticipationDO::getStatus, from));
        return true;
    }

    private List<ActivityMzGiftStockRecordDO> records(String orderSn, int status) {
        return stockRecordMapper.selectList(new LambdaQueryWrapper<ActivityMzGiftStockRecordDO>()
                .eq(ActivityMzGiftStockRecordDO::getOrderSn, orderSn).eq(ActivityMzGiftStockRecordDO::getStatus, status));
    }

    private ActivityMzGiftDO findGift(Long activityId, Long storeId, Long commodityId, Integer type) {
        LambdaQueryWrapper<ActivityMzGiftDO> q = new LambdaQueryWrapper<ActivityMzGiftDO>()
                .eq(ActivityMzGiftDO::getActivityId, activityId).eq(ActivityMzGiftDO::getGiftCommodityId, commodityId);
        if (Objects.equals(type, 2)) q.eq(ActivityMzGiftDO::getStoreId, storeId);
        else q.isNull(ActivityMzGiftDO::getStoreId);
        return giftMapper.selectOne(q);
    }
}
