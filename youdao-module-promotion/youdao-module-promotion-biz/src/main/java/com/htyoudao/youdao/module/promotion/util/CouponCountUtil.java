package com.htyoudao.youdao.module.promotion.util;


import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_STORE_NUM_ERROR;

/**
 * @author dht
 * 优惠券计数工具类
 */
public class CouponCountUtil {

    /**
     * 初始化门店优惠券数量
     * @param couponId 优惠券ID
     * @param storeQuantities 门店和数量映射
     */
    public static void initCouponStoreQuantities(RedisTemplate<String,Object> redisTemplate,
                                                 Long couponId,
                                                 Map<Long, Integer> storeQuantities) {
        String totalKey = GoodCouponConstants.COUPON_STORE_NUM + couponId;

        // 转换Map<Long, Long>为Map<String, String>以适应Redis存储
        Map<String, String> redisMap = new HashMap<>(8);
        storeQuantities.forEach((storeId, count) ->
                redisMap.put(storeId.toString(), count.toString())
        );
        redisTemplate.opsForHash().putAll(totalKey, redisMap);
    }

    /**
     * 初始化门店优惠券数量
     * @param couponId 优惠券ID
     * @param storeQuantities 门店和数量映射
     */
    public static void initCouponClaimNum(RedisTemplate<String,Object> redisTemplate,
                                                 Long couponId,
                                                 Map<Long, Integer> storeQuantities) {
        String totalKey = GoodCouponConstants.COUPON_CLAIMED_NUM + couponId;

        // 转换Map<Long, Long>为Map<String, String>以适应Redis存储
        Map<String, String> redisMap = new HashMap<>(8);
        storeQuantities.forEach((storeId, count) ->
                redisMap.put(storeId.toString(), count.toString())
        );
        redisTemplate.opsForHash().putAll(totalKey, redisMap);
    }

    /**
     * 判断优惠券门店数量是否存在
     * @param redisTemplate redisTemplate
     * @param couponId couponId
     * @return Boolean
     */
    public static Boolean couponHasKey(RedisTemplate<String,Object> redisTemplate,Long couponId) {
        String totalKey = GoodCouponConstants.COUPON_STORE_NUM + couponId;
        return redisTemplate.hasKey(totalKey);
    }


    /**
     * 设置优惠券详情
     * @param redisTemplate redisTemplate
     * @param id 优惠券ID
     * @param goodCouponDO 优惠券基本信息
     */
    public static void setCouponDataDetail(RedisTemplate<String, Object> redisTemplate,
                                           Long id,
                                           GoodCouponDO goodCouponDO) {
        String key = GoodCouponConstants.COUPON_DETAIL + id;
        redisTemplate.opsForValue().set(key, goodCouponDO);
    }

    /**
     * 获取所有门店的优惠券数量
     * @param redisTemplate redisTemplate
     * @param couponId couponId
     * @return Map<Long, Integer>
     */
    public static Map<Long, Integer> getAllData(RedisTemplate<String, Object> redisTemplate,Long couponId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(GoodCouponConstants.COUPON_CLAIMED_NUM + couponId);
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        // String转Long
                        e -> (Long.parseLong(e.getKey().toString())),
                        // String转Integer
                        e -> (Integer) e.getValue()
                ));
    }

    /**
     * 获取所有门店的优惠券数量
     * @param couponId 优惠券ID
     * @return 门店和数量映射
     */
    public Map<Long, Integer> getAllStoreQuantities(RedisTemplate<String, Object> redisTemplate,String couponId) {
        String totalKey = GoodCouponConstants.COUPON_STORE_NUM + couponId;

        Map<Object, Object> redisMap = redisTemplate.opsForHash().entries(totalKey);
        Map<Long, Integer> result = new HashMap<>(8);

        redisMap.forEach((key, value) -> {
            convertToLongPair(key, value).ifPresent(pair -> {
                result.put(pair.getLeft(), pair.getRight());
            });
        });

        return result;
    }

    /**
     * 获取指定门店的剩余数量
     *
     * @param redisTemplate redis
     * @param couponRespVO  门店ID
     */
    public static void getRemainingQuantity(RedisTemplate<String, Object> redisTemplate, GoodCouponRespVO couponRespVO) {
        String claimedKey = GoodCouponConstants.COUPON_CLAIMED_NUM + couponRespVO.getId();
        Long storeId = couponRespVO.getStoreId();
        if(ObjectUtil.isEmpty(storeId)){
            return;
        }
        // 获取总量
        Integer total = couponRespVO.getStoreLimitNum();
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
     * 获取指定门店已经领取的数量
     * @param couponId 优惠券ID
     * @param storeId 门店ID
     * @return 剩余数量
     */
    public Long getClaimedQuantity(RedisTemplate<String, Object> redisTemplate,String couponId, Long storeId) {
        String claimedKey = GoodCouponConstants.COUPON_CLAIMED_NUM + couponId;
        // 获取已领取量
        Object claimedObj = redisTemplate.opsForHash().get(claimedKey, storeId.toString());
        return (claimedObj == null) ? 0L : Long.parseLong(claimedObj.toString());
    }

    /**
     * 获取指定门店的优惠券总数
     * @param couponId 优惠券ID
     * @param storeId 门店ID
     * @return 总数
     */
    public static Long getTotalQuantity(RedisTemplate<String, Object> redisTemplate,String couponId, Long storeId) {
        String claimedKey = GoodCouponConstants.COUPON_STORE_NUM + couponId;
        // 总数
        Object claimedObj = redisTemplate.opsForHash().get(claimedKey, storeId.toString());
        return (claimedObj == null) ? 0L : Long.parseLong(claimedObj.toString());
    }

    /**
     * 设置指定门店优惠券领取数量 3天后过期
     * @param couponId 优惠券ID
     */
    public static void expireThreeDays(RedisTemplate<String, Object> redisTemplate,Long couponId) {
        String claimedKey = GoodCouponConstants.COUPON_CLAIMED_NUM + couponId;
        redisTemplate.expire(claimedKey, 3, TimeUnit.DAYS);
    }

    /**
     * 领取优惠券计算
     * @param redisTemplate Redis 操作模板
     * @param couponId 优惠券ID
     * @param storeId 门店ID
     * @param num 领取数量
     * @param total 总数
     * @throws RuntimeException 如果库存不足
     */
    public static void claimCoupon(
            RedisTemplate<String, Object> redisTemplate,
            Long couponId,
            Long storeId,
            int num,
            int total
    ) {
        if (num <= 0) {
            throw new IllegalArgumentException("领取数量必须大于0");
        }

        String claimedKey = GoodCouponConstants.COUPON_CLAIMED_NUM + couponId;

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
     * 封装个转换 避免异常
     * @param key couponId
     * @param value storeId，count
     * @return storeId，count
     */
    private Optional<Pair<Long, Integer>> convertToLongPair(Object key, Object value) {
        try {
            return Optional.of(Pair.of(
                    Long.parseLong(key.toString()),
                    Integer.parseInt(value.toString())
            ));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
