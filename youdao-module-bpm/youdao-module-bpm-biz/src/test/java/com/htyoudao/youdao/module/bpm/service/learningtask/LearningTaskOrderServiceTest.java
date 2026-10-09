package com.htyoudao.youdao.module.bpm.service.learningtask;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.bpm.controller.admin.learningtask.vo.ExternalLearningMaterialVO;
import com.htyoudao.youdao.module.bpm.controller.admin.learningtask.vo.ExternalLearningMaterialVO.ExternalLearningMaterialFileVO;
import com.htyoudao.youdao.module.bpm.controller.app.learningtask.vo.AppLearningTaskOrderCheckRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.learningtask.LearningTaskDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.learningtask.LearningTaskFileProgressDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.learningtask.LearningTaskMaterialDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.definition.BpmAllStoreInfoMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.learningtask.LearningTaskMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.learningtask.LearningTaskMaterialMapper;
import com.htyoudao.youdao.module.bpm.framework.config.LearningTaskOrderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** 订货限制的时间边界、实时进度与店长ID放行回归。 */
class LearningTaskOrderServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 16, 12, 0);

    @Test
    void remainingDaysUsesCeilingAndExactDeadline() {
        assertEquals(0, LearningTaskOrderService.remainingDays(NOW, NOW.minusSeconds(1)));
        assertEquals(0, LearningTaskOrderService.remainingDays(NOW, NOW));
        assertEquals(1, LearningTaskOrderService.remainingDays(NOW, NOW.plusNanos(1)));
        assertEquals(1, LearningTaskOrderService.remainingDays(NOW, NOW.plusDays(1)));
        assertEquals(2, LearningTaskOrderService.remainingDays(NOW, NOW.plusDays(1).plusSeconds(1)));
    }

    @Test
    void earliestIncompleteTaskDeterminesPermission() {
        LearningTaskOrderService.UnfinishedTask nearest = null;
        nearest = LearningTaskOrderService.includeUnfinishedTask(nearest, task(2L, NOW.minusDays(1), 7));
        nearest = LearningTaskOrderService.includeUnfinishedTask(nearest, task(1L, NOW.minusDays(2), 3));
        AppLearningTaskOrderCheckRespVO result = LearningTaskOrderService.finishEvaluation(nearest, NOW);
        assertEquals(1L, result.getTaskId());
        assertEquals(1, result.getRemainingDays());
        assertTrue(result.getCanOrder());

        result = LearningTaskOrderService.finishEvaluation(nearest, NOW.plusDays(1));
        assertFalse(result.getCanOrder());
        assertEquals(0, result.getRemainingDays());
        nearest = LearningTaskOrderService.includeUnfinishedTask(nearest, task(3L, NOW.minusDays(10), 3));
        result = LearningTaskOrderService.finishEvaluation(nearest, NOW);
        assertEquals(3L, result.getTaskId());
        assertFalse(result.getCanOrder());
    }

    @Test
    void noIncompleteRequiredTaskAllowsOrderingWithoutCountdown() {
        AppLearningTaskOrderCheckRespVO result = LearningTaskOrderService.finishEvaluation(null, NOW);
        assertTrue(result.getCanOrder());
        assertNull(result.getRemainingDays());
        assertNull(result.getTaskId());
    }

    @Test
    void attachmentRevisionAndFileVersionMustMatch() {
        LearningTaskMaterialDO relation = relation();
        ExternalLearningMaterialVO material = material(List.of(file("video", 1)));
        LearningTaskFileProgressDO progress = progress("video", 1);
        progress.setCompletedFlag(true);
        assertTrue(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(progress)));
        material.setAttachmentFingerprint("new-content");
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(progress)));
        material.setAttachmentFingerprint("content");
        progress.setProgressRevision(0);
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(progress)));
        progress.setProgressRevision(1);
        progress.setFileVersion("old-version");
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(progress)));
    }

    @Test
    void currentReadingRuleAndEveryFileAreRequired() {
        LearningTaskMaterialDO relation = relation();
        relation.setDocumentBrowseLimitType(1);
        relation.setMinDocumentBrowseSeconds(10);
        ExternalLearningMaterialVO material = material(List.of(file("pdf", 3), file("video", 1)));
        material.getFiles().get(0).setFileSuffix("pdf");
        LearningTaskFileProgressDO pdf = progress("pdf", 3);
        pdf.setReachedBottom(true);
        pdf.setValidStudySeconds(9L);
        pdf.setCompletedFlag(true); // 旧规则的完成标记不能绕过新的阅读时长。
        LearningTaskFileProgressDO video = progress("video", 1);
        video.setCompletedFlag(true);
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(pdf, video)));
        pdf.setValidStudySeconds(10L);
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(pdf)));
        assertTrue(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(pdf, video)));
        pdf.setReachedBottom(false);
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(pdf, video)));
    }

    @Test
    void richTextNeedsItsOwnReadEvidence() {
        LearningTaskMaterialDO relation = relation();
        ExternalLearningMaterialVO material = material(List.of());
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of()));
        LearningTaskFileProgressDO progress = progress("rich-text", 3);
        progress.setFileVersion("1");
        progress.setReachedBottom(true);
        assertTrue(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(progress)));
    }

    @Test
    void multipleRichTextNodesCountOnceInMixedMaterial() {
        String text = """
                [{"type":"richText","content":"<p>第一段</p>"},
                 {"type":"image","url":"https://example.com/step.jpg"},
                 {"type":"richText","content":"<p>第二段</p>"}]
                """;
        LearningMaterialContentParser.ParsedContent parsed = new LearningMaterialContentParser().parse(text);
        ExternalLearningMaterialVO material = material(parsed.getFiles());
        material.setStudyItems(parsed.getStudyItems());
        LearningTaskMaterialDO relation = relation();
        assertEquals(2, LearningStudyProgress.items(material).size());
        relation.setDocumentBrowseLimitType(1);
        relation.setMinDocumentBrowseSeconds(60);

        ExternalLearningMaterialFileVO image = parsed.getFiles().get(0);
        LearningTaskFileProgressDO imageProgress = progress(image.getFileKey(), image.getFileType());
        imageProgress.setFileVersion(image.getFileVersion());
        imageProgress.setReachedBottom(true);
        assertFalse(LearningTaskOrderService.isMaterialCompleted(relation, material, List.of(imageProgress)));

        LearningTaskFileProgressDO richTextProgress = progress("rich-text", 3);
        richTextProgress.setFileVersion("1");
        richTextProgress.setReachedBottom(true);
        assertTrue(LearningTaskOrderService.isMaterialCompleted(relation, material,
                List.of(imageProgress, richTextProgress)));
    }

    @Test
    void bypassUsesLoginUserIdAndStillRequiresManager() {
        BusinessContextHolder.setBusinessId(10L);
        try {
            LearningTaskOrderService service = new LearningTaskOrderService();
            LearningTaskOrderProperties properties = new LearningTaskOrderProperties();
            properties.setBypassUserIds(Set.of(7L));
            assertFalse(properties.isBypassed(8L));
            assertFalse(properties.isBypassed(null));
            ReflectionTestUtils.setField(service, "orderProperties", properties);
            ReflectionTestUtils.setField(service, "storeInfoMapper", proxy(BpmAllStoreInfoMapper.class, "selectCount", 1L));
            // 白名单仍返回真实剩余天数和未完成任务，每次请求只计算一遍。
            LearningTaskDO overdueTask = task(9L, LocalDateTime.now().minusDays(5), 3);
            LearningTaskMaterialDO relation = relation();
            relation.setTaskId(9L);
            relation.setMaterialId(100L);
            ExternalLearningMaterialVO changedMaterial = material(List.of(file("video", 1)));
            changedMaterial.setAttachmentFingerprint("changed");
            AtomicInteger taskQueries = new AtomicInteger();
            ReflectionTestUtils.setField(service, "taskMapper", Proxy.newProxyInstance(
                    LearningTaskMapper.class.getClassLoader(), new Class<?>[]{LearningTaskMapper.class},
                    (p, method, args) -> {
                        assertEquals("selectOrderLimitedTasks", method.getName());
                        assertEquals(7L, args[1]);
                        taskQueries.incrementAndGet();
                        return List.of(overdueTask);
                    }));
            ReflectionTestUtils.setField(service, "materialMapper", proxy(
                    LearningTaskMaterialMapper.class, "selectList", List.of(relation)));
            ReflectionTestUtils.setField(service, "materialExternalService", proxy(
                    LearningMaterialExternalService.class, "getMaterialMapByIds", Map.of(100L, changedMaterial)));
            AppLearningTaskOrderCheckRespVO result = service.checkOrder(7L);
            assertTrue(result.getCanOrder());
            assertEquals(0, result.getRemainingDays());
            assertEquals(9L, result.getTaskId());
            assertEquals(1, taskQueries.get());

            ReflectionTestUtils.setField(service, "storeInfoMapper", proxy(BpmAllStoreInfoMapper.class, "selectCount", 0L));
            assertThrows(ServiceException.class, () -> service.checkOrder(7L));
            assertEquals(1, taskQueries.get());

            // 配置刷新后，移除用户ID立即恢复正常查询。
            properties.setBypassUserIds(Set.of());
            ReflectionTestUtils.setField(service, "storeInfoMapper", proxy(BpmAllStoreInfoMapper.class, "selectCount", 1L));
            result = service.checkOrder(7L);
            assertFalse(result.getCanOrder());
            assertEquals(0, result.getRemainingDays());
            assertEquals(9L, result.getTaskId());
            assertEquals(2, taskQueries.get());

            // 同一个接口在宽限期内同时返回剩余天数和放行结果。
            overdueTask.setStartTime(LocalDateTime.now().minusDays(1));
            result = service.checkOrder(7L);
            assertEquals(2, result.getRemainingDays());
            assertTrue(result.getCanOrder());
            assertEquals(9L, result.getTaskId());
            assertEquals(3, taskQueries.get());

            ReflectionTestUtils.setField(service, "taskMapper", proxy(LearningTaskMapper.class, "selectOrderLimitedTasks", List.of()));
            result = service.checkOrder(7L);
            assertTrue(result.getCanOrder());
            assertNull(result.getRemainingDays());
            assertNull(result.getTaskId());
        } finally {
            BusinessContextHolder.clear();
        }
    }

    private static LearningTaskDO task(Long id, LocalDateTime start, int days) {
        LearningTaskDO task = new LearningTaskDO();
        task.setTaskId(id); task.setStartTime(start); task.setOrderLimitDays(days);
        return task;
    }

    private static LearningTaskMaterialDO relation() {
        LearningTaskMaterialDO relation = new LearningTaskMaterialDO();
        relation.setTaskMaterialId(1L); relation.setProgressRevision(1);
        relation.setAttachmentFingerprint("content"); relation.setDocumentBrowseLimitType(0);
        return relation;
    }

    private static ExternalLearningMaterialVO material(List<ExternalLearningMaterialFileVO> files) {
        ExternalLearningMaterialVO material = new ExternalLearningMaterialVO();
        material.setEnabledStatus(1); material.setAttachmentFingerprint("content"); material.setFiles(files);
        return material;
    }

    private static ExternalLearningMaterialFileVO file(String key, int type) {
        ExternalLearningMaterialFileVO file = new ExternalLearningMaterialFileVO();
        file.setFileKey(key); file.setFileType(type); file.setFileVersion("version");
        return file;
    }

    private static LearningTaskFileProgressDO progress(String key, int type) {
        LearningTaskFileProgressDO progress = new LearningTaskFileProgressDO();
        progress.setTaskMaterialId(1L); progress.setProgressRevision(1); progress.setFileKey(key);
        progress.setFileType(type); progress.setFileVersion("version");
        return progress;
    }

    private static <T> T proxy(Class<T> type, String allowedMethod, Object result) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, method, args) -> {
            if (method.getName().equals(allowedMethod)) return result;
            throw new AssertionError("不应调用数据库写入或额外查询: " + method.getName());
        }));
    }
}
