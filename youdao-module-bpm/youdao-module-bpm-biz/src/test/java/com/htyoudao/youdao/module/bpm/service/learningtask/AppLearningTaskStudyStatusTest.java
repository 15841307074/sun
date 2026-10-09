package com.htyoudao.youdao.module.bpm.service.learningtask;

import com.htyoudao.youdao.module.bpm.dal.dataobject.learningtask.LearningTaskMaterialDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.learningtask.LearningTaskMaterialProgressDO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 列表和详情的个人学习状态应只受当前任务的进度影响。 */
class AppLearningTaskStudyStatusTest {

    @Test
    void anotherTaskProgressDoesNotMarkCurrentTaskStudying() {
        LearningTaskMaterialDO current = relation(11L);
        assertEquals(0, AppLearningTaskServiceImpl.taskStudyStatus(
                List.of(current), Map.of(22L, new LearningTaskMaterialProgressDO()), 0));
        assertEquals(0, AppLearningTaskServiceImpl.taskStudyStatus(
                List.of(current), Map.of(), 0));
    }

    @Test
    void currentTaskProgressDeterminesStudyingAndCompleted() {
        LearningTaskMaterialDO current = relation(11L);
        assertEquals(1, AppLearningTaskServiceImpl.taskStudyStatus(
                List.of(current), Map.of(11L, new LearningTaskMaterialProgressDO()), 0));
        assertEquals(2, AppLearningTaskServiceImpl.taskStudyStatus(
                List.of(current), Map.of(11L, new LearningTaskMaterialProgressDO()), 1));
    }

    @Test
    void noRequiredMaterialStaysUnstarted() {
        assertEquals(0, AppLearningTaskServiceImpl.taskStudyStatus(
                List.of(), Map.of(22L, new LearningTaskMaterialProgressDO()), 0));
    }

    private static LearningTaskMaterialDO relation(Long id) {
        LearningTaskMaterialDO relation = new LearningTaskMaterialDO();
        relation.setTaskMaterialId(id);
        return relation;
    }
}
