package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsNumVo;
import java.util.*;

/** 展示与任务机会事务共用次数计算规则。 */
public final class LotteryQuota {
    private LotteryQuota() {}
    public static LotterySettingsNumVo calculate(LotterySettingsCacheDataVO cfg,Map<String,long[]> counters,String period,int balance,boolean applyLimits) {
        var result=new LotterySettingsNumVo();
        int total=remaining(zero(cfg.getLotteryTotalNumber()),get(counters,"TOTAL",0)[1]);result.setSum(total);
        boolean multi=on(cfg.getFreeStatus())||on(cfg.getPointsStatus())||on(cfg.getOrderStatus())||on(cfg.getShareEvent())||on(cfg.getBrowseType());
        String orderScope=on(cfg.getPlaceOrderLottery())?"TOTAL":period;
        String pointsScope=on(cfg.getPointsType())?"TOTAL":period;
        if(multi) {
            if(on(cfg.getFreeStatus()))result.setFreeAvailableCount(finite(zero(cfg.getFreeCount())-get(counters,period,1)[1]));
            if(on(cfg.getPointsStatus())&&balance>=zero(cfg.getPrice()))result.setPointsAvailableCount(on(cfg.getPointsType())?-1:finite(zero(cfg.getPointsCount())-get(counters,pointsScope,2)[1]));
            if(on(cfg.getOrderStatus())){result.setOrderAvailableCount(earned(counters,orderScope,3));result.setOrderFinishCount(finite(get(counters,orderScope,3)[2]));}
            if(on(cfg.getShareEvent())){result.setShareAvailableCount(earned(counters,period,4));result.setShareFinishCount(finite(get(counters,period,4)[2]));}
            if(on(cfg.getBrowseType())){result.setBrowseAvailableCount(earned(counters,period,5));result.setBrowseFinishCount(finite(get(counters,period,5)[2]));}
        } else {
            switch(zero(cfg.getLotteryMethod())) {
                case 1 -> result.setFreeAvailableCount(-1);
                case 2 -> result.setPointsAvailableCount(balance>=zero(cfg.getPrice())?-1:0);
                case 3 -> result.setOrderAvailableCount(earned(counters,period,3));
                default -> { }
            }
        }
        int[] sources={result.getFreeAvailableCount(),result.getPointsAvailableCount(),result.getOrderAvailableCount(),result.getShareAvailableCount(),result.getBrowseAvailableCount()};
        int available=Arrays.stream(sources).anyMatch(n->n<0)?-1:finite(Arrays.stream(sources).asLongStream().sum());
        if(applyLimits){available=cap(available,total);available=cap(available,remaining(zero(cfg.getLotteryLimit()),get(counters,period,0)[1]));}
        result.setNum(available);return result;
    }
    public static long[] get(Map<String,long[]> counters,String scope,int source){return counters.getOrDefault(scope+":"+source,new long[]{0,0,0});}
    private static int earned(Map<String,long[]> c,String scope,int source){long[] row=get(c,scope,source);return finite(row[0]-row[1]);}
    private static int remaining(int limit,long used){return limit<=0?-1:finite(limit-used);}
    private static int cap(int value,int limit){return limit<0?value:value<0?limit:Math.min(value,limit);}
    private static boolean on(Integer n){return Objects.equals(n,1);}
    private static int zero(Integer n){return n==null?0:n;}
    private static int finite(long n){return (int)Math.min(Integer.MAX_VALUE,Math.max(0,n));}
}
