package com.htyoudao.youdao.module.member.service.wxmember;

import com.htyoudao.youdao.module.member.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventService {
    List<Long> queryMemberIDs(
        EventType eventType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long businessId);
}
