package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import java.util.Objects;
/** 在预占库存前确定可发放的奖品，渠道来自服务端会员资料。 */
public final class LotteryPrizePolicy {
    private LotteryPrizePolicy(){}
    public static LotteryPrizeDO forChannel(WxMemberVO member,LotteryPrizeDO selected,LotteryPrizeDO guarantee) {
        if(guarantee==null||Objects.equals(guarantee.getPrizeType(),5))throw LotteryErrors.invalid("兜底奖品必须是非红包奖品");
        return Objects.equals(member.getMemberCategory(),1)&&Objects.equals(selected.getPrizeType(),5)?guarantee:selected;
    }
}
