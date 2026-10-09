package com.htyoudao.youdao.module.bpm.controller.admin.learningtask.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningTaskDateTimeDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesStandardDateTimeString() throws Exception {
        LearningTaskSaveReqVO value = objectMapper.readValue("{\"startTime\":\"2026-09-14 00:00:00\","
                + "\"endTime\":\"2026-09-15 23:59:59\"}", LearningTaskSaveReqVO.class);

        assertEquals(LocalDateTime.of(2026, 9, 14, 0, 0), value.getStartTime());
        assertEquals(LocalDateTime.of(2026, 9, 15, 23, 59, 59), value.getEndTime());
    }

    @Test
    void parsesEpochMillisNumberAndNumericString() throws Exception {
        long startMillis = 1789315200000L;
        long endMillis = 1789487999000L;
        LearningTaskSaveReqVO value = objectMapper.readValue("{\"startTime\":" + startMillis
                + ",\"endTime\":\"" + endMillis + "\"}", LearningTaskSaveReqVO.class);

        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault()),
                value.getStartTime());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), ZoneId.systemDefault()),
                value.getEndTime());
    }

    @Test
    void rejectsInvalidDateTimeString() {
        assertThrows(Exception.class, () -> objectMapper.readValue(
                "{\"startTime\":\"2026/09/14\"}", LearningTaskSaveReqVO.class));
    }
}
