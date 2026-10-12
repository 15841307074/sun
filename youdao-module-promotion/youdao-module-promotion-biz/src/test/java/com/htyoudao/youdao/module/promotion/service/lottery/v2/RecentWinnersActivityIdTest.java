package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.AppLotteryController;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryRecentWinnerRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecentWinnersActivityIdTest {
    private LotteryConfigurationCacheImpl cache;
    private LotterySettingsMapper settings;
    private ActivityMapper activities;
    private StringRedisTemplate redis;
    private ValueOperations<String,String> values;
    private LotterySettingsDO cfg;
    private ActivityDO activity;
    private Long previousBusiness;
    @BeforeEach void setup(){
        previousBusiness=BusinessContextHolder.getBusinessId();BusinessContextHolder.setBusinessId(10L);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"recent-winners-test"),LotterySettingsDO.class);
        cache=new LotteryConfigurationCacheImpl();settings=mock(LotterySettingsMapper.class);activities=mock(ActivityMapper.class);redis=mock(StringRedisTemplate.class);values=mock(ValueOperations.class);
        ReflectionTestUtils.setField(cache,"settingsMapper",settings);ReflectionTestUtils.setField(cache,"activityMapper",activities);ReflectionTestUtils.setField(cache,"redis",redis);ReflectionTestUtils.setField(cache,"json",new ObjectMapper());
        ReflectionTestUtils.setField(cache,"stores",mock(ActivityStoreService.class));ReflectionTestUtils.setField(cache,"scope",mock(LotteryScopeService.class));
        cfg=new LotterySettingsDO();cfg.setId(20L);cfg.setBusinessId(10L);cfg.setActivityId(30L);cfg.setConfigVersion(1L);cfg.setStockEpoch(1L);cfg.setRuntimeVersion(2);
        activity=new ActivityDO();activity.setId(30L);activity.setBusinessId(10L);
        when(redis.opsForValue()).thenReturn(values);when(values.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenReturn(true);
        when(redis.execute(any(RedisScript.class),anyList(),any(Object[].class))).thenReturn(1L);
        when(activities.selectById(30L)).thenReturn(activity);
        when(settings.selectOne(any())).thenAnswer(call->{AbstractWrapper<?,?,?> query=call.getArgument(0);String sql=query.getSqlSegment();return sql.contains(" OR ")||query.getParamNameValuePairs().containsValue(30L)?cfg:null;});
    }
    @AfterEach void cleanup(){BusinessContextHolder.setBusinessId(previousBusiness);}
    @Test void requiredActivityParameterBindsLongAndPreservesResponseShape() throws Exception {
        long activityId=900000000000000123L;
        var feed=mock(LotteryWinnerFeed.class);var controller=new AppLotteryController();ReflectionTestUtils.setField(controller,"winnerFeed",feed);
        var winner=new LotteryRecentWinnerRespVO();winner.setMemberMobile("138****1234");winner.setPrizeName("积分奖品");when(feed.latest(activityId)).thenReturn(List.of(winner));
        MockMvcBuilders.standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter()).build()
                .perform(get("/promotion/lottery-mobile/recent-winners").param("activityId",Long.toString(activityId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data[0].memberMobile").value("138****1234")).andExpect(jsonPath("$.data[0].prizeName").value("积分奖品"));
        verify(feed).latest(activityId);
    }
    @Test void removedLotteryIdParameterAloneReturnsBadRequest() throws Exception {
        var feed=mock(LotteryWinnerFeed.class);var controller=new AppLotteryController();ReflectionTestUtils.setField(controller,"winnerFeed",feed);
        MockMvcBuilders.standaloneSetup(controller).build().perform(get("/promotion/lottery-mobile/recent-winners").param("lotteryId","20")).andExpect(status().isBadRequest());verifyNoInteractions(feed);
    }
    @Test void configurationLookupUsesActivityOnlyAndCurrentProject(){
        var result=cache.settingsByActivity(30);assertEquals(30L,result.getActivityId());assertEquals(20L,result.getId());
        var capture=org.mockito.ArgumentCaptor.forClass(AbstractWrapper.class);verify(settings).selectOne(capture.capture());String sql=capture.getValue().getSqlSegment();
        assertTrue(sql.contains("activity_id"));assertFalse(sql.contains(" OR "));assertEquals(Set.of(10L,30L),new HashSet<>(capture.getValue().getParamNameValuePairs().values()));
    }
    @Test void configurationIdCannotBeUsedAsActivityId(){assertThrows(ServiceException.class,()->cache.settingsByActivity(20));}
    @Test void existingOtherEndpointsCanStillResolveConfigurationId(){assertEquals(30L,cache.settings(20).getActivityId());}
    @Test void warmActivityCacheDoesNotQueryDatabase() throws Exception {
        when(values.get(LotteryKeys.key(10,0,"activity-mapping",30))).thenReturn("30");when(values.get(LotteryKeys.key(10,30,"config-version",0))).thenReturn("1");
        var cached=new LotterySettingsCacheDataVO();cached.setId(20L);cached.setActivityId(30L);when(values.get(LotteryKeys.key(10,30,"config", "1"))).thenReturn(new ObjectMapper().writeValueAsString(cached));
        assertEquals(30L,cache.settingsByActivity(30).getActivityId());verifyNoInteractions(settings,activities);
    }
    @Test void invalidActivityAliasCannotRedirectToAnotherActivity(){
        when(values.get(LotteryKeys.key(10,0,"activity-mapping",30))).thenReturn("31");assertEquals(30L,cache.settingsByActivity(30).getActivityId());verify(values,never()).get(LotteryKeys.key(10,31,"config-version",0));
    }
    @Test void otherBusinessActivityIsRejected(){activity.setBusinessId(11L);assertThrows(ServiceException.class,()->cache.settingsByActivity(30));}
    @Test void winnerFeedUsesActivityLookupAndActivityScopedCache(){
        var configuration=mock(LotteryConfigurationCache.class);var feed=new LotteryWinnerFeedImpl();var cfgView=new LotterySettingsCacheDataVO();cfgView.setId(20L);cfgView.setActivityId(30L);
        when(configuration.settingsByActivity(30L)).thenReturn(cfgView);ReflectionTestUtils.setField(feed,"configuration",configuration);ReflectionTestUtils.setField(feed,"redis",redis);
        var zset=mock(ZSetOperations.class);when(redis.opsForZSet()).thenReturn(zset);when(redis.hasKey(anyString())).thenReturn(true);
        assertTrue(feed.latest(30L).isEmpty());verify(configuration).settingsByActivity(30L);verify(configuration,never()).settings(anyLong());
        verify(zset).reverseRange(LotteryKeys.key(10,30,"winners","recent"),0,19);
    }
    @Test void invalidActivityIdDoesNotQueryAnyData(){
        var feed=new LotteryWinnerFeedImpl();var configuration=mock(LotteryConfigurationCache.class);ReflectionTestUtils.setField(feed,"configuration",configuration);
        assertThrows(IllegalArgumentException.class,()->feed.latest(null));assertThrows(IllegalArgumentException.class,()->feed.latest(0L));assertThrows(IllegalArgumentException.class,()->feed.latest(-1L));verifyNoInteractions(configuration);
    }
}
