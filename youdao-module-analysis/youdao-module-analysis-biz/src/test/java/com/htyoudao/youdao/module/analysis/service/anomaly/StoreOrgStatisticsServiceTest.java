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
    void shouldReturnDynamicOrganizationTreeFromSelectedOrganization() throws Exception {
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(100L, 200L, 300L, 400L)));
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of(
                snapshot(1L, "", List.of(node(100L, "东北区域", 0L, 1, 1))),
                snapshot(2L, "辽宁省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(200L, "王子彬组", 100L, 2, 2))),
                snapshot(3L, "吉林省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(200L, "王子彬组", 100L, 2, 2), node(300L, "辽宁省组织", 200L, 3, 1),
                        node(400L, "沈阳市", 300L, 4, 1))),
                snapshot(4L, "辽宁省", List.of(node(100L, "东北区域", 0L, 1, 1),
                        node(200L, "王子彬组", 100L, 2, 2), node(300L, "辽宁省组织", 200L, 3, 1),
                        node(400L, "沈阳市", 300L, 4, 1)))));

        StoreOrgStatisticsRespVO result = service.getStatistics(7L, 100L, "2026-08");

        assertEquals("东北区域", result.getOrgName());
        assertEquals(4L, result.getTotalStoreCount());
        assertEquals(1L, result.getDirectStoreCount());
        assertEquals(1, result.getItems().size());
        var group = result.getItems().get(0);
        assertEquals("200", group.getOrgId());
        assertEquals(3L, group.getStoreCount());
        assertEquals(1L, group.getDirectStoreCount());
        var province = group.getChildren().get(0);
        assertEquals("300", province.getOrgId());
        assertEquals(2L, province.getStoreCount());
        assertEquals(0L, province.getDirectStoreCount());
        var city = province.getChildren().get(0);
        assertEquals("400", city.getOrgId());
        assertEquals(2L, city.getStoreCount());
        assertEquals(2L, city.getDirectStoreCount());
        assertTrue(city.getChildren().isEmpty());

        StoreOrgStatisticsRespVO groupResult = service.getStatistics(7L, 200L, "2026-08");
        assertEquals("王子彬组", groupResult.getOrgName());
        assertEquals(3L, groupResult.getTotalStoreCount());
        assertEquals(1L, groupResult.getDirectStoreCount());
        assertEquals("300", groupResult.getItems().get(0).getOrgId());

        StoreOrgStatisticsRespVO provinceResult = service.getStatistics(7L, 300L, "2026-08");
        assertEquals("辽宁省组织", provinceResult.getOrgName());
        assertEquals(2L, provinceResult.getTotalStoreCount());
        assertEquals(0L, provinceResult.getDirectStoreCount());
        assertEquals("400", provinceResult.getItems().get(0).getOrgId());

        StoreOrgStatisticsRespVO cityResult = service.getStatistics(7L, 400L, "2026-08");
        assertEquals("沈阳市", cityResult.getOrgName());
        assertEquals(2L, cityResult.getTotalStoreCount());
        assertEquals(2L, cityResult.getDirectStoreCount());
        assertTrue(cityResult.getItems().isEmpty());
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
