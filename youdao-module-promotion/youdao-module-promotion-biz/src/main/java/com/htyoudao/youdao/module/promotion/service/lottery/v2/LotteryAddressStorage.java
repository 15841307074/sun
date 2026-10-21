package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryLogVO;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** 校验身份后才进入地址写入短事务，不在事务内查询会员。 */
@Service
@DS("sharding")
public class LotteryAddressStorage {
    @Resource private LotteryLogMapper logs;
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public int update(LotteryLogVO request,LotteryLogDO original) {
        return logs.update(new LambdaUpdateWrapper<LotteryLogDO>().set(LotteryLogDO::getPrizeState,2)
                .set(LotteryLogDO::getReceiveAddress,request.getReceiveAddress()).set(LotteryLogDO::getReceiveUser,request.getMemberNickName())
                .set(LotteryLogDO::getReceiveMobile,request.getMemberMobile()).eq(LotteryLogDO::getId,original.getId())
                .eq(LotteryLogDO::getMemberId,original.getMemberId()).eq(LotteryLogDO::getBusinessId,BusinessContextHolder.getRequiredBusinessId())
                .eq(LotteryLogDO::getPrizeType,3).in(LotteryLogDO::getPrizeState,1,2)
                .and(t->t.isNull(LotteryLogDO::getTrackingNumber).or().eq(LotteryLogDO::getTrackingNumber,"")));
    }
}
