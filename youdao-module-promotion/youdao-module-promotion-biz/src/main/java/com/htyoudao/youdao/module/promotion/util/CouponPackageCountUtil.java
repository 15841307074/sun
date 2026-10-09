package com.htyoudao.youdao.module.promotion.util;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_STORE_NUM_ERROR;

/**
 * @author dht
 * 优惠券包计算门店数量的工具类
 */
public class CouponPackageCountUtil {



    /**
     * 获取指定门店的优惠券包剩余数量
     * @param redisTemplate redis
     * @param couponPackage  券包
     */
    public static void getRemainingQuantity(RedisTemplate<String, Object> redisTemplate, CouponPackageDO couponPackage) {
        String claimedKey = GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + couponPackage.getId();
        Long storeId = couponPackage.getStoreId();
        if(ObjectUtil.isEmpty(storeId)){
            return;
        }
        // 获取总量
        Integer total = couponPackage.getStoreLimitNum();
        if (total == null) {
            return;
        }

        // 获取已领取量
        Object claimedObj = redisTemplate.opsForHash().get(claimedKey, storeId.toString());
        Long claimed = (claimedObj == null) ? 0L : Long.parseLong(claimedObj.toString());
        long receivedNum = total - claimed;
        if (receivedNum < 0){
            throw exception(COUPON_STORE_NUM_ERROR);
        }
    }

    /**
     * 设置指定门店优惠券包领取数量 3天后过期
     * @param couponId 优惠券ID
     */
    public static void expireThreeDays(RedisTemplate<String, Object> redisTemplate,Long couponId) {
        String claimedKey = GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + couponId;
        redisTemplate.expire(claimedKey, 3, TimeUnit.DAYS);
    }


    /**
     * 领取优惠券计算
     * @param redisTemplate Redis 操作模板
     * @param packageId 优惠券ID
     * @param storeId 门店ID
     * @param num 领取数量
     * @param total 总数
     * @throws RuntimeException 如果库存不足
     */
    public static void claimCoupon(
            RedisTemplate<String, Object> redisTemplate,
            Long packageId,
            Long storeId,
            int num,
            int total
    ) {
        if (num <= 0) {
            throw new IllegalArgumentException("领取数量必须大于0");
        }

        String claimedKey = GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + packageId;

        // 1. 原子性增加已领取数量
        Long claimedAfterIncr = redisTemplate.opsForHash().increment(claimedKey, storeId.toString(), num);

        // 2. 检查是否超卖
        if (claimedAfterIncr > total) {
            // 如果超卖，回滚（减少刚才增加的 num）
            redisTemplate.opsForHash().increment(claimedKey, storeId.toString(), -num);
            throw exception(COUPON_STORE_NUM_ERROR);
        }
    }

    /**
     * 获取所有门店的优惠券数量
     * @param redisTemplate redisTemplate
     * @param couponId couponId
     * @return Map<Long, Integer>
     */
    public static Map<Long, Integer> getAllData(RedisTemplate<String, Object> redisTemplate, Long couponId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + couponId);
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        // String转Long
                        e -> (Long.parseLong(e.getKey().toString())),
                        // String转Integer
                        e -> (Integer) e.getValue()
                ));
    }

    /**
     * 判断优惠券包门店数量是否存在
     * @param redisTemplate redisTemplate
     * @param couponId couponId
     * @return Boolean
     */
    public static Boolean packageHasKey(RedisTemplate<String,Object> redisTemplate,Long couponId) {
        String totalKey = GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + couponId;
        return redisTemplate.hasKey(totalKey);
    }

    /**
     * 初始化门店优惠券数量
     * @param packageId 优惠券包id
     * @param storeQuantities 门店和数量映射
     */
    public static void initCouponClaimNum(RedisTemplate<String,Object> redisTemplate,
                                          Long packageId,
                                          Map<Long, Integer> storeQuantities) {
        String totalKey = GoodCouponConstants.COUPON_PACKAGE_CLAIMED_NUM + packageId;

        // 转换Map<Long, Long>为Map<String, String>以适应Redis存储
        Map<String, String> redisMap = new HashMap<>(8);
        storeQuantities.forEach((storeId, count) ->
                redisMap.put(storeId.toString(), count.toString())
        );
        redisTemplate.opsForHash().putAll(totalKey, redisMap);
    }

}
