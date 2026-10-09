package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

/** 缓存终态结果；缓存过期或活动删除后仍可查询持久化流水。 */
@Service("lotteryResultCache")
public class LotteryResultCacheImpl implements LotteryResultCache {
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ObjectMapper json;
    @Resource
    private LotteryProperties properties;
    @Override
    public Entry get(long b,LotteryVO request) {
        try {
            String activity=redis.opsForValue().get(LotteryKeys.key(b,0,"mapping",request.getLotteryId()));
            if(activity==null||"-".equals(activity))return null;
            String value=redis.opsForValue().get(key(b,Long.parseLong(activity),request.getMemberId(),request.getRequestId()));
            if(value==null)return null;
            Entry entry=json.readValue(value,Entry.class);
            String revision=redis.opsForValue().get(revisionKey(b,Long.parseLong(activity),request.getMemberId(),request.getRequestId()));
            return revision!=null && entry.reissueSequence()<Long.parseLong(revision)?null:entry;
        }catch(Exception ignored){return null;}
    }
    @Override
    public void publish(LotteryLedger.Draw draw,LotteryUserLogVO result) {
        if("ACCEPTED".equals(draw.getDrawStatus()))return;
        try {
            redis.execute(new DefaultRedisScript<>(
                    "local r=tonumber(redis.call('GET',KEYS[2]) or '0'); if tonumber(ARGV[3])<r then return 0 end; local v=redis.call('GET',KEYS[1]); if v then local ok,old=pcall(cjson.decode,v); if ok and (old.lifecycleStatus=='RETURNED' or (old.result and old.result.drawStatus=='RETURNED')) then return 0 end end; redis.call('SET',KEYS[2],ARGV[3],'EX',ARGV[2]); redis.call('SET',KEYS[1],ARGV[1],'EX',ARGV[2]); return 1",Long.class),
                    List.of(key(draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),draw.getRequestId()),revisionKey(draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),draw.getRequestId())),
                    json.writeValueAsString(new Entry(draw.getStoreId(),result,draw.getDrawStatus(),draw.getSnapshot().getReissueSequence())),Integer.toString(Math.max(1,properties.getResultTtlSeconds())),Long.toString(draw.getSnapshot().getReissueSequence()));
            for(long alias:new long[]{draw.getActivityId(),draw.getSnapshot().getSettings().getId()})
                redis.opsForValue().set(LotteryKeys.key(draw.getBusinessId(),0,"mapping",alias),Long.toString(draw.getActivityId()),Duration.ofDays(1));
        }catch(Exception ignored){ /* 请求流水仍以持久化账本为准。 */ }
    }
    /** 提升补发版本后清理旧失败结果，迟到的旧查询不能再次缓存它。 */
    @Override
    public void invalidateForReissue(LotteryLedger.Draw draw) {
        invalidateForReissue(draw,draw.getSnapshot().getReissueSequence());
    }
    @Override
    public void invalidateForReissue(LotteryLedger.Draw draw,long sequence) {
        redis.execute(new DefaultRedisScript<>(
                "local r=tonumber(redis.call('GET',KEYS[2]) or '0'); if r>tonumber(ARGV[1]) then return 0 end; redis.call('SET',KEYS[2],ARGV[1],'EX',ARGV[2]); redis.call('DEL',KEYS[1]); return 1",Long.class),
                List.of(key(draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),draw.getRequestId()),revisionKey(draw.getBusinessId(),draw.getActivityId(),draw.getMemberId(),draw.getRequestId())),
                Long.toString(sequence),Integer.toString(Math.max(1,properties.getResultTtlSeconds())));
    }
    private String key(long b,long a,long member,String requestId){return LotteryKeys.key(b,a,"result",member+":"+requestId);}
    private String revisionKey(long b,long a,long member,String requestId){return key(b,a,member,requestId)+":revision";}
}
