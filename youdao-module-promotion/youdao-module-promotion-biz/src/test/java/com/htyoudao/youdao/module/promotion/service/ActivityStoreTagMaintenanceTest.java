package com.htyoudao.youdao.module.promotion.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityStoreTagMaintenanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void createSavesDistinctNonNullTags(String type) throws Exception {
        Fixture fixture = new Fixture(type, 1, Arrays.asList(10L, null, 10L, 20L));
        fixture.invoke("create");
        fixture.assertTags(10L, 20L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void createStoreScopeIgnoresTags(String type) throws Exception {
        Fixture fixture = new Fixture(type, 0, List.of(10L));
        fixture.invoke("create");
        fixture.assertTags();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void updateReplacesTags(String type) throws Exception {
        Fixture fixture = new Fixture(type, 1, Arrays.asList(20L, null, 20L));
        fixture.oldScope(1);
        fixture.invoke("update");
        fixture.assertTags(20L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void updateRetainsTagsWhenScopeUnchangedAndTagsOmittedOrEmpty(String type) throws Exception {
        for (List<Long> tags : Arrays.<List<Long>>asList(null, Collections.emptyList())) {
            Fixture fixture = new Fixture(type, 1, tags);
            fixture.oldScope(1);
            fixture.invoke("update");
            fixture.assertTags(10L);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void switchToStoreScopeClearsTags(String type) throws Exception {
        Fixture fixture = new Fixture(type, 0, List.of(20L));
        fixture.oldScope(1);
        fixture.invoke("update");
        fixture.assertTags();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mj", "Njnz"})
    void switchToTagScopeDoesNotReuseHistoricalTags(String type) throws Exception {
        Fixture fixture = new Fixture(type, 1, null);
        fixture.oldScope(0);
        fixture.invoke("update");
        fixture.assertTags();
    }

    private static class Fixture {
        private static final String BASE = "com.htyoudao.youdao.module.promotion.";
        private final String type;
        private final Object service;
        private final Object request;
        private final ActivityStoreTagMapper tagMapper = mock(ActivityStoreTagMapper.class);
        private final ActivityService activityService = mock(ActivityService.class);

        Fixture(String type, Integer scope, List<Long> tags) throws Exception {
            this.type = type;
            service = Class.forName(BASE + "service.activity" + type + ".Activity" + type + "ServiceImpl")
                    .getDeclaredConstructor().newInstance();
            request = Class.forName(BASE + "controller.admin.activity" + type + ".vo.Activity" + type + "SaveReqVO")
                    .getDeclaredConstructor().newInstance();
            BeanWrapperImpl bean = new BeanWrapperImpl(request);
            bean.setPropertyValue("id", 2L);
            bean.setPropertyValue("activityId", 100L);
            bean.setPropertyValue("discountType", 1);
            bean.setPropertyValue("appScope", scope);
            bean.setPropertyValue("tagIds", tags);
            ReflectionTestUtils.setField(service, "activityService", activityService);
            ReflectionTestUtils.setField(service, "activityStoreTagMapper", tagMapper);
            ReflectionTestUtils.setField(service, "activityStoreService", mock(ActivityStoreService.class));
            ReflectionTestUtils.setField(service, "activity" + type + "Mapper",
                    mock(Class.forName(BASE + "dal.mysql.activity" + type + ".Activity" + type + "Mapper")));
            ReflectionTestUtils.setField(service, "activity" + type + "CommodityService",
                    mock(Class.forName(BASE + "service.activity" + type + "Commodity.Activity" + type + "CommodityService")));
            when(activityService.createActivity(any(ActivityDO.class))).thenReturn(100L);
            ActivityStoreTagDO oldTag = new ActivityStoreTagDO();
            oldTag.setActivityId(100L);
            oldTag.setTagId(10L);
            when(tagMapper.selectList(any(Wrapper.class))).thenReturn(List.of(oldTag));
        }

        void oldScope(int scope) {
            ActivityDO oldActivity = new ActivityDO();
            oldActivity.setAppScope(scope);
            when(activityService.selectById(100L)).thenReturn(oldActivity);
        }

        void invoke(String operation) throws Exception {
            service.getClass().getMethod(operation + "Activity" + type, request.getClass()).invoke(service, request);
        }

        void assertTags(Long... expected) {
            verify(tagMapper).delete(any(Wrapper.class));
            ArgumentCaptor<ActivityStoreTagDO> captor = ArgumentCaptor.forClass(ActivityStoreTagDO.class);
            verify(tagMapper, times(expected.length)).insert(captor.capture());
            assertEquals(Arrays.asList(expected), captor.getAllValues().stream()
                    .map(ActivityStoreTagDO::getTagId).collect(Collectors.toList()));
            captor.getAllValues().forEach(tag -> assertEquals(100L, tag.getActivityId()));
        }
    }
}
