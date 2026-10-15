package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.StoreOrgSnapshotMapper;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreOrgStatisticsServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private StoreOrgStatisticsService service;
    @Mock
    private StoreOrgSnapshotMapper snapshotMapper;
    @Mock
    private StoreOrgSnapshotApi storeOrgSnapshotApi;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new StoreOrgStatisticsService();
        ReflectionTestUtils.setField(service, "snapshotMapper", snapshotMapper);
        ReflectionTestUtils.setField(service, "storeOrgSnapshotApi", storeOrgSnapshotApi);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        BusinessContextHolder.setBusinessId(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        BusinessContextHolder.clear();
        mocks.close();
    }

    @Test
    void shouldGroupByImmediateChildAndProvinceIncludingDirectStores() throws Exception {
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(100L, 200L, 300L, 400L)));
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of(
                snapshot(1L, "", List.of(node(100L, "东北区域", 0L, 1, 1))),
                snapshot(2L, "辽宁省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(200L, "王子彬组", 100L, 2, 2))),
                snapshot(3L, "吉林省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(200L, "王子彬组", 100L, 2, 2), node(300L, "下级", 200L, 3, 1))),
                snapshot(4L, "辽宁省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(400L, "孙鑫洋组", 100L, 2, 3)))));

        StoreOrgStatisticsRespVO result = service.getStatistics(7L, 100L, "2026-08");

        assertEquals("东北区域", result.getOrgName());
        assertEquals(4L, result.getTotalStoreCount());
        assertEquals(4, result.getItems().size());
        assertEquals("100", result.getItems().get(0).getGroupOrgId());
        assertEquals("未设置", result.getItems().get(0).getProvinceName());
        assertEquals("200", result.getItems().get(1).getGroupOrgId());
        assertEquals("吉林省", result.getItems().get(1).getProvinceName());
        assertEquals("200", result.getItems().get(2).getGroupOrgId());
        assertEquals("辽宁省", result.getItems().get(2).getProvinceName());
        assertEquals("400", result.getItems().get(3).getGroupOrgId());
        assertEquals(result.getTotalStoreCount(),
                result.getItems().stream().mapToLong(item -> item.getStoreCount()).sum());
    }

    @Test
    void shouldRejectOrganizationOutsideUserScope() {
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(200L)));

        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, 100L, "2026-08"));
        verifyNoInteractions(snapshotMapper);
    }

    @Test
    void shouldRejectMissingMonthSnapshot() {
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(100L)));
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, 100L, "2026-08"));
    }

    @Test
    void shouldRejectInvalidMonthBeforeRemoteCall() {
        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, 100L, "2026-8"));
        verifyNoInteractions(storeOrgSnapshotApi, snapshotMapper);
    }

    private StoreOrgSnapshotDO snapshot(Long storeId, String province,
                                        List<StoreOrgPathNodeDTO> path) throws Exception {
        StoreOrgSnapshotDO snapshot = new StoreOrgSnapshotDO();
        snapshot.setStoreId(storeId);
        snapshot.setStoreProvince(province);
        snapshot.setOrgPathJson(objectMapper.writeValueAsString(path));
        return snapshot;
    }

    private StoreOrgPathNodeDTO node(Long id, String name, Long parentId, Integer level, Integer sort) {
        StoreOrgPathNodeDTO node = new StoreOrgPathNodeDTO();
        node.setId(id);
        node.setName(name);
        node.setParentId(parentId);
        node.setLevel(level);
        node.setSort(sort);
        return node;
    }
}
