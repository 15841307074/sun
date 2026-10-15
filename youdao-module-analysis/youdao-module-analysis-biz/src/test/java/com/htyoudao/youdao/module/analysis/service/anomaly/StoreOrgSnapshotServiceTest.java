package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreOrgSnapshotServiceTest {

    private StoreOrgSnapshotService service;
    @Mock
    private StoreOrgSnapshotApi storeOrgSnapshotApi;
    @Mock
    private StoreOrgSnapshotPersistenceService persistenceService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new StoreOrgSnapshotService();
        ReflectionTestUtils.setField(service, "storeOrgSnapshotApi", storeOrgSnapshotApi);
        ReflectionTestUtils.setField(service, "persistenceService", persistenceService);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        BusinessContextHolder.setBusinessId(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        BusinessContextHolder.clear();
        mocks.close();
    }

    @Test
    void shouldValidateAllRemoteDataBeforeReplacingSnapshot() {
        when(storeOrgSnapshotApi.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of()));

        assertThrows(IllegalStateException.class,
                () -> service.saveMonthlySnapshot(LocalDate.of(2026, 8, 1)));
        verifyNoInteractions(persistenceService);
    }

    @Test
    void shouldPersistCurrentBusinessAndSerializedPath() {
        StoreOrgSnapshotDTO source = new StoreOrgSnapshotDTO();
        source.setStoreId(900L);
        source.setStoreName("测试门店");
        source.setStoreProvince("辽宁省");
        source.setOrgId(200L);
        source.setOrgName("测试组织");
        source.setParentOrgId(100L);
        source.setParentOrgName("上级组织");
        source.setOrgPath(List.of(node(100L, "上级组织", 0L), node(200L, "测试组织", 100L)));
        when(storeOrgSnapshotApi.listNormalStoreOrgData())
                .thenReturn(CommonResult.success(List.of(source)));

        int count = service.saveMonthlySnapshot(LocalDate.of(2026, 8, 1));

        assertEquals(1, count);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StoreOrgSnapshotDO>> snapshots = ArgumentCaptor.forClass(List.class);
        verify(persistenceService).replace(eq(10L), eq(LocalDate.of(2026, 8, 1)), snapshots.capture());
        StoreOrgSnapshotDO saved = snapshots.getValue().get(0);
        assertEquals(10L, saved.getBusinessId());
        assertEquals(900L, saved.getStoreId());
        assertTrue(saved.getOrgPathJson().contains("测试组织"));
    }

    private StoreOrgPathNodeDTO node(Long id, String name, Long parentId) {
        StoreOrgPathNodeDTO node = new StoreOrgPathNodeDTO();
        node.setId(id);
        node.setName(name);
        node.setParentId(parentId);
        node.setLevel(id.equals(100L) ? 1 : 2);
        node.setSort(1);
        return node;
    }
}
