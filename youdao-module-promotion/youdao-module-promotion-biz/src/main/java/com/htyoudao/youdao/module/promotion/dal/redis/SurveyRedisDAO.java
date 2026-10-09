package com.htyoudao.youdao.module.promotion.dal.redis;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 问卷调查 Redis 缓存
 */
@Repository
public class SurveyRedisDAO {

    /** 问卷详情缓存过期时间：24小时 */
    private static final long DETAIL_EXPIRE_HOURS = 24;

    /** 手机号锁过期时间：10秒（防并发，需大于事务执行时间） */
    private static final long LOCK_EXPIRE_SECONDS = 10;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ========== 问卷详情缓存 ==========

    /**
     * 缓存问卷详情
     */
    public void setSurveyDetail(Long surveyId, Object detail) {
        redisTemplate.opsForValue().set(RedisKeyConstants.SURVEY_DETAIL + surveyId, detail, DETAIL_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 获取问卷详情缓存
     */
    public Object getSurveyDetail(Long surveyId) {
        return redisTemplate.opsForValue().get(RedisKeyConstants.SURVEY_DETAIL + surveyId);
    }

    /**
     * 删除问卷详情缓存
     */
    public void deleteSurveyDetail(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_DETAIL + surveyId);
    }

    // ========== 小程序问卷详情缓存（含题目和选项） ==========

    /**
     * 缓存小程序问卷详情（含题目选项），过期时间1小时
     */
    public void setAppSurveyDetail(Long surveyId, Object appDetail) {
        redisTemplate.opsForValue().set(RedisKeyConstants.SURVEY_APP_DETAIL + surveyId, appDetail, 1, TimeUnit.HOURS);
    }

    /**
     * 获取小程序问卷详情缓存
     */
    public Object getAppSurveyDetail(Long surveyId) {
        return redisTemplate.opsForValue().get(RedisKeyConstants.SURVEY_APP_DETAIL + surveyId);
    }

    /**
     * 删除小程序问卷详情缓存
     */
    public void deleteAppSurveyDetail(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_APP_DETAIL + surveyId);
    }

    // ========== UV（HyperLogLog） ==========

    /**
     * 记录UV（手机号去重）
     */
    public Long addUv(Long surveyId, Long phone) {
        return redisTemplate.opsForHyperLogLog().add(RedisKeyConstants.SURVEY_UV + surveyId, phone);
    }

    /**
     * 获取UV数
     */
    public Long getUv(Long surveyId) {
        Long count = redisTemplate.opsForHyperLogLog().size(RedisKeyConstants.SURVEY_UV + surveyId);
        return count != null ? count : 0L;
    }

    /**
     * 删除UV缓存
     */
    public void deleteUv(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_UV + surveyId);
    }

    // ========== UV 手机号去重集合（Set） ==========

    /**
     * 检查手机号是否为新的UV访问（未统计过）
     */
    public Boolean isUvPhoneNew(Long surveyId, Long phone) {
        return !redisTemplate.opsForSet().isMember(RedisKeyConstants.SURVEY_UV_PHONE + surveyId, phone);
    }

    /**
     * 记录UV手机号到去重集合
     */
    public Long addUvPhone(Long surveyId, Long phone) {
        return redisTemplate.opsForSet().add(RedisKeyConstants.SURVEY_UV_PHONE + surveyId, phone);
    }

    /**
     * 删除UV手机号集合
     */
    public void deleteUvPhone(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_UV_PHONE + surveyId);
    }

    // ========== 已提交手机号集合（Set） ==========

    /**
     * 记录已提交的手机号
     */
    public Long addSubmitted(Long surveyId, Long phone) {
        return redisTemplate.opsForSet().add(RedisKeyConstants.SURVEY_SUBMITTED + surveyId, phone);
    }

    /**
     * 检查手机号是否已提交
     */
    public Boolean hasSubmitted(Long surveyId, Long phone) {
        return redisTemplate.opsForSet().isMember(RedisKeyConstants.SURVEY_SUBMITTED + surveyId, phone);
    }

    /**
     * 获取已提交手机号数量
     */
    public Long getSubmittedCount(Long surveyId) {
        Long size = redisTemplate.opsForSet().size(RedisKeyConstants.SURVEY_SUBMITTED + surveyId);
        return size != null ? size : 0L;
    }

    /**
     * 删除已提交集合
     */
    public void deleteSubmitted(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_SUBMITTED + surveyId);
    }

    // ========== 提交计数（AtomicLong via String） ==========

    /**
     * 提交计数+1
     */
    public Long incrSubmitCount(Long surveyId) {
        return redisTemplate.opsForValue().increment(RedisKeyConstants.SURVEY_SUBMIT_COUNT + surveyId);
    }

    /**
     * 获取提交计数
     */
    public Long getSubmitCount(Long surveyId) {
        Object val = redisTemplate.opsForValue().get(RedisKeyConstants.SURVEY_SUBMIT_COUNT + surveyId);
        if (val == null) return 0L;
        return Long.parseLong(val.toString());
    }

    /**
     * 设置提交计数
     */
    public void setSubmitCount(Long surveyId, Long count) {
        redisTemplate.opsForValue().set(RedisKeyConstants.SURVEY_SUBMIT_COUNT + surveyId, count);
    }

    /**
     * 删除提交计数
     */
    public void deleteSubmitCount(Long surveyId) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_SUBMIT_COUNT + surveyId);
    }

    // ========== 手机号锁（防并发重复提交） ==========

    /**
     * 尝试获取手机号锁（防并发）
     * @return true=获取成功，false=已被锁定
     */
    public boolean tryLockPhone(Long surveyId, Long phone) {
        String key = RedisKeyConstants.SURVEY_PHONE_LOCK + surveyId + ":" + phone;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放手机号锁
     */
    public void unlockPhone(Long surveyId, Long phone) {
        redisTemplate.delete(RedisKeyConstants.SURVEY_PHONE_LOCK + surveyId + ":" + phone);
    }

    // ========== 批量清除问卷所有缓存 ==========

    /**
     * 清除问卷相关所有缓存（删除问卷时调用）
     */
    public void deleteAllSurveyCache(Long surveyId) {
        deleteSurveyDetail(surveyId);
        deleteAppSurveyDetail(surveyId);
        deleteUv(surveyId);
        deleteUvPhone(surveyId);
        deleteSubmitted(surveyId);
        deleteSubmitCount(surveyId);
    }
}
