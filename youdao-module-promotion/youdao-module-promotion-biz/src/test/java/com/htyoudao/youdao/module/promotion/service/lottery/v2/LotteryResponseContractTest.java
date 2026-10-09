package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryV2ServiceImpl;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryResultCacheImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicReference;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryResponseContractTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void oldPersistedResultCanBeReadWithoutExposingDrawStatus() throws Exception {
        LotteryUserLogVO result = json.readValue("""
                {"requestId":"request_123","drawStatus":"COMPLETED","grantStatus":"SUCCESS",
                 "lotteryPrizeId":100,"prizeName":"100积分","prizeType":2}
                """, LotteryUserLogVO.class);
        JsonNode response = json.readTree(json.writeValueAsString(result));
        assertFalse(response.has("drawStatus"));
        assertEquals("request_123", result.getRequestId());
        assertEquals("SUCCESS", result.getGrantStatus());
        assertEquals("100积分", result.getPrizeName());
    }

    @Test
    void initiallyAcceptedDrawStillReturnsPrizeWithoutDrawStatus() throws Exception {
        LotteryV2Service service = new LotteryV2ServiceImpl();
        ReflectionTestUtils.setField(service, "results", mock(LotteryResultCache.class));
        LotteryLedger.Draw draw = draw("ACCEPTED");
        LotteryPrizeDO prize = new LotteryPrizeDO();
        prize.setId(100L); prize.setPrizeName("100积分"); prize.setPrizeType(2);
        LotteryDrawSnapshot snapshot = new LotteryDrawSnapshot(); snapshot.setPrize(prize);
        draw.setSnapshot(snapshot);
        LotteryUserLogVO result = service.result(draw);
        JsonNode response = json.readTree(json.writeValueAsString(result));
        assertFalse(response.has("drawStatus"));
        assertEquals(100L, result.getLotteryPrizeId());
        assertEquals("100积分", result.getPrizeName());
        assertEquals("PENDING", result.getGrantStatus());
        assertEquals("ACCEPTED", draw.getDrawStatus()); // 后台任务状态仍保留。
    }

    @Test
    void legacyCacheEnvelopeIsCompatible() throws Exception {
        LotteryResultCache.Entry entry = json.readValue("""
                {"storeId":30,"result":{"requestId":"request_123","drawStatus":"RETURNED",
                 "grantStatus":"RETURNED","prizeName":"实物奖品","prizeType":3}}
                """, LotteryResultCache.Entry.class);
        assertNull(entry.lifecycleStatus());
        assertEquals("RETURNED", entry.result().getGrantStatus());
        assertFalse(json.readTree(json.writeValueAsString(entry.result())).has("drawStatus"));
    }

    @Test
    void resultQueryOnlyReadsTheSameRequest() {
        LotteryLedger ledger = mock(LotteryLedger.class);
        LotteryResultCache cache = mock(LotteryResultCache.class);
        LotteryAdmission admission = mock(LotteryAdmission.class);
        LotteryV2Service service = new LotteryV2ServiceImpl();
        ReflectionTestUtils.setField(service, "ledger", ledger);
        ReflectionTestUtils.setField(service, "results", cache);
        ReflectionTestUtils.setField(service, "admission", admission);
        LotteryVO request = new LotteryVO(); request.setLotteryId(20L); request.setMemberId(40L);
        request.setStoreId(30L); request.setRequestId("request_123");
        LotteryUserLogVO original = response("SUCCESS");
        when(cache.get(10L, request)).thenReturn(new LotteryResultCache.Entry(30L, original, "COMPLETED"));
        when(admission.enter("result",10L,0L,40L))
                .thenReturn(mock(LotteryAdmission.Permit.class));
        Long previous = BusinessContextHolder.getBusinessId();
        try {
            BusinessContextHolder.setBusinessId(10L);
            assertSame(original, service.query(request));
            verifyNoInteractions(ledger); // 查询不会重新受理、扣次或发奖。
        } finally { BusinessContextHolder.setBusinessId(previous); }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"storeId\":30,\"lifecycleStatus\":\"RETURNED\",\"result\":{\"grantStatus\":\"RETURNED\"}}",
            "{\"storeId\":30,\"result\":{\"drawStatus\":\"RETURNED\",\"grantStatus\":\"RETURNED\"}}"
    })
    void delayedGrantCannotOverwriteNewOrLegacyReturnedCache(String returned) throws Exception {
        AtomicReference<String> stored = new AtomicReference<>(returned);
        LotteryResultCache cache = cacheWithLua(stored);
        cache.publish(draw("COMPLETED"), response("SUCCESS"));
        assertEquals(returned, stored.get());
    }

    @Test
    void newTerminalCacheStoresLifecycleOutsideFrontendResult() throws Exception {
        AtomicReference<String> stored = new AtomicReference<>();
        LotteryResultCache cache = cacheWithLua(stored);
        cache.publish(draw("COMPLETED"), response("SUCCESS"));
        JsonNode envelope = json.readTree(stored.get());
        assertEquals("COMPLETED", envelope.path("lifecycleStatus").asText());
        assertFalse(envelope.path("result").has("drawStatus"));
        assertEquals("SUCCESS", envelope.path("result").path("grantStatus").asText());
    }

    @SuppressWarnings("unchecked")
    private LotteryResultCache cacheWithLua(AtomicReference<String> stored) {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String,String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        AtomicReference<String> revision = new AtomicReference<>();
        Answer<Long> execute = invocation -> {
                    RedisScript<Long> script = invocation.getArgument(0);
                    Globals lua = JsePlatform.standardGlobals();
                    java.util.List<String> inputKeys=invocation.getArgument(1);
                    LuaTable keys = new LuaTable(); keys.set(1,inputKeys.get(0)); keys.set(2,inputKeys.get(1)); lua.set("KEYS", keys);
                    LuaTable args = new LuaTable();
                    args.set(1, LuaValue.valueOf((String) invocation.getArgument(2)));
                    args.set(2, LuaValue.valueOf((String) invocation.getArgument(3)));
                    if(invocation.getArguments().length>4)args.set(3,LuaValue.valueOf((String)invocation.getArgument(4)));
                    lua.set("ARGV", args);
                    LuaTable cjson = new LuaTable();
                    cjson.set("decode", new OneArgFunction() {
                        public LuaValue call(LuaValue value) {
                            try { return toLua(json.readTree(value.checkjstring())); }
                            catch(Exception invalid) { return error(invalid.getMessage()); }
                        }
                    });
                    lua.set("cjson", cjson);
                    LuaTable commands = new LuaTable();
                    commands.set("call", new VarArgFunction() {
                        public Varargs invoke(Varargs args) {
                            boolean isRevision=inputKeys.get(1).equals(args.arg(2).checkjstring());
                            if("GET".equals(args.arg(1).checkjstring())) {
                                String value=isRevision?revision.get():stored.get();
                                return value==null?LuaValue.NIL:LuaValue.valueOf(value);
                            }
                            if("SET".equals(args.arg(1).checkjstring())) {
                                (isRevision?revision:stored).set(args.arg(3).checkjstring()); return LuaValue.valueOf("OK");
                            }
                            if("DEL".equals(args.arg(1).checkjstring())) {stored.set(null);return LuaValue.valueOf(1);}
                            throw new AssertionError("Unexpected Redis command");
                        }
                    });
                    lua.set("redis", commands);
                    return lua.load(script.getScriptAsString()).call().tolong();
                };
        when(redis.execute(org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(), any())).thenAnswer(execute);
        when(redis.execute(org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(), any(),any())).thenAnswer(execute);
        LotteryResultCache cache = new LotteryResultCacheImpl();
        ReflectionTestUtils.setField(cache,"redis",redis);
        ReflectionTestUtils.setField(cache,"json",json);
        ReflectionTestUtils.setField(cache,"properties",new LotteryProperties());
        return cache;
    }

    @Test
    void oldFailureCannotBeCachedAfterManualReissue() throws Exception {
        AtomicReference<String> stored=new AtomicReference<>();
        LotteryResultCache cache=cacheWithLua(stored);
        LotteryLedger.Draw old=draw("FAILED");
        cache.publish(old,response("FAILED"));
        assertNotNull(stored.get());
        LotteryLedger.Draw manual=draw("ACCEPTED");
        manual.getSnapshot().setReissueSequence(1);
        cache.invalidateForReissue(manual);
        assertNull(stored.get());
        cache.publish(old,response("FAILED"));
        assertNull(stored.get());
        manual.setDrawStatus("COMPLETED");
        cache.publish(manual,response("SUCCESS"));
        assertEquals(1,json.readTree(stored.get()).path("reissueSequence").asLong());
    }

    private LuaValue toLua(JsonNode node) {
        if(node.isNull()) return LuaValue.NIL;
        if(node.isObject()) {
            LuaTable table = new LuaTable();
            node.fields().forEachRemaining(field -> table.set(field.getKey(),toLua(field.getValue())));
            return table;
        }
        if(node.isNumber()) return LuaValue.valueOf(node.asDouble());
        if(node.isBoolean()) return LuaValue.valueOf(node.asBoolean());
        return LuaValue.valueOf(node.asText());
    }

    private LotteryLedger.Draw draw(String lifecycle) {
        LotteryLedger.Draw draw = new LotteryLedger.Draw();
        draw.setBusinessId(10L); draw.setActivityId(20L); draw.setStoreId(30L); draw.setMemberId(40L);
        draw.setRequestId("request_123"); draw.setDrawStatus(lifecycle); draw.setGrantStatus("PENDING");
        LotteryDrawSnapshot snapshot = new LotteryDrawSnapshot();
        var settings = new com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO();
        settings.setId(21L); snapshot.setSettings(settings); draw.setSnapshot(snapshot);
        return draw;
    }

    private LotteryUserLogVO response(String grant) {
        LotteryUserLogVO result = new LotteryUserLogVO();
        result.setRequestId("request_123"); result.setGrantStatus(grant);
        result.setLotteryPrizeId(100L); result.setPrizeName("100积分"); result.setPrizeType(2);
        return result;
    }
}
