package com.htyoudao.youdao.module.system.service.storebackground;

import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTemplateDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreBackgroundCacheServiceTest {

    private final StoreBackgroundCacheService service = new StoreBackgroundCacheService(Runnable::run);

    @Test
    void shouldUseAllStoreTemplateWithoutStoreRelation() {
        StoreBackgroundTemplateDO defaultTemplate = template(1L, null, 1, 1, "2026-08-01T10:00:00");
        StoreBackgroundTemplateDO allStoreTemplate = template(2L, 10L, 1, 1,
                "2026-08-19T12:00:00");

        StoreBackgroundTemplateDO selected = service.selectTemplateForStore(
                List.of(allStoreTemplate), Collections.emptySet(), 10L, defaultTemplate);

        assertEquals(2L, selected.getBackgroundId());
    }

    @Test
    void shouldPreferSpecificTemplateOverNewerAllStoreTemplate() {
        StoreBackgroundTemplateDO defaultTemplate = template(1L, null, 1, 1, "2026-08-01T10:00:00");
        StoreBackgroundTemplateDO allStoreTemplate = template(2L, 10L, 1, 1, "2026-08-19T12:00:00");
        StoreBackgroundTemplateDO specificTemplate = template(3L, 10L, 1, 2, "2026-08-18T12:00:00");

        StoreBackgroundTemplateDO selected = service.selectTemplateForStore(
                List.of(allStoreTemplate, specificTemplate), Set.of(3L), 10L, defaultTemplate);

        assertEquals(3L, selected.getBackgroundId());
    }

    @Test
    void shouldUseLatestSpecificTemplateWhenStoreMatchesMultipleTemplates() {
        StoreBackgroundTemplateDO defaultTemplate = template(1L, null, 1, 1, "2026-08-01T10:00:00");
        StoreBackgroundTemplateDO directTemplate = template(3L, 10L, 1, 2, "2026-08-18T12:00:00");
        StoreBackgroundTemplateDO tagTemplate = template(4L, 10L, 2, null, "2026-08-19T12:00:00");

        StoreBackgroundTemplateDO selected = service.selectTemplateForStore(
                List.of(directTemplate, tagTemplate), Set.of(3L, 4L), 10L, defaultTemplate);

        assertEquals(4L, selected.getBackgroundId());
    }

    @Test
    void shouldIgnoreAllStoreTemplateFromAnotherBusiness() {
        StoreBackgroundTemplateDO defaultTemplate = template(1L, null, 1, 1, "2026-08-01T10:00:00");
        StoreBackgroundTemplateDO otherBusinessTemplate = template(2L, 11L, 1, 1,
                "2026-08-19T12:00:00");

        StoreBackgroundTemplateDO selected = service.selectTemplateForStore(
                List.of(otherBusinessTemplate), Collections.emptySet(), 10L, defaultTemplate);

        assertEquals(1L, selected.getBackgroundId());
    }

    private StoreBackgroundTemplateDO template(Long id, Long businessId, Integer appScope,
                                                 Integer storeScope, String releaseTime) {
        StoreBackgroundTemplateDO template = new StoreBackgroundTemplateDO();
        template.setBackgroundId(id);
        template.setBusinessId(businessId);
        template.setAppScope(appScope);
        template.setStoreScope(storeScope);
        template.setReleaseTime(LocalDateTime.parse(releaseTime));
        return template;
    }
}
