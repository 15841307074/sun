package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryLogVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryRecordPlatformFilterTest {
    private LotteryMobileServiceImpl service;
    private LotteryLogMapper logs;
    private WxMemberApi members;
    private LotteryRecordStateService states;
    private LotteryLogVO request;

    @BeforeEach void setup() {
        for (var type : List.of(LotteryLogDO.class, LotterySettingsDO.class)) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), type);
        }
        BusinessContextHolder.setBusinessId(10L);
        service = new LotteryMobileServiceImpl();
        logs = mock(LotteryLogMapper.class);
        members = mock(WxMemberApi.class);
        states = mock(LotteryRecordStateService.class);
        var identities = mock(LotteryIdentityService.class);
        when(identities.mobile(50L)).thenReturn("13800000000");
        when(identities.recordPhones("13800000000")).thenReturn(List.of("13800000000", "+8613800000000"));
        var settings = mock(LotterySettingsMapper.class);
        var activity = new LotterySettingsDO(); activity.setId(20L); activity.setActivityId(30L);
        when(settings.selectOne(any())).thenReturn(activity);
        ReflectionTestUtils.setField(service, "lotteryLogMapper", logs);
        ReflectionTestUtils.setField(service, "lotterySettingsMapper", settings);
        ReflectionTestUtils.setField(service, "lotteryIdentityService", identities);
        ReflectionTestUtils.setField(service, "lotteryRecordStateService", states);
        ReflectionTestUtils.setField(service, "wxMemberApi", members);
        request = new LotteryLogVO(); request.setMemberId(50L);
    }

    @AfterEach void cleanup() { BusinessContextHolder.clear(); }

    @Test void alipayHidesOnlyRedPacketsInBothQueryModesAndKeepsSharedPhoneScope() {
        checkPlatform(1, List.of(1L, 2L, 3L, 4L, 6L));
    }

    @Test void wechatKeepsAllPrizeTypesAndExistingRedPacketExpiryInBothQueryModes() {
        checkPlatform(0, List.of(1L, 2L, 3L, 4L, 5L, 6L));
    }

    @Test void unknownCategoryKeepsOriginalRecords() {
        checkPlatform(null, List.of(1L, 2L, 3L, 4L, 5L, 6L));
    }

    private void checkPlatform(Integer category, List<Long> expected) {
        var member = new WxMemberVO(); member.setMemberCategory(category);
        when(members.getWxMemberById(50L)).thenReturn(CommonResult.success(member));
        for (Long lotteryId : Arrays.asList(null, 20L)) {
            request.setLotteryId(lotteryId);
            var rows = new ArrayList<LotteryLogDO>();
            for (int type = 1; type <= 6; type++) {
                var row = new LotteryLogDO(); row.setId((long) type); row.setPrizeType(type);
                row.setMemberId(40L); row.setMemberMobile("13800000000");
                row.setClaimStatus(1); row.setCreateTime(LocalDateTime.now().minusHours(25));
                rows.add(row);
            }
            doAnswer(call -> {
                var wrapper = call.getArgument(0, AbstractWrapper.class);
                String sql = wrapper.getSqlSegment();
                assertTrue(sql.contains("member_mobile"));
                assertTrue(sql.contains("business_id"));
                assertTrue(sql.contains("deleted"));
                assertTrue(sql.contains("ORDER BY create_time DESC"));
                assertTrue(wrapper.getParamNameValuePairs().containsValue("13800000000"));
                assertTrue(wrapper.getParamNameValuePairs().containsValue(50L));
                return Collections.unmodifiableList(rows);
            }).when(logs).selectLotteryRows(any());
            var result = service.getLotteryLogByMemberId(request);
            assertEquals(expected, result.stream().map(LotteryLogDO::getId).toList());
            verify(states).enrich(result);
            clearInvocations(states);
            assertEquals(category != null && category == 1 ? 1 : 3, rows.get(4).getClaimStatus());
        }
    }

    @Test void emptyRecordsDoNotFetchMember() {
        when(logs.selectLotteryRows(any())).thenReturn(List.of());
        assertTrue(service.getLotteryLogByMemberId(request).isEmpty());
        verifyNoInteractions(members);
    }

    @Test void missingMemberKeepsEmptyResponse() {
        request.setMemberId(null);
        assertTrue(service.getLotteryLogByMemberId(request).isEmpty());
        verifyNoInteractions(logs, members, states);
    }
}
