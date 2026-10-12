package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LotteryConfigurationTest {
    private LotteryProperties bind(Map<String, Object> values) {
        LotteryProperties properties = new LotteryProperties();
        new Binder(new MapConfigurationPropertySource(values))
                .bind("promotion.lottery", Bindable.ofInstance(properties));
        return properties;
    }

    @Test
    void shortNacosConfigurationKeepsAdvancedDefaults() {
        LotteryProperties p = bind(Map.of("promotion.lottery.draw-rate", 80,
                "promotion.lottery.activity-draw-rate", 50,
                "promotion.lottery.member-draw-rate", 2,
                "promotion.lottery.result-rate", 300));
        assertEquals(80, p.getDrawRate()); assertEquals(50, p.getActivityDrawRate());
        assertEquals(2, p.getMemberDrawRate()); assertEquals(300, p.getResultRate());
        assertTrue(p.isAccepting()); assertEquals(1000, p.getQueryRate());
        assertEquals(200, p.getTaskRate()); assertEquals(8, p.getDrawConcurrency());
        assertEquals(4, p.getGrantConcurrency()); assertEquals(16, p.getConnectionBudget());
    }

    @Test
    void operationalOverridesStillBind() {
        LotteryProperties p = bind(Map.of("promotion.lottery.accepting", false,
                "promotion.lottery.query-rate", 800, "promotion.lottery.task-rate", 120,
                "promotion.lottery.draw-concurrency", 6,
                "promotion.lottery.grant-concurrency", 3,
                "promotion.lottery.connection-budget", 14));
        assertFalse(p.isAccepting()); assertEquals(800, p.getQueryRate());
        assertEquals(120, p.getTaskRate()); assertEquals(6, p.getDrawConcurrency());
        assertEquals(3, p.getGrantConcurrency()); assertEquals(14, p.getConnectionBudget());
    }

    @Test
    void obsoleteCacheKeysDoNotBreakRemainingBindings() {
        LotteryProperties p = bind(Map.of("promotion.lottery.config-ttl-seconds", 99,
                "promotion.lottery.list-ttl-seconds", 9,
                "promotion.lottery.result-ttl-seconds", 999,
                "promotion.lottery.refill-concurrency", 1,
                "promotion.lottery.member-draw-rate", 2));
        assertEquals(2, p.getMemberDrawRate()); assertEquals(100, p.getDrawRate());
        assertEquals(16, p.getConnectionBudget());
    }
}
