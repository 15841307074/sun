package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryGrantStorageImpl;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCashLogTest {
    private LotteryGrantStorageImpl storage;
    private LotteryLogMapper logs;
    private LotteryLedger ledger;
    private LotteryWinnerFeed feed;
    private LotteryLedger.Draw draw;
    @BeforeEach void setup() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(new com.baomidou.mybatisplus.core.MybatisConfiguration(),"cash-log-test"),LotteryLogDO.class);
        storage=new LotteryGrantStorageImpl();logs=mock(LotteryLogMapper.class);ledger=mock(LotteryLedger.class);feed=mock(LotteryWinnerFeed.class);
        ReflectionTestUtils.setField(storage,"logs",logs);ReflectionTestUtils.setField(storage,"ledger",ledger);ReflectionTestUtils.setField(storage,"winnerFeed",feed);
        draw=new LotteryLedger.Draw();draw.setId(100);draw.setBusinessId(10);draw.setActivityId(21);draw.setMemberId(40);draw.setOutBillNo("L100");draw.setDrawStatus("ACCEPTED");draw.setGrantStatus("PENDING");
        var snapshot=new LotteryDrawSnapshot();var settings=new LotterySettingsCacheDataVO();settings.setId(20L);settings.setLotteryType(1);snapshot.setSettings(settings);snapshot.setMember(new WxMemberVO());
        var prize=new LotteryPrizeDO();prize.setId(101L);prize.setPrizeType(5);prize.setIsGuarantees(0);snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        when(ledger.get(100)).thenReturn(draw);when(logs.update(any(LotteryLogDO.class),any())).thenReturn(1);
    }
    @Test void pendingCashWinnerIsRecordedBeforeWechatSuccess() {
        storage.log(draw);var captured=ArgumentCaptor.forClass(LotteryLogDO.class);verify(logs).insert(captured.capture());
        assertNull(captured.getValue().getOutBillNo());assertEquals(1,captured.getValue().getClaimStatus());assertNull(captured.getValue().getPackageInfo());verifyNoInteractions(feed);
    }
    @Test void waitingCashWritesThePackageIntoTheExistingRecord() {
        existing();draw.setGrantStatus("WAIT_USER_CONFIRM");draw.setOutBillNo("activity21Awechatbill");var result=new LotteryUserLogVO();result.setPackageInfo("test-package");result.setOutBillNo(draw.getOutBillNo());draw.setResult(result);
        storage.log(draw);var captured=ArgumentCaptor.forClass(LotteryLogDO.class);verify(logs).update(captured.capture(),any());
        assertEquals("test-package",captured.getValue().getPackageInfo());assertEquals("activity21Awechatbill",captured.getValue().getOutBillNo());assertEquals(1,captured.getValue().getClaimStatus());verifyNoInteractions(feed);
    }
    @Test void successfulCashUpdatesClaimStatusInsteadOfIgnoringTheDuplicate() {
        existing();draw.setGrantStatus("SUCCESS");draw.setDrawStatus("COMPLETED");
        storage.log(draw);var captured=ArgumentCaptor.forClass(LotteryLogDO.class);verify(logs).update(captured.capture(),any());assertEquals(2,captured.getValue().getClaimStatus());verify(feed).afterLogSaved(eq(21L),any());
    }
    @Test void staleManualReissueCannotOverwriteANewerBill() {
        existing();var latest=new LotteryLedger.Draw();latest.setSnapshot(new LotteryDrawSnapshot());latest.getSnapshot().setReissueSequence(2);when(ledger.get(100)).thenReturn(latest);
        storage.log(draw);verify(logs,never()).update(any(LotteryLogDO.class),any());verifyNoInteractions(feed);
    }
    @Test void cashFallbackReplacesThePendingRecordWithTheReturnedGuaranteePrize() {
        existing();var guarantee=new LotteryPrizeDO();guarantee.setId(202L);guarantee.setPrizeType(4);guarantee.setIsGuarantees(1);
        draw.getSnapshot().setFallbackApplied(true);draw.getSnapshot().setPrize(guarantee);
        storage.log(draw);var captured=ArgumentCaptor.forClass(LotteryLogDO.class);verify(logs).update(captured.capture(),any());
        assertEquals(202L,captured.getValue().getLotteryPrizeId());assertEquals(4,captured.getValue().getPrizeType());assertNull(captured.getValue().getOutBillNo());assertNull(captured.getValue().getPackageInfo());
    }
    private void existing() {
        var previous=new LotteryLogDO();previous.setId(100L);previous.setBusinessId(10L);previous.setMemberId(40L);previous.setPrizeType(5);
        when(logs.insert(any(LotteryLogDO.class))).thenThrow(new DuplicateKeyException("same draw"));when(logs.selectOne(any())).thenReturn(previous);
    }
}
