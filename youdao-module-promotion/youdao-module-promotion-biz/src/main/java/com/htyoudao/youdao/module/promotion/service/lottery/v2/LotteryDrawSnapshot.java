package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** 受理时固定配置快照，发奖不再读取可能变化的活动和奖品配置。 */
@Data
public class LotteryDrawSnapshot {
    private long acceptedAt = System.currentTimeMillis();
    private Long tenantId;
    private GoodCouponDO coupon;
    private GoodCouponDO guaranteeCoupon;
    private boolean fallbackApplied;
    /** 人工补发沿用奖品，不再次扣次数/积分；失败不自动切换其他奖品。 */
    private boolean manualReissue;
    private long reissueSequence;
    /** 仅原微信单明确失败后，人工补发才生成新的支付单号。 */
    private String reissueCashBillNo;
    private LotteryVO request;
    private LotterySettingsCacheDataVO settings;
    private WxMemberVO member;
    private LotteryPrizeDO prize;
    private LotteryPrizeDO guarantee;
    private String scopeKey;
    private List<Chance> chances = new ArrayList<>();
    @Data
    public static class Chance {
        private int source;
        private String scope;
        private long limit;
        private int pointsCost;
        private boolean earned;
        public Chance() {}
        public Chance(int source, String scope, long limit, int pointsCost, boolean earned) {
            this.source=source; this.scope=scope; this.limit=limit; this.pointsCost=pointsCost; this.earned=earned;
        }
    }
}
