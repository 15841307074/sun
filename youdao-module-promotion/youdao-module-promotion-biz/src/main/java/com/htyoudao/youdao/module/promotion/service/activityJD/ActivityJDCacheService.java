package com.htyoudao.youdao.module.promotion.service.activityJD;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponPackageReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDCouponReqSaveVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.JD_MEMBER_POINTS_COLLECT;

@Service
public class ActivityJDCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    
    // 缓存活动详情
    public void cacheActivity(ActivityJDFullRespVO activity, String redisKey) {
        String key = redisKey + activity.getId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(activity));
    }

    // 获取活动详情
    public ActivityJDFullRespVO getActivity(Long activityId, String redisKey) {
        String key = redisKey + activityId;
        String jsonStr = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(jsonStr)){
            return null;
        }
        return JSON.parseObject(jsonStr, ActivityJDFullRespVO.class);
    }
    
    // 删除缓存
    public void remove(String activityId, String redisKey) {
        String key = redisKey + activityId;
        redisTemplate.delete(key);
    }

    public void cacheStoreActivity(List<Long> activityStoreDO, Long activityId, String redisKey) {
        for (Long storeId : activityStoreDO){
            String activityStoreKey = redisKey + storeId;
            redisTemplate.opsForHash().put(activityStoreKey, String.valueOf(activityId), 1L);
        }

    }

    public void cacheActivityCoupon(List<ActivityJDCouponReqSaveVO> couponList, Long activityId, String redisKey) {
        // key 强制为 String 类型（将 Long 类型的 id 转为 String）
        Map<String, ActivityJDCouponReqSaveVO> couponMap = couponList.stream()
                .collect(Collectors.toMap(
                        // 将 id（Long）转为 String 作为 key
                        coupon -> String.valueOf(coupon.getId()),
                        // value 仍为对象本身
                        coupon -> coupon
                ));
        String activityCouponKey = redisKey + activityId;
        redisTemplate.opsForHash().putAll(activityCouponKey, couponMap);
    }

    /**
     * 通过 activityId 和 couponId 获取优惠券信息
     */
    public ActivityJDCouponReqSaveVO getActivityCoupon(Long activityId, Long couponId, String redisKey) {
        String activityCouponKey = redisKey + activityId;
        String couponKey = String.valueOf(couponId);
        Object couponObj = redisTemplate.opsForHash().get(activityCouponKey, couponKey);
        if (couponObj == null) {
            return null;
        }
        // 直接类型转换
        if (couponObj instanceof ActivityJDCouponReqSaveVO) {
            return (ActivityJDCouponReqSaveVO) couponObj;
        }
        return null;
    }

    /**
     * 通过 activityId 获取所有优惠券包信息
     */
    public Map<Long, ActivityJDCouponReqSaveVO> getAllActivityCoupon(Long activityId, String redisKey) {
        String activityCouponKey = redisKey + activityId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(activityCouponKey);

        return entries.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ActivityJDCouponReqSaveVO)
                .collect(Collectors.toMap(
                        entry -> Long.valueOf(entry.getKey().toString()), // String转Long
                        entry -> (ActivityJDCouponReqSaveVO) entry.getValue()
                ));
    }

    public void cacheActivityCouponPackage(List<ActivityJDCouponPackageReqSaveVO> couponPackageList, Long activityId, String redisKey) {
        // 将 key 从 Long 转为 String 类型
        Map<String, ActivityJDCouponPackageReqSaveVO> couponMap = couponPackageList.stream()
                .collect(Collectors.toMap(
                        // 核心：将 Long 类型的 id 转换为 String 作为 key
                        coupon -> String.valueOf(coupon.getId()),
                        // value 保持为对象本身
                        coupon -> coupon
                ));
        String activityCouponPackageKey = redisKey + activityId;
        redisTemplate.opsForHash().putAll(activityCouponPackageKey, couponMap);
    }

    public Integer getMemberPoints(Long activityId,Long memberId){
        Integer points = (Integer) redisTemplate.opsForHash().get(RedisKeyConstants.JD_MEMBER_POINTS_COLLECT + activityId, memberId.toString());
        return points == null ? 0 : points;
    }

    /**
     * 通过 activityId 和 couponId 获取优惠券包信息
     */
    public ActivityJDCouponPackageReqSaveVO getActivityCouponPackage(Long activityId, Long couponId, String redisKey) {
        String activityCouponKey = redisKey + activityId;
        String couponKey = String.valueOf(couponId);
        Object couponObj = redisTemplate.opsForHash().get(activityCouponKey, couponKey);
        // 直接类型转换
        if (couponObj instanceof ActivityJDCouponPackageReqSaveVO) {
            return (ActivityJDCouponPackageReqSaveVO) couponObj;
        }
        return null;
    }

    /**
     * 通过 activityId 获取所有优惠券包信息
     */
    public Map<Long, ActivityJDCouponPackageReqSaveVO> getAllActivityCouponPackages(Long activityId, String redisKey) {
        String activityCouponKey = redisKey + activityId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(activityCouponKey);

        return entries.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ActivityJDCouponPackageReqSaveVO)
                .collect(Collectors.toMap(
                        entry -> Long.valueOf(entry.getKey().toString()), // String转Long
                        entry -> (ActivityJDCouponPackageReqSaveVO) entry.getValue()
                ));
    }

    public void removeActivityStore(List<StoreInfoDTO> storeInfoDTOList, Long id, String redisKey) {
        for (StoreInfoDTO store : storeInfoDTOList) {
            String activityStoreKey = redisKey + store.getStoreId();
            redisTemplate.opsForHash().delete(activityStoreKey, id);
        }
    }

    public void batchRemoveActivityStore(List<StoreInfoDTO> storeInfoDTOList, Long id, String redisKeyPrefix) {
        List<String> activityStoreKeys = storeInfoDTOList.stream()
                .map(StoreInfoDTO::getStoreId)
                .filter(Objects::nonNull)
                .map(storeId -> redisKeyPrefix + storeId)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(activityStoreKeys)) {
            return;
        }

        // 使用 Pipeline 批量执行删除命令
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            // 获取字符串序列化器（确保与 RedisTemplate 配置的 key 序列化器一致）
            RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
            for (String hashKey : activityStoreKeys) {
                // 序列化 hashKey（字符串）
                byte[] hashKeyBytes = stringSerializer.serialize(hashKey);
                // 序列化 field（Long 转为字符串后序列化）
                byte[] fieldBytes = stringSerializer.serialize(String.valueOf(id));
                if (hashKeyBytes != null && fieldBytes != null) {
                    connection.hDel(hashKeyBytes, fieldBytes);
                }
            }
            return null;
        });
    }

    /**
     * member在某个集点活动中已经兑换的商品(券/包)
     * @param activityId activityId
     * @param memberId memberId
     * @return Set<String>
     */
    public Set<String> getMemberClaimedCoupon(Long activityId,Long memberId){
        String key = RedisKeyConstants.JD_ACTIVITY + activityId + RedisKeyConstants.JD_MEMBER + memberId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        assert members != null;
        return members.stream()
                .filter(Objects::nonNull)
                .map(obj -> String.valueOf(obj.toString()))
                .collect(Collectors.toSet());
    }

    /**
     * 向用户已领取优惠券集合中添加数据
     */
    public void removeMemberClaimedCoupon(Long activityId, Long memberId, Long couponId) {
        String key = RedisKeyConstants.JD_ACTIVITY + activityId + RedisKeyConstants.JD_MEMBER + memberId;
        redisTemplate.opsForSet().remove(key, couponId.toString());
    }

    /**
     * 向用户已领取优惠券集合中添加数据
     */
    public void addMemberClaimedCoupon(Long activityId, Long memberId, Long couponId) {
        String key = RedisKeyConstants.JD_ACTIVITY + activityId + RedisKeyConstants.JD_MEMBER + memberId;
        redisTemplate.opsForSet().add(key, couponId.toString());
        redisTemplate.expire(key, Duration.ofDays(180));
    }

    /**
     * 增加某个集点活动中已经兑换的券/包的数量
     * @param activityId activityId
     * @param couponId couponId
     * @return Integer
     */
    public Integer getIncrClaimedCoupon(Long activityId,Long couponId){
        String key = RedisKeyConstants.JD_ACTIVITY_CLAIMED_NUM + activityId;
        String couponKey = RedisKeyConstants.JD_COUPON + couponId;
        try {
            return redisTemplate.opsForHash().increment(key, couponKey,1).intValue();
        }finally {
            redisTemplate.expire(key, Duration.ofDays(180));
        }
    }

    /**
     * member在某个集点活动中是否已经兑换此商品(券/包)
     * @param activityId activityId
     * @param memberId memberId
     * @param couponId couponId
     * @return Boolean
     */
    public Boolean isCouponExist(Long activityId,Long memberId,Long couponId){
        String key = RedisKeyConstants.JD_ACTIVITY + activityId + RedisKeyConstants.JD_MEMBER + memberId;
        return redisTemplate.opsForSet().isMember(key, couponId);
    }

    /**
     * 获取会员在活动中所有已兑换的商品ID集合
     */
    public Set<Long> getMemberClaimedCoupons(Long activityId, Long memberId) {
        String key = RedisKeyConstants.JD_ACTIVITY + activityId + RedisKeyConstants.JD_MEMBER + memberId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return new HashSet<>();
        }
        return members.stream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    try {
                        return Long.valueOf(obj.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 减去某个集点活动中已经兑换的券/包数量
     * @param activityId activityId
     * @param couponId couponId
     */
    public void decrClaimedCoupon(Long activityId, Long couponId) {
        String key = RedisKeyConstants.JD_ACTIVITY_CLAIMED_NUM + activityId;
        String couponKey = RedisKeyConstants.JD_COUPON + couponId;
        redisTemplate.opsForHash().increment(key, couponKey,-1);
        redisTemplate.expire(key, Duration.ofDays(180));
    }

    /**
     * 获取活动中所有优惠券的已兑换数量
     */
    public Map<Long, Integer> getAllClaimedCoupons(Long activityId) {
        String key = RedisKeyConstants.JD_ACTIVITY_CLAIMED_NUM + activityId;

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> extractCouponId(entry.getKey().toString()),
                        entry -> Integer.valueOf(entry.getValue().toString())
                ));
    }

    /**
     * 从 couponKey 中提取 couponId
     */
    private Long extractCouponId(String couponKey) {
        String prefix = RedisKeyConstants.JD_COUPON;
        return Long.valueOf(couponKey.substring(prefix.length()));
    }

    /**
     * 扣掉用户手中点数
     * @param activityId activityId
     * @param memberId memberId
     */
    public void decrMemberPoints(Long activityId, Long memberId,Integer points) {
        redisTemplate.opsForHash().increment(JD_MEMBER_POINTS_COLLECT + activityId, memberId.toString(), -points);
    }

    /**
     * 扣掉用户手中点数
     * @param activityId activityId
     * @param memberId memberId
     */
    public void incrMemberPointsByMe(Long activityId, Long memberId,Integer points) {
        redisTemplate.opsForHash().increment(JD_MEMBER_POINTS_COLLECT + activityId, memberId.toString(), points);
    }

    /**
     * 加点用户手中点数
     * @param activityId activityId
     * @param memberId memberId
     */
    public void incrMemberPoints(Long activityId, Long memberId, Integer points) {
        redisTemplate.opsForHash().increment(JD_MEMBER_POINTS_COLLECT + activityId, memberId.toString(), points);
    }


    public Integer getCacheInventory(Long goodsId, Long activityId, String redisKey, String redisField) {
        String key = redisKey + activityId;
        String field = redisField + goodsId;
        Object o = redisTemplate.opsForHash().get(key, field);
        if (o == null){
            return 0;
        }
        return (Integer) o;
    }

    public void updateActivityCoupon(ActivityJDCouponReqSaveVO activityJDCouponReqSaveVO, Long activityId, String redisKey) {
        String key = redisKey + activityId;
        String field = String.valueOf(activityJDCouponReqSaveVO.getId());

        redisTemplate.opsForHash().put(key, field, activityJDCouponReqSaveVO);
    }

    public void updateActivityCouponPackage(ActivityJDCouponPackageReqSaveVO activityJDCouponPackageReqSaveVO, Long activityId, String redisKey) {
        String key = redisKey + activityId;
        String field = String.valueOf(activityJDCouponPackageReqSaveVO.getId());

        redisTemplate.opsForHash().put(key, field, activityJDCouponPackageReqSaveVO);
    }
}