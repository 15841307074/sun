package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

/** 奖品预占使用短锁，先于数据库事务获取，不覆盖远程发奖。 */
@Component
public class LotteryStockCache {
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private RedissonClient redisson;
    @Resource
    private LotteryLedger ledger;
    private static final DefaultRedisScript<Long> RESERVE=new DefaultRedisScript<>("""
            if redis.call('EXISTS',KEYS[1])==0 then return -2 end
            if redis.call('HEXISTS',KEYS[1],'pending')==1 then return -2 end
            if redis.call('HGET',KEYS[1],'configVersion')~=ARGV[2] then return -2 end
            local total=tonumber(redis.call('HGET',KEYS[1],'total'))
            local used=tonumber(redis.call('HGET',KEYS[1],'used'))
            if not total or not used or total<0 or used<0 or used>total then return -2 end
            if used>=total then return 0 end
            redis.call('HINCRBY',KEYS[1],'used',1)
            redis.call('HSET',KEYS[1],'pending',ARGV[1])
            return 1
            """,Long.class);
    private static final DefaultRedisScript<Long> COMMIT=new DefaultRedisScript<>("""
            if redis.call('HGET',KEYS[1],'pending')~=ARGV[1] then return 0 end
            redis.call('HDEL',KEYS[1],'pending')
            return 1
            """,Long.class);

    public void reconcile(LotteryLedger.Draw draw) {
        if("NONE".equals(draw.getStockState()))return;
        String key=LotteryKeys.stock(draw.getBusinessId(),draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getStockPrizeCode());
        RLock lock=redisson.getLock(key+":lock");
        if(!lock.tryLock())throw exception(LOTTERY_SYSTEM_AGAIN);
        try(Reservation reservation=new Reservation(lock,key,draw.getBusinessId(),draw.getActivityId(),draw.getEpoch(),draw.getPool(),draw.getStockPrizeCode(),null)){reservation.reconcile();}
    }

    public Reservation reserve(long b,LotteryDrawSnapshot s) {
        var cfg=s.getSettings();var prize=s.getPrize();long pool=Objects.equals(cfg.getPrizePoolRules(),2)?s.getRequest().getStoreId():0;
        String key=LotteryKeys.stock(b,cfg.getActivityId(),cfg.getStockEpoch(),pool,prize.getCode());
        RLock lock=redisson.getLock(key+":lock");
        if(!lock.tryLock())throw exception(LOTTERY_SYSTEM_AGAIN);
        Reservation reservation=new Reservation(lock,key,b,cfg.getActivityId(),cfg.getStockEpoch(),pool,prize.getCode(),cfg.getConfigVersion());
        try {
            // 热库存预占不查库；Redis 预占与数据库提交之间中断时，待确认标记会保留，
            // 后续请求必须先恢复该库存分区才能继续受理。
            Long result=reservation.tryReserve();
            if(Objects.equals(result,-2L)) {
                reservation.reconcile();
                result=reservation.tryReserve();
            }
            if(Objects.equals(result,0L))throw new LotteryLedger.UnavailablePrize();
            if(!Objects.equals(result,1L))throw exception(LOTTERY_SYSTEM_AGAIN);
            return reservation;
        } catch(RuntimeException e) {reservation.close();throw e;}
    }
    public class Reservation implements AutoCloseable {
        private final RLock lock;private final String key,code;private final long b,a,epoch,pool;
        private final Long configVersion;
        private final String token=UUID.randomUUID().toString();
        private Reservation(RLock lock,String key,long b,long a,long epoch,long pool,String code,Long configVersion){this.lock=lock;this.key=key;this.b=b;this.a=a;this.epoch=epoch;this.pool=pool;this.code=code;this.configVersion=configVersion;}
        private Long tryReserve(){return redis.execute(RESERVE,List.of(key),token,configVersion.toString());}
        /** 只有新提交的请求才能确认本次预占；重试必须核对库存。 */
        public void commit() {
            if(!Objects.equals(redis.execute(COMMIT,List.of(key),token),1L))
                throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        public void reconcile() {
            io.micrometer.core.instrument.Metrics.counter("lottery.stock.recovery").increment();
            LotteryLedger.Stock stock=ledger.stock(b,a,epoch,pool,code);
            if(stock==null)throw exception(LOTTERY_SYSTEM_AGAIN);
            // 原子替换缓存快照；Redis 故障切换后仍以数据库条件更新为最终约束。
            redis.execute(new DefaultRedisScript<>("redis.call('HSET',KEYS[1],'total',ARGV[1],'used',ARGV[2],'revision',ARGV[3],'configVersion',ARGV[4]); redis.call('HDEL',KEYS[1],'pending'); redis.call('PERSIST',KEYS[1]); redis.call('SADD',KEYS[2],KEYS[1]); return 1",Long.class),
                    List.of(key,LotteryKeys.mutableRegistry(b,a)),Long.toString(stock.total()),Long.toString(stock.reserved()+stock.issued()),Long.toString(stock.revision()),configVersion==null?"":configVersion.toString());
        }
        @Override
        public void close(){if(lock.isHeldByCurrentThread())lock.unlock();}
    }
}
