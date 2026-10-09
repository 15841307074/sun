package com.htyoudao.youdao.module.promotion.service.goodcoupon;

import com.htyoudao.youdao.module.promotion.enums.CouponEventType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author dht
 */
public interface ICouponEventService {

    Map<String, Long> statPvUv(CouponEventType eventType, String eventId,
                               LocalDateTime startTime, LocalDateTime endTime,
                               Long businessId);

    Long statUv(CouponEventType eventType, String eventId,
                LocalDateTime startTime, LocalDateTime endTime,
                Long businessId);

    Long statPv(CouponEventType eventType, String eventId,
                LocalDateTime startTime, LocalDateTime endTime,
                Long businessId);

    Map<String, Map<String, Long>> statDailyPvUv(CouponEventType eventType, String eventId,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Long businessId);
}
