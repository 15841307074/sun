package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import lombok.Data;
import java.util.List;
import java.util.Map;

/** LotteryLedger 服务契约；实现位于 impl 包。 */
public interface LotteryLedger {
    @Data public static class Draw {
        private long id,businessId,activityId,memberId,storeId,epoch,pool;
        private String requestId,outBillNo,drawStatus,grantStatus,stockState,scopeKey,stockPrizeCode;
        private int source,pointsCost;
        private LotteryDrawSnapshot snapshot;
        private LotteryUserLogVO result;
        /** 仅在内存中标记本次新受理，区分幂等重放。 */
        private boolean newlyAccepted;
        /** 积分扣退的幂等键不随微信商户单号绑定而改变。 */
        public String pointsBusinessNo() { return "L" + id; }
        public String cashBillNo() {
            if (snapshot.getReissueCashBillNo()!=null && !snapshot.getReissueCashBillNo().isBlank()) return snapshot.getReissueCashBillNo();
            return snapshot.isFallbackApplied() ? outBillNo + "F" : outBillNo;
        }
    }

    /** 持久化任务就绪通知；提交后唤醒，延迟任务仍以数据库 next_at 为准。 */
    record JobReady(long id,long delayMillis) {
        public JobReady(long id){this(id,0);}
    }

    public record Stock(long total,long reserved,long issued,long revision) {}

    public static class UnavailablePrize extends RuntimeException {}

    public record Reissue(Draw draw, boolean queued, String message) {}

    String encode(Object value);

    Draw find(long b,long a,long m,String requestId);

    Draw findByReference(long b,long ref,long m,String requestId);

    void fillDisplayStock(long businessId,long activityId,long epoch,List<LotteryPrizeDO> prizes);

    Draw get(long id);

    Draw byBill(String bill);

    Stock stock(long b,long a,long epoch,long pool,String code);

    long consumed(long b,long a,long m,String scope,int source);

    long activityDrawCount(long b,long a);

    long[] counter(long b,long a,long m,String scope,int source);

    Map<String,long[]> counters(long b,long a,long m,String period);

    Draw accept(long b,LotteryDrawSnapshot snapshot);

    void grantState(long id,String grant,LotteryUserLogVO result,boolean terminal,boolean success);

    boolean firstCashAttempt(long id);

    void bindCashBill(long id,String bill);

    void rejectUnpaid(long id);

    void fallback(long id);

    boolean wakeByBill(String bill);

    Reissue reissue(long b,long id,long member,long store,String requestId,String expectedBill,
                           long expectedSequence,boolean cashDefinitelyFailed);

    boolean queuePhysicalReturn(LotteryLogDO log);

    boolean isReturnJob(long id);

    void confirmPhysicalReturn(long id);

    List<Long> due(int count);

    boolean claim(long id,String token);

    void finishJob(long id,String token);

    void retryJob(long id,String token,String message);

    void stopJob(long id,String token,String message);

    void earn(long b,LotterySettingsCacheDataVO cfg,long m,String period,String scope,int source,int limit,boolean unlimited,int reward,int balance);

    void earnOrder(long b,LotterySettingsCacheDataVO cfg,long m,String period,String scope,int limit,boolean unlimited,String orderSn);
}
