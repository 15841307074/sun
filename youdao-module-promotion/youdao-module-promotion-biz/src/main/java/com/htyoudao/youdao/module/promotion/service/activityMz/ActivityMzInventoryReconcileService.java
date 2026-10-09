package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.ActivityMzRedisDAO;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 满赠有限库存Redis/MySQL只读对账服务。 */
@Service
public class ActivityMzInventoryReconcileService {

    @Resource
    private ActivityMzGiftMapper giftMapper;
    @Resource
    private ActivityMzRedisDAO redisDAO;

    /** 只报告差异，不自动覆盖任何一侧库存。 */
    public ReconcileResult reconcile(Long activityId) {
        LambdaQueryWrapper<ActivityMzGiftDO> query = new LambdaQueryWrapper<ActivityMzGiftDO>()
                .isNotNull(ActivityMzGiftDO::getActivityInventory)
                .orderByAsc(ActivityMzGiftDO::getActivityId, ActivityMzGiftDO::getStoreId,
                        ActivityMzGiftDO::getGiftCommodityId);
        if (activityId != null) query.eq(ActivityMzGiftDO::getActivityId, activityId);
        List<ActivityMzGiftDO> gifts = giftMapper.selectList(query);

        Map<String, List<ActivityMzGiftDO>> giftsByKey = new LinkedHashMap<>();
        for (ActivityMzGiftDO gift : gifts) {
            String key = gift.getStoreId() == null
                    ? redisDAO.buildSharedKey(gift.getActivityId())
                    : redisDAO.buildStoreKey(gift.getActivityId(), gift.getStoreId());
            giftsByKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(gift);
        }

        List<InventoryDifference> differences = new ArrayList<>();
        for (Map.Entry<String, List<ActivityMzGiftDO>> entry : giftsByKey.entrySet()) {
            List<ActivityMzGiftDO> keyGifts = entry.getValue();
            List<Long> giftIds = keyGifts.stream().map(ActivityMzGiftDO::getGiftCommodityId).toList();
            List<Integer> redisValues = redisDAO.getGiftInventories(entry.getKey(), giftIds);
            for (int i = 0; i < keyGifts.size(); i++) {
                ActivityMzGiftDO gift = keyGifts.get(i);
                Integer redisInventory = redisValues.get(i);
                if (!Objects.equals(gift.getRemainingInventory(), redisInventory)) {
                    InventoryDifference difference = new InventoryDifference();
                    difference.setActivityMzGiftId(gift.getId());
                    difference.setActivityId(gift.getActivityId());
                    difference.setStoreId(gift.getStoreId());
                    difference.setGiftCommodityId(gift.getGiftCommodityId());
                    difference.setMysqlInventory(gift.getRemainingInventory());
                    difference.setRedisInventory(redisInventory);
                    difference.setRedisKey(entry.getKey());
                    differences.add(difference);
                }
            }
        }

        ReconcileResult result = new ReconcileResult();
        result.setCheckedCount(gifts.size());
        result.setRedisKeyCount(giftsByKey.size());
        result.setDifferenceCount(differences.size());
        result.setDifferences(differences);
        return result;
    }

    @Data
    public static class ReconcileResult {
        private Integer checkedCount;
        private Integer redisKeyCount;
        private Integer differenceCount;
        private List<InventoryDifference> differences;
    }

    @Data
    public static class InventoryDifference {
        private Long activityMzGiftId;
        private Long activityId;
        private Long storeId;
        private Long giftCommodityId;
        private Integer mysqlInventory;
        private Integer redisInventory;
        private String redisKey;
    }
}
