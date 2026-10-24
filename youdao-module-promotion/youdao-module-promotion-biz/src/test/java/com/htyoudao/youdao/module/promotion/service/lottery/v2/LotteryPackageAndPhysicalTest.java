package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryLogServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.LotteryController;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogExportRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogExportVO;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogQueryService;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.excel.EasyExcel;
import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryGrantStorageImpl;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import java.util.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class LotteryPackageAndPhysicalTest {
    @BeforeAll static void metadata(){TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),LotteryLogDO.class);}
    @Test void packagePartialSuccessManualRetryUsesTheSameIdsAndExpiration() {
        var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
        jdbc.execute("CREATE TABLE lottery_coupon_package_grant(business_id BIGINT,request_pk BIGINT,plan_json CLOB,state VARCHAR(16) DEFAULT 'PENDING',update_time TIMESTAMP,PRIMARY KEY(business_id,request_pk))");
        var mapper=mock(UserCouponMapper.class);var packages=mock(UserCouponPackageMapper.class);var receipts=mock(LotteryCouponGrantMapper.class);
        Map<Long,UserCouponDO> saved=new HashMap<>();Map<Long,UserCouponPackageDO> headers=new HashMap<>();var failOnce=new AtomicBoolean(true);
        when(receipts.coupon(anyLong(),anyLong())).thenAnswer(call->{var row=saved.get(call.getArgument(1,Long.class));return row==null?null:Map.of("business_id",row.getBusinessId(),"user_id",row.getUserId(),"coupon_id",row.getCouponId(),"package_id",row.getPackageId());});
        when(receipts.header(anyLong(),anyLong())).thenAnswer(call->{var row=headers.get(call.getArgument(1,Long.class));return row==null?null:Map.of("business_id",row.getBusinessId(),"user_id",row.getUserId(),"package_id",row.getPackageId());});
        when(mapper.insert(any(UserCouponDO.class))).thenAnswer(call->{UserCouponDO row=call.getArgument(0);if(row.getId()==1002L&&failOnce.getAndSet(false))throw new org.springframework.dao.QueryTimeoutException("test timeout");saved.put(row.getId(),row);return 1;});
        when(packages.insert(any(UserCouponPackageDO.class))).thenAnswer(call->{var row=call.getArgument(0,UserCouponPackageDO.class);headers.put(row.getId(),row);return 1;});
        var storage=new LotteryCouponPackageStorage();ReflectionTestUtils.setField(storage,"coupons",mapper);ReflectionTestUtils.setField(storage,"packages",packages);ReflectionTestUtils.setField(storage,"receipts",receipts);
        var service=new LotteryCouponPackageService();ReflectionTestUtils.setField(service,"jdbc",jdbc);ReflectionTestUtils.setField(service,"json",new ObjectMapper().registerModule(new JavaTimeModule()));ReflectionTestUtils.setField(service,"storage",storage);
        var draw=draw(6);var reward=new LotteryDrawSnapshot.PackageReward();reward.setPackageId(500L);
        for(long id:List.of(1001L,1002L)) {var copy=new LotteryDrawSnapshot.CouponCopy();copy.setId(id);var template=new GoodCouponDO();template.setId(55L);template.setUseType(1);template.setUseTime("7");copy.setTemplate(template);reward.getCoupons().add(copy);}
        draw.getSnapshot().setCouponPackage(reward);
        assertThrows(org.springframework.dao.QueryTimeoutException.class,()->service.grant(draw));
        assertEquals(1,saved.size());var expiration=saved.get(1001L).getExpirationTime();var created=saved.get(1001L).getCreateTime();
        service.grant(draw);service.grant(draw);
        assertEquals(Set.of(1001L,1002L),saved.keySet());assertEquals(1,headers.size());
        assertEquals(expiration,saved.get(1002L).getExpirationTime());assertEquals(created,saved.get(1002L).getCreateTime());
        assertEquals(40L,saved.get(1002L).getUserId());
        verify(mapper,times(1)).insert(argThat((UserCouponDO row)->row.getId()==1001L));
        assertEquals("SUCCESS",jdbc.queryForObject("SELECT state FROM lottery_coupon_package_grant",String.class));
    }
    @Test void packageFailureStopsWithoutFallbackRefundOrStockRelease() {
        var worker=new LotteryGrantWorker();var ledger=mock(LotteryLedger.class);var storage=mock(LotteryGrantStorage.class);var service=mock(LotteryV2Service.class);var packages=mock(LotteryCouponPackageService.class);var members=mock(WxMemberApi.class);
        ReflectionTestUtils.setField(worker,"ledger",ledger);ReflectionTestUtils.setField(worker,"storage",storage);ReflectionTestUtils.setField(worker,"service",service);ReflectionTestUtils.setField(worker,"couponPackages",packages);ReflectionTestUtils.setField(worker,"members",members);
        var draw=draw(6);when(ledger.claim(eq(100L),anyString())).thenReturn(true);when(ledger.get(100)).thenReturn(draw);when(service.result(draw)).thenReturn(new LotteryUserLogVO());
        doThrow(new IllegalStateException("partial issue")).when(packages).grant(draw);
        ReflectionTestUtils.invokeMethod(worker,"run",100L);
        verify(ledger).grantState(eq(100L),eq("FAILED"),any(),eq(false),eq(false));
        verify(ledger).stopJob(eq(100L),anyString(),contains("券包发放失败"));
        verify(ledger,never()).finishJob(anyLong(),anyString());verify(ledger,never()).fallback(anyLong());verify(ledger,never()).rejectUnpaid(anyLong());verifyNoInteractions(members);
        assertEquals("RESERVED",draw.getStockState());worker.stop();
    }
    @Test void physicalReturnDoesNotRefundPointsOrTouchCash() {
        var worker=new LotteryGrantWorker();var ledger=mock(LotteryLedger.class);var storage=mock(LotteryGrantStorage.class);var service=mock(LotteryV2Service.class);var members=mock(WxMemberApi.class);var stock=mock(LotteryStockCache.class);var cash=mock(LotteryCashGateway.class);
        ReflectionTestUtils.setField(worker,"ledger",ledger);ReflectionTestUtils.setField(worker,"storage",storage);ReflectionTestUtils.setField(worker,"service",service);ReflectionTestUtils.setField(worker,"members",members);ReflectionTestUtils.setField(worker,"stockCache",stock);ReflectionTestUtils.setField(worker,"cash",cash);
        var draw=draw(3);draw.setDrawStatus("COMPLETED");draw.setStockState("ISSUED");draw.setPointsCost(100);
        when(ledger.claim(eq(100L),anyString())).thenReturn(true);when(ledger.get(100)).thenReturn(draw);when(ledger.isReturnJob(100)).thenReturn(true);when(storage.claimPhysicalReturn(draw)).thenReturn(true);
        doAnswer(call->{draw.setDrawStatus("RETURNED");draw.setStockState("RETURNED");return null;}).when(ledger).confirmPhysicalReturn(100);
        ReflectionTestUtils.invokeMethod(worker,"run",100L);
        verify(stock).reconcile(draw);verify(ledger).finishJob(eq(100L),anyString());verifyNoInteractions(members,cash);worker.stop();
    }
    @Test void physicalReturnChecksPrecise48HoursAndNeverReclaimsAnAddress() {
        var storage=new LotteryGrantStorageImpl();var logs=mock(LotteryLogMapper.class);ReflectionTestUtils.setField(storage,"logs",logs);
        var draw=draw(3);var row=new LotteryLogDO();row.setPrizeType(3);row.setPrizeState(1);row.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusHours(49));
        when(logs.selectOne(any(Wrapper.class))).thenReturn(row);when(logs.update(any(LotteryLogDO.class),any(Wrapper.class))).thenReturn(1);
        assertTrue(storage.claimPhysicalReturn(draw));
        row.setReceiveAddress("已填写地址");assertFalse(storage.claimPhysicalReturn(draw));
        row.setReceiveAddress(null);row.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusHours(47));assertFalse(storage.claimPhysicalReturn(draw));
        verify(logs,times(1)).update(any(LotteryLogDO.class),any(Wrapper.class));
    }
    @Test void alipayCashSelectionUsesOnlyNonCashGuaranteeAndWechatKeepsCash() {
        var member=new WxMemberVO();var cash=new LotteryPrizeDO();cash.setPrizeType(5);var guarantee=new LotteryPrizeDO();guarantee.setPrizeType(6);
        member.setMemberCategory(1);assertSame(guarantee,LotteryPrizePolicy.forChannel(member,cash,guarantee));
        member.setMemberCategory(0);assertSame(cash,LotteryPrizePolicy.forChannel(member,cash,guarantee));
        guarantee.setPrizeType(5);assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,()->LotteryPrizePolicy.forChannel(member,cash,guarantee));
    }
    @Test void missingPhoneDoesNotFallBackToMemberId() {
        var identities=new LotteryIdentityService();var member=new WxMemberVO();member.setMemberId(40L);
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,()->identities.mobile(member));
        member.setMemberMobile("+8613800000000");assertEquals("13800000000",identities.mobile(member));
    }
    @Test void adminQueryKeepsPackageTypePrizeNameAndGrantState() {
        var service = new LotteryLogServiceImpl();
        var logs = mock(LotteryLogMapper.class);
        var settings = mock(LotterySettingsMapper.class);
        var stores = mock(StoreApi.class);
        ReflectionTestUtils.setField(service, "lotteryLogMapper", logs);
        ReflectionTestUtils.setField(service, "lotterySettingsMapper", settings);
        ReflectionTestUtils.setField(service, "storeApi", stores);
        ReflectionTestUtils.setField(service, "recordStates", mock(LotteryRecordStateService.class));
        var setting = new LotterySettingsDO();
        setting.setActivityId(21L);
        when(settings.selectOne(any(Wrapper.class))).thenReturn(setting);
        var row = new LotteryLogDO();
        row.setId(100L); row.setPrizeType(6); row.setPrizeName("午餐优惠券包"); row.setStoreId(30L);
        row.setGrantStatus("FAILED"); row.setRequestId("package-request"); row.setCanReissue(true);
        when(stores.getStoreByStoreId(30L)).thenReturn(CommonResult.success(null));
        when(logs.selectPage(any(PageParam.class), any(Wrapper.class)))
                .thenReturn(new PageResult<>(List.of(row), 1L));
        var controller = new LotteryController();
        ReflectionTestUtils.setField(controller, "lotteryLogService", service);
        var req = new LotteryLogReqVO();
        req.setLotteryId(21L); req.setPrizeType(6);
        var result = controller.getByLotteryLogList(req).getCheckedData();
        assertEquals(1L, result.getTotal());
        var item = result.getList().get(0);
        assertEquals(6, item.getPrizeType()); assertEquals("午餐优惠券包", item.getPrizeName());
        assertEquals("FAILED", item.getGrantStatus()); assertEquals("package-request", item.getRequestId());
        assertTrue(item.getCanReissue()); assertNull(item.getClaimStatus());
        var where = org.mockito.ArgumentCaptor.forClass(Wrapper.class);
        verify(logs).selectPage(any(PageParam.class), where.capture());
        var query = (AbstractWrapper<?, ?, ?>) where.getValue();
        assertTrue(query.getSqlSegment().contains("prize_type"));
        assertTrue(query.getParamNameValuePairs().containsValue(6));
    }

    @Test void exportPackageFilterAndExcelCellsKeepPackageNameAndExistingTypes() throws Exception {
        var service = new LotteryLogServiceImpl();
        var settings = mock(LotterySettingsMapper.class);
        var query = mock(LotteryLogQueryService.class);
        var excel = mock(ExcelActionService.class);
        ReflectionTestUtils.setField(service, "lotterySettingsMapper", settings);
        ReflectionTestUtils.setField(service, "lotteryLogQueryService", query);
        ReflectionTestUtils.setField(service, "lotteryLogMapper", mock(LotteryLogMapper.class));
        ReflectionTestUtils.setField(service, "logExportRespVOExcelActionService", excel);
        var setting = new LotterySettingsDO();
        setting.setActivityId(21L);
        when(settings.selectOne(any(Wrapper.class))).thenReturn(setting);
        var row = new LotteryLogDO(); row.setMemberId(40L); row.setPrizeType(6); row.setPrizeName("午餐优惠券包");
        when(query.selectPage(any(), any())).thenReturn(new Page<LotteryLogDO>().setRecords(List.of(row)));
        doAnswer(call -> {
            Function<Page<LotteryLogExportRespVO>, List<LotteryLogExportRespVO>> supplier = call.getArgument(2);
            var rows = supplier.apply(call.getArgument(1));
            var bytes = new ByteArrayOutputStream();
            EasyExcel.write(bytes, LotteryLogExportRespVO.class).sheet().doWrite(rows);
            List<Map<Integer, String>> cells = EasyExcel.read(new ByteArrayInputStream(bytes.toByteArray())).sheet().doReadSync();
            assertEquals("优惠券包", cells.get(0).get(3));
            assertEquals("午餐优惠券包", cells.get(0).get(4));
            assertNull(rows.get(0).getClaimName());
            String[] names = {"优惠券", "积分", "实物", "无奖品", "现金红包"};
            for (int type = 1; type <= 5; type++) {
                row.setPrizeType(type);
                if (type == 5) row.setClaimStatus(1);
                assertEquals(names[type - 1], supplier.apply(call.getArgument(1)).get(0).getPrizeTypeName());
            }
            return null;
        }).when(excel).exportAsyncExcel(eq(LotteryLogExportRespVO.class), any(Page.class), any(Function.class), anyString(), anyList());
        var req = new LotteryLogExportVO();
        req.setLotteryId(21L); req.setPrizeType(6);
        try {
            BusinessContextHolder.setBusinessId(10L);
            service.exportLotteryList(req, null, null);
        } finally {
            BusinessContextHolder.clear();
        }
        var where = org.mockito.ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(query, times(6)).selectPage(any(), where.capture());
        var wrapper = where.getValue();
        assertTrue(wrapper.getSqlSegment().contains("prize_type"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(6));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(10L));
    }

    private LotteryLedger.Draw draw(int type) {
        var draw=new LotteryLedger.Draw();draw.setId(100);draw.setBusinessId(10);draw.setMemberId(40);draw.setStoreId(30);draw.setActivityId(21);draw.setDrawStatus("ACCEPTED");draw.setGrantStatus("PENDING");draw.setStockState("RESERVED");
        var s=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setId(101L);prize.setPrizeType(type);s.setPrize(prize);s.setGuarantee(prize);var member=new WxMemberVO();member.setMemberId(40L);member.setMemberMobile("13800000000");s.setMember(member);draw.setSnapshot(s);return draw;
    }
}
