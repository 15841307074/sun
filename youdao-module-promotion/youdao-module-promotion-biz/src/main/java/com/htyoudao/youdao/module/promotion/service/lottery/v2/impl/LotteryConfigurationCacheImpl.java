package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service("lotteryConfigurationCache")
@DS("master")
public class LotteryConfigurationCacheImpl implements LotteryConfigurationCache {
    @Resource
    private LotterySettingsMapper settingsMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private LotteryPrizeMapper prizeMapper;
    @Resource
    private ActivityStoreService stores;
    @Resource
    private LotteryScopeService scope;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private LotteryProperties properties;
    @Resource
    private ObjectMapper json;
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private LotteryLedger ledger;
    private final AtomicInteger refills=new AtomicInteger();
    private static final DefaultRedisScript<Long> UNLOCK=new DefaultRedisScript<>("if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end return 0",Long.class);
    @Override
    public LotterySettingsCacheDataVO settings(long id) {
        long b=BusinessContextHolder.getRequiredBusinessId();
        String alias=LotteryKeys.key(b,0,"mapping",id);
        String a=redis.opsForValue().get(alias);
        if("-".equals(a))throw exception(LOTTERY_NOT_NULL);
        if(a!=null) {
            String head=redis.opsForValue().get(LotteryKeys.key(b,Long.parseLong(a),"config-version",0));
            if(head!=null) {
                String cached=redis.opsForValue().get(LotteryKeys.key(b,Long.parseLong(a),"config",head));
                if(cached!=null){cacheRead("config","hit");return parse(cached,LotterySettingsCacheDataVO.class);}
            }
        }
        cacheRead("config","miss");return refill(alias,()->{
            LotterySettingsDO cfg=settingsMapper.selectOne(new LambdaQueryWrapper<LotterySettingsDO>()
                    .eq(LotterySettingsDO::getBusinessId,b).and(w->w.eq(LotterySettingsDO::getId,id).or().eq(LotterySettingsDO::getActivityId,id)));
            ActivityDO activity=cfg==null||cfg.getActivityId()==null?null:activityMapper.selectById(cfg.getActivityId());
            if(cfg==null||activity==null||!Objects.equals(activity.getBusinessId(),b)) {
                redis.opsForValue().set(alias,"-",Duration.ofSeconds(15));throw exception(LOTTERY_NOT_NULL);
            }
            LotterySettingsCacheDataVO value=BeanUtils.toBean(cfg,LotterySettingsCacheDataVO.class);
            BeanUtils.copyProperties(activity,value);
            value.setAppScope(Optional.ofNullable(activity.getAppScope()).orElse(0));
            value.setId(cfg.getId());value.setActivityId(cfg.getActivityId());value.setState(activity.getIsEnabled());
            value.setLotteryStartTime(activity.getStartDate());value.setLotteryEndTime(activity.getEndDate());
            value.setConfigVersion(cfg.getConfigVersion());value.setStockEpoch(cfg.getStockEpoch());value.setRuntimeVersion(cfg.getRuntimeVersion());
            value.setStoreIds(stores.selectStoreIdsByActivityId(cfg.getActivityId()));value.setTagIds(scope.tagIds(cfg.getActivityId()));
            long version=Optional.ofNullable(cfg.getConfigVersion()).orElse(0L);
            String headKey=LotteryKeys.key(b,cfg.getActivityId(),"config-version",0);
            Long published=redis.execute(new DefaultRedisScript<>("local v=redis.call('GET',KEYS[1]); if v and tonumber(v)>tonumber(ARGV[1]) then return 0 end redis.call('SET',KEYS[1],ARGV[1]); return 1",Long.class),List.of(headKey),Long.toString(version));
            if(!Objects.equals(published,1L))throw exception(LOTTERY_SYSTEM_AGAIN);
            redis.opsForValue().set(LotteryKeys.key(b,cfg.getActivityId(),"config",version),encode(value),ttl());
            for(long ref:new long[]{cfg.getId(),cfg.getActivityId()})redis.opsForValue().set(LotteryKeys.key(b,0,"mapping",ref),cfg.getActivityId().toString(),Duration.ofDays(1));
            return value;
        });
    }
    @Override
    public List<LotteryPrizeDO> prizes(LotterySettingsCacheDataVO cfg,long storeId) {
        long b=BusinessContextHolder.getRequiredBusinessId();long pool=Objects.equals(cfg.getPrizePoolRules(),2)?storeId:0;
        String key=LotteryKeys.key(b,cfg.getActivityId(),"prizes",cfg.getConfigVersion()+":"+pool);
        String cached=redis.opsForValue().get(key);
        if(cached!=null){cacheRead("prizes","hit");return Arrays.asList(parse(cached,LotteryPrizeDO[].class));}
        cacheRead("prizes","miss");
        return refill(key,()->{
            LambdaQueryWrapper<LotteryPrizeDO> query=new LambdaQueryWrapper<LotteryPrizeDO>().eq(LotteryPrizeDO::getLotteryId,cfg.getId());
            if(pool==0)query.and(w->w.isNull(LotteryPrizeDO::getStoreId).or().eq(LotteryPrizeDO::getStoreId,0L));else query.eq(LotteryPrizeDO::getStoreId,pool);
            List<LotteryPrizeDO> values=prizeMapper.selectList(query);
            // 奖品展示缓存不保存可作为扣减依据的剩余库存。
            values.forEach(p->p.setRemainNum(null));
            redis.opsForValue().set(key,encode(values),values.isEmpty()?Duration.ofSeconds(15):ttl());
            return values;
        });
    }
    @Override
    public List<LotteryPrizeDO> displayPrizes(LotterySettingsCacheDataVO cfg,long storeId) {
        if(!Objects.equals(cfg.getRuntimeVersion(),2)) {
            var query=new LambdaQueryWrapper<LotteryPrizeDO>().eq(LotteryPrizeDO::getLotteryId,cfg.getId());
            if(Objects.equals(cfg.getPrizePoolRules(),2))query.eq(LotteryPrizeDO::getStoreId,storeId);else query.and(w->w.isNull(LotteryPrizeDO::getStoreId).or().eq(LotteryPrizeDO::getStoreId,0L));
            return prizeMapper.selectList(query);
        }
        List<LotteryPrizeDO> result=prizes(cfg,storeId);
        ledger.fillDisplayStock(BusinessContextHolder.getRequiredBusinessId(),cfg.getActivityId(),cfg.getStockEpoch(),result);
        return result;
    }
    @Override
    public List<Long> storeSettings(long storeId) {
        long b=BusinessContextHolder.getRequiredBusinessId();
        String version=redis.opsForValue().get(LotteryKeys.key(b,0,"store-list-version",0));
        String key=LotteryKeys.key(b,0,"store-list",Objects.toString(version,"0")+":"+storeId);
        String cached=redis.opsForValue().get(key);
        if(cached!=null)return Arrays.asList(parse(cached,Long[].class));
        return refill(key,()->{
            List<Long> ids=jdbc.queryForList("SELECT s.id FROM lottery_settings s JOIN activity a ON a.id=s.activity_id AND a.business_id=s.business_id "
                    +"WHERE s.business_id=? AND s.deleted=0 AND a.deleted=0 AND s.runtime_version=2 AND s.state=1 AND a.is_enabled=1 AND s.public_button=0 "
                    +"AND ((COALESCE(a.app_scope,0)=0 AND a.activity_store=1) OR EXISTS(SELECT 1 FROM activity_store r WHERE r.activity_id=a.id AND r.business_id=? AND r.store_id=? AND r.deleted=0)) ORDER BY s.lottery_type,s.id",Long.class,b,b,storeId);
            redis.opsForValue().set(key,encode(ids),Duration.ofSeconds(Math.max(1,properties.getListTtlSeconds())+ThreadLocalRandom.current().nextInt(6)));
            return ids;
        });
    }
    private Duration ttl(){return Duration.ofSeconds(Math.max(15,properties.getConfigTtlSeconds())+ThreadLocalRandom.current().nextInt(61));}
    private String encode(Object o){try{return json.writeValueAsString(o);}catch(Exception e){throw new IllegalStateException(e);}}
    private <T>T parse(String s,Class<T> t){try{return json.readValue(s,t);}catch(Exception e){throw new IllegalStateException(e);}}
    private <T>T refill(String key,Supplier<T> action) {
        if(refills.incrementAndGet()>Math.max(1,properties.getRefillConcurrency())){refills.decrementAndGet();throw exception(LOTTERY_SYSTEM_AGAIN);}
        String lock=key+":refill",token=UUID.randomUUID().toString();boolean acquired=false;
        try {
            acquired=Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lock,token,Duration.ofSeconds(5)));
            if(!acquired)throw exception(LOTTERY_SYSTEM_AGAIN);
            io.micrometer.core.instrument.Metrics.counter("lottery.cache.refill").increment();
            return action.get();
        } finally {
            refills.decrementAndGet();
            if(acquired)redis.execute(UNLOCK,List.of(lock),token);
        }
    }
    private void cacheRead(String cache,String result){io.micrometer.core.instrument.Metrics.counter("lottery.cache.read","cache",cache,"result",result).increment();}
}
