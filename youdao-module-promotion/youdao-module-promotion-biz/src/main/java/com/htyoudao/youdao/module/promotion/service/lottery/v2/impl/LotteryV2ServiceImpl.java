package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.Objects;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service("lotteryV2Service")
@Slf4j
public class LotteryV2ServiceImpl implements LotteryV2Service {
    @Resource
    private LotteryLedger ledger;
    @Resource
    private LotteryConfigurationCache cache;
    @Resource
    private LotteryAdmission admission;
    @Resource
    private LotteryStockCache stock;
    @Resource
    private LotteryProperties properties;
    @Resource
    private LotteryResultCache results;
    @Resource @Lazy private LotteryMobileServiceImpl legacy;

    @Override
    public boolean enabled(LotterySettingsCacheDataVO cfg) {return Objects.equals(cfg.getRuntimeVersion(),2);}
    /** 接口查询结果未命中后调用；事务仍校验幂等。 */
    @Override
    public LotteryUserLogVO drawAfterResultMiss(LotteryVO req,LotterySettingsCacheDataVO cfg) {
        validateRequest(req);
        long b=BusinessContextHolder.getRequiredBusinessId();
        // 历史记录仍可查询；新抽奖须先重新启用并进入 V2。
        if(!enabled(cfg))throw new IllegalArgumentException("历史活动请在管理端重新启用，初始化后使用新版抽奖流程");
        if(!properties.isAccepting())throw exception(LOTTERY_SYSTEM_AGAIN);
        try(var permit=admission.enter("draw",b,cfg.getActivityId(),req.getMemberId())) {
            LotteryDrawSnapshot snapshot=legacy.prepareV2Draw(req,cfg,cache.prizes(cfg,req.getStoreId()));
            LotteryStockCache.Reservation reserved=null;
            try {
                if(!Objects.equals(snapshot.getPrize().getIsGuarantees(),1))reserved=stock.reserve(b,snapshot);
                LotteryLedger.Draw accepted=ledger.accept(b,snapshot);
                if(accepted.isNewlyAccepted())commit(reserved);else reconcile(reserved);
                return result(accepted);
            } catch(LotteryLedger.UnavailablePrize noStock) {
                reconcile(reserved);if(reserved!=null){reserved.close();reserved=null;}
                snapshot.setPrize(snapshot.getGuarantee());
                snapshot.setCoupon(snapshot.getGuaranteeCoupon());
                return result(ledger.accept(b,snapshot));
            } catch(DuplicateKeyException duplicate) {
                reconcile(reserved);
                LotteryLedger.Draw accepted=ledger.find(b,cfg.getActivityId(),req.getMemberId(),req.getRequestId());
                if(accepted==null)throw duplicate;
                return sameRequest(accepted,req);
            } catch(RuntimeException failed) {
                // 提交结果不确定时查询持久化流水，不盲目回退库存。
                reconcile(reserved);
                LotteryLedger.Draw accepted=ledger.find(b,cfg.getActivityId(),req.getMemberId(),req.getRequestId());
                if(accepted!=null)return sameRequest(accepted,req);
                throw failed;
            } finally {if(reserved!=null)reserved.close();}
        }
    }
    @Override
    public LotteryUserLogVO query(LotteryVO req) {
        validateRequest(req);long b=BusinessContextHolder.getRequiredBusinessId();
        try(var permit=admission.enter("result",b,0,req.getMemberId())) {
            var cached=results.get(b,req);
            if(cached!=null) {
                if(cached.storeId()!=req.getStoreId())throw new IllegalArgumentException("同一 requestId 不能更换门店");
                return cached.result();
            }
            LotteryLedger.Draw draw=ledger.findByReference(b,req.getLotteryId(),req.getMemberId(),req.getRequestId());
            return draw==null?null:sameRequest(draw,req);
        }
    }
    private void validateRequest(LotteryVO req) {
        if(req==null||req.getLotteryId()==null||req.getMemberId()==null||req.getStoreId()==null||req.getRequestId()==null||!req.getRequestId().matches("[A-Za-z0-9_-]{8,64}"))
            throw new IllegalArgumentException("lotteryId、memberId、storeId、requestId 必填；requestId 为 8-64 位字母数字、下划线或短横线");
    }
    private LotteryUserLogVO sameRequest(LotteryLedger.Draw draw,LotteryVO req) {
        if(draw.getStoreId()!=req.getStoreId())throw new IllegalArgumentException("同一 requestId 不能更换门店");
        return result(draw);
    }
    @Override
    public LotteryUserLogVO result(LotteryLedger.Draw draw) {
        LotteryUserLogVO result=draw.getResult();
        if(result==null){result=new LotteryUserLogVO();var prize=draw.getSnapshot().getPrize();result.setLotteryPrizeId(prize.getId());result.setPrizeName(prize.getPrizeName());result.setPrizeType(prize.getPrizeType());result.setPrizeImgUrl(prize.getPrizeImgUrl());}
        result.setRequestId(draw.getRequestId());result.setGrantStatus(draw.getGrantStatus());
        if(Objects.equals(draw.getSnapshot().getPrize().getPrizeType(),5))result.setOutBillNo(draw.cashBillNo());
        results.publish(draw,result);
        return result;
    }
    private void reconcile(LotteryStockCache.Reservation reservation) {
        if(reservation!=null)try{reservation.reconcile();}catch(RuntimeException e){log.warn("Lottery stock cache needs recovery: {}",e.getClass().getSimpleName());}
    }
    private void commit(LotteryStockCache.Reservation reservation) {
        // Redis 确认失败不能撤销已受理请求；异常标记或缓存缺失会触发恢复。
        if(reservation!=null)try{reservation.commit();}catch(RuntimeException e){log.warn("Lottery stock cache commit deferred: {}",e.getClass().getSimpleName());}
    }
}
