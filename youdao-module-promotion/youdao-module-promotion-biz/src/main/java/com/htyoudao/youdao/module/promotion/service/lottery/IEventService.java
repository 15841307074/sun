package com.htyoudao.youdao.module.promotion.service.lottery;

import com.htyoudao.youdao.module.promotion.enums.EventType;

import java.time.LocalDateTime;
import java.util.Map;

public interface IEventService {
    Map<String, Long> statPvUv(EventType eventType, String eventId,
                               LocalDateTime startTime, LocalDateTime endTime,
                               Long businessId);

    Long statUv(EventType eventType, String eventId,
                LocalDateTime startTime, LocalDateTime endTime,
                Long businessId);

    Long statPv(EventType eventType, String eventId,
                LocalDateTime startTime, LocalDateTime endTime,
                Long businessId);

    Map<String, Map<String, Long>> statDailyPvUv(EventType eventType, String eventId,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Long businessId);

}
