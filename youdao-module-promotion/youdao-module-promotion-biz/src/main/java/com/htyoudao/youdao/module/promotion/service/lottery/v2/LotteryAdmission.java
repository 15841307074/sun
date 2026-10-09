package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import jakarta.annotation.Resource;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

@Component
public class LotteryAdmission {
    @Resource
    private LotteryProperties properties;
    @Resource
    private StringRedisTemplate redis;
    private final AtomicInteger draws=new AtomicInteger();
    private final AtomicInteger reads=new AtomicInteger();
    private final AtomicInteger results=new AtomicInteger();
    private final AtomicInteger tasks=new AtomicInteger();
    private final AtomicInteger general=new AtomicInteger();
    private static final DefaultRedisScript<Long> RATE=new DefaultRedisScript<>(
            "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('PEXPIRE',KEYS[1],1000) end; return n",Long.class);
    public Permit enter(String kind, long b, long a, Long member) {
        boolean draw="draw".equals(kind), result="result".equals(kind)||"callback".equals(kind), task="task".equals(kind);
        AtomicInteger active=draw?draws:result?results:task?tasks:reads;
        // 为结果查询和后台缓存发布各预留两个执行名额。
        int budget=Math.max(1,properties.getConnectionBudget()-properties.getGrantConcurrency()-4);
        int max=draw?Math.min(properties.getDrawConcurrency(),budget):result?2:budget;
        if(active.incrementAndGet()>max) {active.decrementAndGet(); rejected(kind); throw exception(LOTTERY_SYSTEM_AGAIN);}
        if(!result&&general.incrementAndGet()>budget){general.decrementAndGet();active.decrementAndGet();rejected(kind);throw exception(LOTTERY_SYSTEM_AGAIN);}
        try {
            if(draw&&!properties.isAccepting()) throw exception(LOTTERY_SYSTEM_AGAIN);
            int rate=draw?properties.getDrawRate():result?properties.getResultRate():task?properties.getTaskRate():properties.getQueryRate();
            try { if(!"callback".equals(kind))check(LotteryKeys.key(0,0,"rate",kind),rate); }
            catch(RedisConnectionFailureException unavailable) {
                if(!result)throw unavailable; // 已受理结果查询预留两个受控执行名额。
            }
            if(draw) {
                check(LotteryKeys.key(b,a,"rate","activity"),properties.getActivityDrawRate());
                check(LotteryKeys.key(b,a,"rate","member:"+member),properties.getMemberDrawRate());
            }
            return new Permit(active,result?null:general);
        } catch(RuntimeException e) { active.decrementAndGet();if(!result)general.decrementAndGet();rejected(kind); throw exception(LOTTERY_SYSTEM_AGAIN); }
    }
    private void rejected(String kind){io.micrometer.core.instrument.Metrics.counter("lottery.rejected","kind",kind).increment();}
    private void check(String key,int limit) {
        Long n=redis.execute(RATE,List.of(key));
        if(n==null||n>Math.max(0,limit)) throw exception(LOTTERY_SYSTEM_AGAIN);
    }
    public static final class Permit implements AutoCloseable {
        private AtomicInteger active,general;
        private Permit(AtomicInteger active,AtomicInteger general){this.active=active;this.general=general;}
        @Override
        public void close(){if(active!=null){active.decrementAndGet();if(general!=null)general.decrementAndGet();active=null;}}
    }
}
