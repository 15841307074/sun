package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryReissueReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryReissueRespVO;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import java.util.Objects;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_REISSUE_REJECTED;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.SYSTEM_LOTTERY_LOG_TYPE;

@Service("lotteryReissueService")
public class LotteryReissueServiceImpl implements LotteryReissueService {
    @Resource private LotteryLedger ledger;
    @Resource private LotteryV2Service results;
    @Resource private LotteryResultCache cache;
    @Resource private LotteryCashGateway cash;
    @DubboReference(timeout=3000,retries=0) private WxMemberApi members;

    @LogRecord(type=SYSTEM_LOTTERY_LOG_TYPE,subType="抽奖人工补发",bizNo="{{#reqVO.requestId}}",
            success="人工补发请求{{#reqVO.requestId}}，会员{{#reqVO.memberId}}，原因：{{#reqVO.reason}}")
    @Override
    public LotteryReissueRespVO reissue(LotteryReissueReqVO reqVO) {
        long b=BusinessContextHolder.getRequiredBusinessId();
        var draw=ledger.findByReference(b,reqVO.getLotteryId(),reqVO.getMemberId(),reqVO.getRequestId());
        if(draw==null)throw exception(LOTTERY_REISSUE_REJECTED,"原 V2 抽奖流水不存在或不属于当前项目/会员");
        if(draw.getStoreId()!=reqVO.getStoreId())throw exception(LOTTERY_REISSUE_REJECTED,"门店与原抽奖不一致");
        if("RETURNED".equals(draw.getDrawStatus())||"RETURNED".equals(draw.getStockState()))throw exception(LOTTERY_REISSUE_REJECTED,"已退奖记录不能补发");
        if("COMPLETED".equals(draw.getDrawStatus())||"SUCCESS".equals(draw.getGrantStatus()))
            return new LotteryReissueRespVO(false,"奖品已发放，不重复补发",results.result(draw));
        // 原失败退款可能因服务异常尚未完成。幂等补齐原退款，而不是再扣一次。
        if("FAILED".equals(draw.getDrawStatus())&&!draw.getSnapshot().isManualReissue()&&draw.getPointsCost()>0)
            members.refundLotteryPoints(draw.getMemberId(),draw.getOutBillNo());
        boolean cashFailed=false;
        if(Objects.equals(draw.getSnapshot().getPrize().getPrizeType(),5)) {
            var remote=cash.queryForReissue(draw); // 不占库存事务，不触发支付。
            if(remote!=null) {
                if(remote.getState()==null)throw new IllegalStateException("红包状态未知，不能安排补发");
                String state=remote.getState().name();
                cashFailed="FAIL".equals(state)||"CANCELLED".equals(state);
                // 未结束的转账及已成功转账沿用旧单，仅让原任务查单/补齐记录。
            }
        }
        // 先清理旧结果并阻止旧查询回写；Redis 不可用时不提交补发，避免安排后仍长期返回旧失败缓存。
        // 若数据库拒绝/另一操作已处理，缓存暂时不回填旧版本，查询仍可读取持久化流水。
        cache.invalidateForReissue(draw,Math.addExact(draw.getSnapshot().getReissueSequence(),1));
        var scheduled=ledger.reissue(b,draw.getId(),reqVO.getMemberId(),reqVO.getStoreId(),reqVO.getRequestId(),
                draw.cashBillNo(),draw.getSnapshot().getReissueSequence(),cashFailed);
        if(scheduled.draw().getSnapshot().isManualReissue())cache.invalidateForReissue(scheduled.draw());
        return new LotteryReissueRespVO(scheduled.queued(),scheduled.message(),results.result(scheduled.draw()));
    }
}
