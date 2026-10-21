package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import jakarta.annotation.Resource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.springframework.transaction.event.TransactionalEventListener;
import javax.sql.DataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.zaxxer.hikari.HikariDataSource;
import com.alibaba.druid.pool.DruidDataSource;

@Component
@Slf4j
public class LotteryGrantWorker {
    @Resource
    private LotteryLedger ledger;
    @Resource
    private LotteryGrantStorage storage;
    @Resource
    private LotteryCashGateway cash;
    @Resource
    private LotteryStockCache stockCache;
    @Resource
    private LotteryV2Service service;
    @Resource private LotteryCouponPackageService couponPackages;
    @Resource
    private LotteryProperties properties;
    @Resource
    private DataSource dataSource;
    @DubboReference(timeout=3000,retries=0) private WxMemberApi members;
    // 待执行队列有界；通知丢失或溢出仍由持久化任务恢复，不在请求线程发奖。
    private final ThreadPoolExecutor executor=new ThreadPoolExecutor(4,4,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64),r->{Thread t=new Thread(r,"lottery-grant");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    private static final long IDLE_AUDIT_MILLIS=TimeUnit.MINUTES.toMillis(5);
    private static final long BUSY_AUDIT_MILLIS=1000;
    private static final int MAX_DELAYED_WAKEUPS=1024;
    private final Set<Long> inFlight=ConcurrentHashMap.newKeySet();
    private record Wakeup(long at,String source) {}
    private final ConcurrentMap<Long,Wakeup> delayed=new ConcurrentHashMap<>();
    private final AtomicLong nextAuditAt=new AtomicLong();
    private LongSupplier clock=()->System.nanoTime()/1000000;
    private LongSupplier auditJitter=()->ThreadLocalRandom.current().nextLong(30001);
    private volatile boolean stopped;

    private record PoolState(int max,int active) {
        int reserve(){return Math.min(max-1,Math.max(2,(max+3)/4));}
        int room(){return Math.max(0,max-reserve()-active);}
    }

    /** 只读连接池内存指标，不取连接、不执行 SQL。无法识别的池沿用配置预算。 */
    private PoolState poolState() {
        try {
            DataSource pool=dataSource;
            if(pool instanceof DynamicRoutingDataSource routing)pool=routing.getDataSource(DynamicDataSourceContextHolder.peek());
            if(pool instanceof ItemDataSource item)pool=item.getRealDataSource();
            if(pool instanceof HikariDataSource hikari) {
                int max=hikari.getMaximumPoolSize();
                var metrics=hikari.getHikariPoolMXBean();
                return max>0?new PoolState(max,metrics==null?0:metrics.getActiveConnections()):null;
            }
            if(pool instanceof DruidDataSource druid)
                return druid.getMaxActive()>0?new PoolState(druid.getMaxActive(),druid.getActiveCount()):null;
        } catch(RuntimeException ignored) { /* 数据源初始化/切换期间仍受配置并发预算约束。 */ }
        return null;
    }

    private boolean poolHasRoom(){PoolState pool=poolState();return pool==null||pool.room()>0;}

    @TransactionalEventListener(fallbackExecution=true)
    public void onJobReady(LotteryLedger.JobReady event) {
        if(stopped)return;
        if(event.delayMillis()>0) {
            long at=clock.getAsLong()+event.delayMillis();
            if(delayed.containsKey(event.id())||delayed.size()<MAX_DELAYED_WAKEUPS) {
                var wakeup=new Wakeup(at,DynamicDataSourceContextHolder.peek());
                delayed.merge(event.id(),wakeup,(old,next)->old.at()<=next.at()?old:next);
            } else requestAudit(at);
        } else submit(event.id());
    }

    private synchronized int resizeExecutor() {
        int size=Math.max(1,Math.min(properties.getGrantConcurrency(),properties.getConnectionBudget()/2));
        PoolState pool=poolState();
        if(pool!=null)size=Math.min(size,Math.max(1,pool.max()/4));
        if(size>executor.getMaximumPoolSize()){executor.setMaximumPoolSize(size);executor.setCorePoolSize(size);}
        else {executor.setCorePoolSize(size);executor.setMaximumPoolSize(size);}
        return size;
    }

    private void requestAudit(long at) {
        nextAuditAt.getAndUpdate(previous->previous==0?0:Math.min(previous,at));
    }

    private void submit(long id) {
        submit(id,DynamicDataSourceContextHolder.peek());
    }

    private void submit(long id,String source) {
        if(stopped||!inFlight.add(id))return;
        try {
            resizeExecutor();
            executor.execute(()->{
                if(source!=null)DynamicDataSourceContextHolder.push(source);
                try {
                    if(!stopped&&poolHasRoom())run(id);
                    else requestAudit(clock.getAsLong()+BUSY_AUDIT_MILLIS);
                }
                catch(RuntimeException e){log.warn("Lottery grant dispatch deferred: request={}, reason={}",id,e.getClass().getSimpleName());}
                finally {if(source!=null)DynamicDataSourceContextHolder.poll();inFlight.remove(id);}
            });
        } catch(RuntimeException e) {
            inFlight.remove(id);
            requestAudit(clock.getAsLong()+BUSY_AUDIT_MILLIS);
        }
    }

    // 每秒只检查内存期限；空闲审计五分钟加抖动，八实例错峰。启动后首次恢复不等待五分钟。
    @Scheduled(fixedDelay=1000)
    public void poll() {
        if(stopped)return;
        long now=clock.getAsLong();
        delayed.forEach((id,wakeup)->{if(now>=wakeup.at()&&delayed.remove(id,wakeup))submit(id,wakeup.source());});
        int size=resizeExecutor();
        int free=Math.max(0,size-executor.getActiveCount()-executor.getQueue().size());
        PoolState pool=poolState();
        if(pool!=null)free=Math.min(free,pool.room());
        if(free==0)return;
        long dueAt=nextAuditAt.get();
        if(dueAt!=0&&now<dueAt)return;
        if(!nextAuditAt.compareAndSet(dueAt,now+IDLE_AUDIT_MILLIS+auditJitter.getAsLong()))return;
        try {
            List<Long> ids=ledger.due(free);
            for(Long id:ids)submit(id);
            if(ids.size()==free)requestAudit(now+BUSY_AUDIT_MILLIS);
        } catch(RuntimeException e){log.warn("Lottery work audit deferred: {}",e.getClass().getSimpleName());}
    }
    private LotteryLedger.Draw run(long id) {
        long started=System.nanoTime(), transferNanos=0;
        boolean cashDraw=false, cashTransferAttempted=false, packageGrantAttempted=false;
        String token=UUID.randomUUID().toString();
        if(!ledger.claim(id,token))return null;
        Long previousBusiness=BusinessContextHolder.getBusinessId();
        try {
            LotteryLedger.Draw draw=ledger.get(id);
            Integer prizeType=draw.getSnapshot()==null||draw.getSnapshot().getPrize()==null?null:draw.getSnapshot().getPrize().getPrizeType();
            cashDraw=Objects.equals(prizeType,5);
            BusinessContextHolder.setBusinessId(draw.getBusinessId());
            if(Objects.equals(prizeType,3)&&ledger.isReturnJob(id)) {
                if(storage.claimPhysicalReturn(draw)) {
                    ledger.confirmPhysicalReturn(id);draw=ledger.get(id);
                    if("RETURNED".equals(draw.getDrawStatus())) {
                        stockCache.reconcile(draw);service.result(draw);
                    }
                }
                ledger.finishJob(id,token);return draw;
            }
            if("ACCEPTED".equals(draw.getDrawStatus())) {
                if(!draw.getSnapshot().isManualReissue()&&draw.getPointsCost()>0&&!members.changeLotteryPoints(draw.getMemberId(),draw.pointsBusinessNo()+":DEBIT",-draw.getPointsCost())) {
                    ledger.rejectUnpaid(id);draw=ledger.get(id);
                } else {
                    var result=service.result(draw);
                    int type=draw.getSnapshot().getPrize().getPrizeType();
                    if(type==5) {
                        // 持久化受理记录及任务已存在。先获取领取参数，再一次写入最新小票记录，
                        // 省去请求线程中重复的日志插入/读取/更新和中间状态事务。
                        long transferStarted=System.nanoTime();
                        TransferToUser.TransferToUserResponse response;
                        cashTransferAttempted=true;
                        try {response=cash.reconcileOrSend(draw);}
                        finally {transferNanos=System.nanoTime()-transferStarted;}
                        if(response.getOutBillNo()==null||response.getOutBillNo().isBlank())
                            throw new IllegalStateException("红包商户单号未返回");
                        if(!("L"+draw.getId()).equals(draw.getOutBillNo())&&!draw.cashBillNo().equals(response.getOutBillNo()))
                            throw new IllegalStateException("红包返回单号与原抽奖不一致");
                        ledger.bindCashBill(id,response.getOutBillNo());
                        if(response.getState()==null)throw new IllegalStateException("红包状态未知");
                        String state=response.getState().name();result.setPackageInfo(response.getPackageInfo());
                        result.setOutBillNo(response.getOutBillNo());
                        if("SUCCESS".equals(state))ledger.grantState(id,"SUCCESS",result,true,true);
                        else if("FAIL".equals(state)||"CANCELLED".equals(state)) {
                            if(draw.getSnapshot().isManualReissue())ledger.grantState(id,"FAILED",result,true,false);
                            else if(Objects.equals(draw.getSnapshot().getPrize().getId(),draw.getSnapshot().getGuarantee().getId())) ledger.rejectUnpaid(id);
                            else ledger.fallback(id);
                        }
                        else {
                            ledger.grantState(id,state,result,false,false);
                            LotteryLedger.Draw current=ledger.get(id);
                            storage.log(current);
                            ledger.retryJob(id,token,"微信红包处理中");return current;
                        }
                    } else {
                        if(type==6) {
                            packageGrantAttempted=true;
                            storage.log(draw);
                            couponPackages.grant(draw);
                        }
                        if(type==1)storage.coupon(draw);
                        if(type==2&&!members.changeLotteryPoints(draw.getMemberId(),draw.pointsBusinessNo()+":AWARD",draw.getSnapshot().getPrize().getPrizeValue().intValueExact()))
                            throw new IllegalStateException("积分发放未完成");
                        ledger.grantState(id,"SUCCESS",result,true,true);
                    }
                    draw=ledger.get(id);
                }
            }
            // 日志失败只重试日志；已发放奖励不退库存，也不重复发放。
            // 预占转已发放不改变总占用，无需重新查询库存。
            // 释放和退奖仍需核对对应库存分区。
            if(!"ISSUED".equals(draw.getStockState()))stockCache.reconcile(draw);
            if("FAILED".equals(draw.getDrawStatus()) && !draw.getSnapshot().isManualReissue() && draw.getPointsCost()>0)
                members.refundLotteryPoints(draw.getMemberId(),draw.pointsBusinessNo());
            if("COMPLETED".equals(draw.getDrawStatus())||Objects.equals(draw.getSnapshot().getPrize().getPrizeType(),5)||draw.getSnapshot().isFallbackApplied())storage.log(draw);
            if("ACCEPTED".equals(draw.getDrawStatus()))ledger.retryJob(id,token,"保底发放待处理");
            else {ledger.finishJob(id,token);delayed.remove(id);}
            return draw;
        } catch(Exception e){
            // 微信调用失败仍保留中奖记录；记录失败不掩盖原异常、不重复转账。
            LotteryLedger.Draw failed=null;
            try {failed=ledger.get(id);if(failed!=null&&Objects.equals(failed.getSnapshot().getPrize().getPrizeType(),5))storage.log(failed);}
            catch(Exception logging){log.warn("Lottery cash log deferred: request={}, reason={}",id,logging.getClass().getSimpleName());}
            if(packageGrantAttempted) {
                // 只处理进入发券阶段后的失败；扣积分阶段仍沿用原重试逻辑。
                try {
                    var pending=failed==null?ledger.get(id):failed;
                    if(pending!=null&&!"COMPLETED".equals(pending.getDrawStatus())) {
                        ledger.grantState(id,"FAILED",service.result(pending),false,false);
                        try{storage.log(ledger.get(id));}catch(Exception ignored){log.warn("券包中奖记录待人工补齐 request={}",id);}
                    }
                } finally {ledger.stopJob(id,token,"券包发放失败，待人工补发："+e.getClass().getSimpleName());}
            } else if(cashTransferAttempted&&(failed==null||("L"+id).equals(failed.getOutBillNo()))) {
                ledger.stopJob(id,token,"红包转账单号未落账，需人工核对");
                log.warn("Lottery cash transfer requires reconciliation: request={}, reason={}",id,e.getClass().getSimpleName());
            } else {
                ledger.retryJob(id,token,e.getClass().getSimpleName());
                log.warn("Lottery grant deferred: request={}, reason={}",id,e.getClass().getSimpleName());
            }
            return failed;}
        finally{
            BusinessContextHolder.setBusinessId(previousBusiness);
            long total=System.nanoTime()-started;
            if(cashDraw&&total>=TimeUnit.MILLISECONDS.toNanos(500))
                log.info("红包处理耗时 requestPk={}, totalMs={}, transferMs={}, localMs={}",id,
                        TimeUnit.NANOSECONDS.toMillis(total),TimeUnit.NANOSECONDS.toMillis(transferNanos),TimeUnit.NANOSECONDS.toMillis(total-transferNanos));
        }
    }
    /** 保持原红包按钮契约：lottery 返回前准备微信单与领取参数，到账确认仍异步。 */
    public LotteryLedger.Draw prepareCash(long id){return run(id);}
    @PreDestroy public void stop(){stopped=true;executor.shutdown();delayed.clear();}
}
