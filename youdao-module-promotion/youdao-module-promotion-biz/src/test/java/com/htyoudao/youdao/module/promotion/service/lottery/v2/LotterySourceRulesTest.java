package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LotterySourceRulesTest {
    @Test void freeOnlyIsValidWithoutAnyTask() {
        var cfg=new LotterySettingsCacheDataVO();cfg.setFreeStatus(1);cfg.setFreeCount(3);
        assertTrue(LotteryQuota.hasSource(cfg));assertEquals(3,LotteryQuota.calculate(cfg,Map.of(),"day",0,true).getNum());
    }
    @Test void pointsOnlyIsValidAndBalanceIsCheckedSeparately() {
        var cfg=new LotterySettingsCacheDataVO();cfg.setPointsStatus(1);cfg.setPointsType(1);cfg.setPrice(10);
        assertTrue(LotteryQuota.hasSource(cfg));assertEquals(-1,LotteryQuota.calculate(cfg,Map.of(),"day",20,true).getNum());
        assertEquals(0,LotteryQuota.calculate(cfg,Map.of(),"day",9,true).getNum());
    }
    @Test void absentSourceAndInvalidLegacyMethodCannotBecomeUnlimitedFree() {
        var cfg=new LotterySettingsCacheDataVO();assertFalse(LotteryQuota.hasSource(cfg));
        cfg.setLotteryMethod(0);assertFalse(LotteryQuota.hasSource(cfg));
        cfg.setLotteryMethod(1);assertTrue(LotteryQuota.hasSource(cfg));
    }
    @Test void zeroFreeAllowanceDoesNotGiveAnOpportunity() {
        var cfg=new LotterySettingsCacheDataVO();cfg.setFreeStatus(1);cfg.setFreeCount(0);
        assertEquals(0,LotteryQuota.calculate(cfg,Map.of(),"day",100,true).getNum());
    }
    @Test void resetUsesCurrentSessionAndKeepsTheActivityTotalCap() {
        var cfg=new LotterySettingsCacheDataVO();cfg.setOrderStatus(1);cfg.setPlaceOrderLottery(1);cfg.setLotteryRulesReset(1);cfg.setLotteryTotalNumber(5);
        var counters=Map.of("TOTAL:0",new long[]{0,2,0},"old:3",new long[]{10,0,10},"now:3",new long[]{1,0,1});
        var quota=LotteryQuota.calculate(cfg,counters,"now",0,true);
        assertEquals(1,quota.getNum());assertEquals(1,quota.getOrderFinishCount());assertEquals(3,quota.getSum());
        assertEquals("now",LotteryQuota.sourceScope(cfg,"now",true));
        cfg.setLotteryRulesReset(0);assertEquals("TOTAL",LotteryQuota.sourceScope(cfg,"now",true));
    }
    @Test void allDayResetStillChangesNaturallyAtMidnight() {
        var cfg=new LotterySettingsCacheDataVO();cfg.setLotteryRulesReset(1);cfg.setCalculationRules(1);cfg.setTimeRange("00:00-24:00");
        Object scope=ReflectionTestUtils.invokeMethod(new LotteryMobileServiceImpl(),"buildTaskScope",cfg);
        assertEquals(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),ReflectionTestUtils.getField(scope,"scopeKey"));
    }
}
