package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import jakarta.annotation.Resource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.*;

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
    @Resource
    private LotteryProperties properties;
    @DubboReference(timeout=3000,retries=0) private WxMemberApi members;
    private final ThreadPoolExecutor executor=new ThreadPoolExecutor(4,4,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"lottery-grant");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    @Scheduled(fixedDelay=500)
    public void poll() {
        int size=Math.max(1,Math.min(properties.getGrantConcurrency(),properties.getConnectionBudget()/2));
        if(size>executor.getMaximumPoolSize()){executor.setMaximumPoolSize(size);executor.setCorePoolSize(size);}
        else {executor.setCorePoolSize(size);executor.setMaximumPoolSize(size);}
        int free=Math.max(0,size-executor.getActiveCount());if(free==0)return;
        try {
            for(Long id:ledger.due(free))try{executor.execute(()->run(id));}catch(RejectedExecutionException e){break;}
        } catch(RuntimeException e){log.warn("Lottery work poll unavailable: {}",e.getClass().getSimpleName());}
    }
    private void run(long id) {
        String token=UUID.randomUUID().toString();
        if(!ledger.claim(id,token))return;
        Long previousBusiness=BusinessContextHolder.getBusinessId();
        try {
            LotteryLedger.Draw draw=ledger.get(id);
            BusinessContextHolder.setBusinessId(draw.getBusinessId());
            if(ledger.isReturnJob(id)) {
                if(storage.claimPhysicalReturn(draw)) {
                    ledger.confirmPhysicalReturn(id);draw=ledger.get(id);
                    if("RETURNED".equals(draw.getDrawStatus())) {
                        if(draw.getPointsCost()>0)members.refundLotteryPoints(draw.getMemberId(),draw.getOutBillNo());
                        stockCache.reconcile(draw);service.result(draw);
                    }
                }
                ledger.finishJob(id,token);return;
            }
            if("ACCEPTED".equals(draw.getDrawStatus())) {
                if(!draw.getSnapshot().isManualReissue()&&draw.getPointsCost()>0&&!members.changeLotteryPoints(draw.getMemberId(),draw.getOutBillNo()+":DEBIT",-draw.getPointsCost())) {
                    ledger.rejectUnpaid(id);draw=ledger.get(id);
                } else {
                    var result=service.result(draw);
                    int type=draw.getSnapshot().getPrize().getPrizeType();
                    if(type==5) {
                        ledger.grantState(id,"PROCESSING",result,false,false);
                        var response=cash.reconcileOrSend(draw);
                        if(response.getState()==null)throw new IllegalStateException("红包状态未知");
                        String state=response.getState().name();result.setPackageInfo(response.getPackageInfo());
                        if("SUCCESS".equals(state))ledger.grantState(id,"SUCCESS",result,true,true);
                        else if("FAIL".equals(state)||"CANCELLED".equals(state)) {
                            if(draw.getSnapshot().isManualReissue())ledger.grantState(id,"FAILED",result,true,false);
                            else if(Objects.equals(draw.getSnapshot().getPrize().getId(),draw.getSnapshot().getGuarantee().getId())) ledger.rejectUnpaid(id);
                            else ledger.fallback(id);
                        }
                        else {
                            ledger.grantState(id,state,result,false,false);
                            ledger.retryJob(id,token,"微信红包处理中");return;
                        }
                    } else {
                        if(type==1)storage.coupon(draw);
                        if(type==2&&!members.changeLotteryPoints(draw.getMemberId(),draw.getOutBillNo()+":AWARD",draw.getSnapshot().getPrize().getPrizeValue().intValueExact()))
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
                members.refundLotteryPoints(draw.getMemberId(),draw.getOutBillNo());
            if("COMPLETED".equals(draw.getDrawStatus()))storage.log(draw);
            if("ACCEPTED".equals(draw.getDrawStatus()))ledger.retryJob(id,token,"保底发放待处理");
            else ledger.finishJob(id,token);
        } catch(Exception e){ledger.retryJob(id,token,e.getClass().getSimpleName());log.warn("Lottery grant deferred: request={}, reason={}",id,e.getClass().getSimpleName());}
        finally{BusinessContextHolder.setBusinessId(previousBusiness);}
    }
    @PreDestroy public void stop(){executor.shutdown();}
}
