package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.AnalysisServerApplication;
import com.htyoudao.youdao.module.analysis.dal.es.BaseEvent;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AnalysisServerApplication.class)
public class EventTest {

    @Resource
    private IEventService eventService;


    @Test // 根据 ID 编号，查询一条记录
    public void testQueryUV() {

        List<Long> storeIds = List.of(1279920041211486208L);
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStartTime(LocalDateTime.of(2025,8,20,0,0,0));
        queryDTO.setEndTime(LocalDateTime.of(2025,8,20,23,59,59));
        queryDTO.setEventType(EventType.IN_STORE);
        Long l = eventService.queryPV(queryDTO);
        System.out.println(l);
    }

    @Test // 根据 ID 编号数组，查询多

    // 条记录
    public void testAdd() {

        eventService.add(BaseEvent.builder()
            .storeId(1279920041211486208L)
            .eventType(EventType.IN_STORE.getCode())
            .timestamp(new Date())
            .memberId(111L)
            .businessId(1L)
            .build());
    }


    @Test // 根据 ID 编号，查询一条记录
    public void testQueryMemberIds() {


    }


}
