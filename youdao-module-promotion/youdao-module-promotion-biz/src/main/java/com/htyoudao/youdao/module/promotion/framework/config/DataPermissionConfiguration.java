package com.htyoudao.youdao.module.promotion.framework.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponshare.CouponShareDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangecommodity.ExchangeCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponDataAnalysisBySourceDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponStoreIdDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dht
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // 活动
            rule.addBusinessColumn(GoodCouponPackageDO.class);
            rule.addBusinessColumn(CouponCommodityDO.class);
            rule.addBusinessColumn(CouponPackageDO.class);

            rule.addBusinessColumn(CouponStoreDO.class);
            rule.addBusinessColumn(GoodCouponDO.class);

            rule.addBusinessColumn(CouponShareDO.class);
            rule.addBusinessColumn(UserCouponDO.class);

            rule.addBusinessColumn(UserCouponPackageDO.class);
            rule.addBusinessColumn(UserCouponRecordDO.class);

            rule.addBusinessColumn(LotterySettingsDO.class);
            rule.addBusinessColumn(LotteryPrizeDO.class);
            rule.addBusinessColumn(LotteryLogDO.class);

            rule.addBusinessColumn(CouponChooseDO.class);
            rule.addBusinessColumn(CouponPackageDO.class);

//            rule.addBusinessColumn(SmsMarketDO.class);
//            rule.addBusinessColumn(SmsTemplateDO.class);
            rule.addBusinessColumn(UserCouponDataAnalysisBySourceDO.class);
            rule.addBusinessColumn(UserCouponStoreIdDO.class);

            rule.addBusinessColumn(ActivityDO.class);
            rule.addBusinessColumn(ActivitySeckillDO.class);
//            rule.addBusinessColumn(ActivityNjnzDO.class);
            rule.addBusinessColumn(ExchangeCommodityDO.class);

            rule.addBusinessColumn(ActivityExchangeLogDO.class);
        };
    }
}
