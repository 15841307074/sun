package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

/** 原任务入口使用此服务；每批200条，单条失败不影响其他记录。 */
@Service
@DS("sharding")
@Slf4j
public class LotteryPhysicalReturnService {
    @Resource private LotteryLogMapper logs;
    @Resource private LotteryPrizeMapper prizes;
    @Resource private LotterySettingsMapper settings;
    @Resource private LotteryLedger ledger;
    @Resource private LotteryLegacyReturnStorage legacyStorage;
    @Resource private JdbcTemplate jdbc;
    @Resource private StringRedisTemplate redis;
    @Resource private RedissonClient redisson;
    private JdbcTemplate returnJdbc;
    @PostConstruct void initialize(){returnJdbc=new JdbcTemplate(jdbc.getDataSource());returnJdbc.setQueryTimeout(5);}
    private JdbcTemplate db(){return returnJdbc==null?jdbc:returnJdbc;}
    private static final DefaultRedisScript<Long> RESTORE=new DefaultRedisScript<>("""
            local value=redis.call('GET',KEYS[1])
            if redis.call('EXISTS',KEYS[2])==1 then return tonumber(value) or -2 end
            if value then
              local number=tonumber(value)
              if not number then return -1 end
              local used=math.max(0,number-1)
              redis.call('SET',KEYS[1],used)
              redis.call('SET',KEYS[2],'1')
              return used
            end
            redis.call('SET',KEYS[2],'1')
            return -2
            """,Long.class);
    @DataPermission(enable=false)
    public void sweep() {
        LocalDateTime deadline=LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusHours(48);
        long cursor=0;
        for(int page=0;page<50;page++) {
            var batch=logs.selectLotteryRows(new LambdaQueryWrapper<LotteryLogDO>().gt(LotteryLogDO::getId,cursor)
                    .eq(LotteryLogDO::getPrizeState,1).eq(LotteryLogDO::getPrizeType,3).eq(LotteryLogDO::getDeleted,false)
                    .le(LotteryLogDO::getCreateTime,deadline)
                    .and(a -> a.isNull(LotteryLogDO::getReceiveAddress).or().eq(LotteryLogDO::getReceiveAddress,""))
                    .and(t -> t.isNull(LotteryLogDO::getTrackingNumber).or().eq(LotteryLogDO::getTrackingNumber,""))
                    .orderByAsc(LotteryLogDO::getId).last("LIMIT 200"));
            if(batch.isEmpty())break;
            for(var row:batch) {
                cursor=Math.max(cursor,row.getId());Long previous=BusinessContextHolder.getBusinessId();
                try {
                    BusinessContextHolder.setBusinessId(row.getBusinessId());
                    if(!ledger.queuePhysicalReturn(row))legacy(row);
                } catch(RuntimeException failure){log.warn("实物退回失败，保留后续处理 businessId={}, logId={}, reason={}",row.getBusinessId(),row.getId(),failure.getClass().getSimpleName());}
                finally{BusinessContextHolder.setBusinessId(previous);}
            }
            if(batch.size()<200)break;
        }
        recoverLegacy();ledger.wakePhysicalReturns();
    }
    private void legacy(LotteryLogDO row) {
        var prize=prizes.selectOne(new LambdaQueryWrapper<LotteryPrizeDO>().eq(LotteryPrizeDO::getId,row.getLotteryPrizeId()).eq(LotteryPrizeDO::getBusinessId,row.getBusinessId()));
        if(prize==null)throw new IllegalStateException("原实物奖品配置不存在");
        var cfg=settings.selectOne(new LambdaQueryWrapper<LotterySettingsDO>().eq(LotterySettingsDO::getBusinessId,row.getBusinessId())
                .and(w -> w.eq(LotterySettingsDO::getId,prize.getLotteryId()).or().eq(LotterySettingsDO::getActivityId,prize.getLotteryId())).last("LIMIT 1"));
        boolean independent=cfg!=null&&Objects.equals(cfg.getPrizePoolRules(),2);
        long activity=cfg==null?prize.getLotteryId():cfg.getActivityId();
        String suffix=(independent?":store:"+row.getStoreId():"")+":prize:"+prize.getId();
        String primary="prize_num:"+activity+suffix,old="prize_num:"+prize.getLotteryId()+suffix;
        String cache="lottery_prize:"+activity+(independent?":store:"+row.getStoreId():"");
        String lockKey="prizeInventory:lock:lottery:"+prize.getLotteryId()+suffix;
        // 与旧抽奖相同的奖品锁，在事务之前获取，绝不持有数据库锁等待Redis。
        var lock=redisson.getLock(lockKey);
        if(!lock.tryLock())return;
        try{legacyStorage.claim(row,primary,old,cache,lockKey);}finally{if(lock.isHeldByCurrentThread())lock.unlock();}
    }
    private void recoverLegacy() {
        for(var receipt:db().queryForList("SELECT * FROM lottery_legacy_return WHERE state='READY' ORDER BY log_id LIMIT 200")) {
            long id=((Number)receipt.get("log_id")).longValue(),business=((Number)receipt.get("business_id")).longValue();
            var lock=redisson.getLock((String)receipt.get("lock_key"));boolean acquired=false;
            try {
                acquired=lock.tryLock();if(!acquired)continue;
                String state=db().queryForObject("SELECT state FROM lottery_legacy_return WHERE business_id=? AND log_id=?",String.class,business,id);
                if(!"READY".equals(state))continue;
                String primary=(String)receipt.get("primary_stock_key"),old=(String)receipt.get("legacy_stock_key");
                if(!primary.equals(old)&&!Boolean.TRUE.equals(redis.hasKey(primary))) {
                    String previous=redis.opsForValue().get(old);
                    if(previous!=null)redis.opsForValue().setIfAbsent(primary,previous);
                }
                // 标记键与主库存键落在同一Redis Cluster槽，旧别名在Lua外同步。
                String marker="{"+primary+"}:physical-return:"+business+":"+id;
                Long restored=redis.execute(RESTORE,List.of(primary,marker));
                if(restored==null||restored==-1)throw new IllegalStateException("旧库存缓存格式不正确");
                if(restored>=0&&!primary.equals(old))redis.opsForValue().set(old,Long.toString(restored));
                redis.delete((String)receipt.get("prize_cache_key"));
                db().update("UPDATE lottery_legacy_return SET state='DONE',update_time=NOW() WHERE business_id=? AND log_id=? AND state='READY'",business,id);
            }catch(RuntimeException failure){log.warn("旧实物库存同步待重试 logId={}, reason={}",id,failure.getClass().getSimpleName());}
            finally{if(acquired&&lock.isHeldByCurrentThread())lock.unlock();}
        }
    }
}
