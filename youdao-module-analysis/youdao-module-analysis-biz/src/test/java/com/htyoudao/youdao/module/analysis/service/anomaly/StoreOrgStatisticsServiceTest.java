package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.app.AnomalyAppController;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoreOrgStatisticsServiceTest {
    private StoreOrgStatisticsService service;
    private StoreOrgSnapshotApi api;
    @BeforeEach void setUp() {
        BusinessContextHolder.setBusinessId(10L);
        service = new StoreOrgStatisticsService();api = mock(StoreOrgSnapshotApi.class);
        ReflectionTestUtils.setField(service, "storeOrgSnapshotApi", api);
        when(api.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(100L));
        when(api.listManageableOrgIds(7L)).thenReturn(CommonResult.success(Set.of(100L, 200L, 300L, 400L)));
    }
    @AfterEach void clear() { BusinessContextHolder.clear(); }

    @Test void currentTreeWorksAtHeadquartersGroupProvinceAndCity() {
        var root = node(100, "总部", 0, 1, 0);var group = node(200, "小组", 100, 2, 0);
        var province = node(300, "省区", 200, 3, 0);var city = node(400, "城市", 300, 4, 0);
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(
                store(1, List.of(root)), store(2, List.of(root, group)),
                store(3, List.of(root, group, province, city)), store(4, List.of(root, group, province, city)))));
        when(api.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(100L), CommonResult.success(200L), CommonResult.success(300L), CommonResult.success(400L));
        var headquarters = service.getStatistics(7L);
        assertEquals("100", headquarters.getOrgId());assertEquals(4L, headquarters.getTotalStoreCount());assertEquals(1L, headquarters.getDirectStoreCount());
        var groupNode = headquarters.getItems().get(0);assertEquals("200", groupNode.getOrgId());assertEquals(3L, groupNode.getStoreCount());assertEquals(1L, groupNode.getDirectStoreCount());
        assertEquals(2L, groupNode.getChildren().get(0).getStoreCount());
        assertEquals("400", groupNode.getChildren().get(0).getChildren().get(0).getOrgId());
        var groupResult = service.getStatistics(7L);assertEquals(3L, groupResult.getTotalStoreCount());assertEquals(1L, groupResult.getDirectStoreCount());
        var provinceResult = service.getStatistics(7L);assertEquals(2L, provinceResult.getTotalStoreCount());assertEquals(0L, provinceResult.getDirectStoreCount());
        var cityResult = service.getStatistics(7L);assertEquals(2L, cityResult.getTotalStoreCount());assertEquals(2L, cityResult.getDirectStoreCount());assertTrue(cityResult.getItems().isEmpty());
    }

    @Test void everyRequestUsesLatestStoresAndOrganizationPaths() {
        var root = node(100, "总部", 0, 1, 0);var group = node(200, "原小组", 100, 2, 0);
        var moved = node(500, "其他区域", 0, 1, 0);
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(store(1, List.of(root, group)))),
                CommonResult.success(List.of(store(1, List.of(moved)))), CommonResult.success(List.of()));
        assertEquals(1L, service.getStatistics(7L).getTotalStoreCount());
        assertEquals(0L, service.getStatistics(7L).getTotalStoreCount());
        assertEquals(0L, service.getStatistics(7L).getTotalStoreCount());
        verify(api, times(3)).listNormalStoreOrgData();
    }

    @Test void noStoresIsANormalZeroResult() {
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of()));
        var result = service.getStatistics(7L);
        assertEquals("100", result.getOrgId());assertEquals(0L, result.getTotalStoreCount());
        assertEquals(0L, result.getDirectStoreCount());assertTrue(result.getItems().isEmpty());
    }

    @Test void recordsAreDeduplicatedSortedAndUnassignedStoresAreExcluded() {
        var root = node(100, "总部", 0, 1, 0);var first = store(3, List.of(root));var second = store(1, List.of(root));
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(Arrays.asList(first, second, first, store(4, List.of()), store(5, null))));
        assertEquals(List.of(1L, 3L), service.getOrgStores(7L, 100L).stream().map(StoreOrgSnapshotDTO::getStoreId).toList());
        var result = service.getStatistics(7L);assertEquals(2L, result.getTotalStoreCount());assertEquals(2L, result.getDirectStoreCount());
    }

    @Test void explicitOrganizationUsesCurrentPermissionAndWholeIdMatching() {
        var root = node(100, "总部", 0, 1, 0);var selected = node(200, "小组", 100, 2, 0);var other = node(2000, "其他", 100, 2, 0);
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(store(1, List.of(root, selected)), store(2, List.of(root, other)))));
        assertEquals(List.of(1L), service.getOrgStores(7L, 200L).stream().map(StoreOrgSnapshotDTO::getStoreId).toList());
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getOrgStores(7L, 999L));
        verify(api).listNormalStoreOrgData();
    }

    @Test void descendantsFollowSortLevelAndIdAndIdsAreStrings() {
        long large = 9007199254740993L;
        when(api.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(large));
        when(api.listManageableOrgIds(7L)).thenReturn(CommonResult.success(Set.of(large)));
        var root = node(large, "总部", 0, 1, 0);
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(
                store(1, List.of(root, node(201, "后", large, 2, 3))),store(2, List.of(root, node(202, "先", large, 2, 1))))));
        var result = service.getStatistics(7L);assertEquals(Long.toString(large), result.getOrgId());
        assertEquals(List.of("202", "201"), result.getItems().stream().map(n -> n.getOrgId()).toList());
    }

    @Test void aUserWithoutDefaultOrganizationCannotLoadStoreData() {
        when(api.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(null));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
        verify(api, never()).listNormalStoreOrgData();
    }

    @Test void permissionChangesAreCheckedOnEveryRequest() {
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of()));
        service.getStatistics(7L);
        when(api.listManageableOrgIds(7L)).thenReturn(CommonResult.success(Set.of()));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
        verify(api).listNormalStoreOrgData();
    }

    @Test void currentProjectContextIsRequiredBeforeRemoteCalls() {
        BusinessContextHolder.clear();
        assertThrows(RuntimeException.class, () -> service.getStatistics(7L));
        assertThrows(RuntimeException.class, () -> service.getOrgStores(7L, 100L));
        verifyNoInteractions(api);
    }

    @Test void projectAndUserContextArePreserved() {
        BusinessContextHolder.setBusinessId(20L);
        when(api.getUserDefaultOrgId(7L)).thenAnswer(call -> { assertEquals(20L, BusinessContextHolder.getRequiredBusinessId()); return CommonResult.success(100L); });
        when(api.listNormalStoreOrgData()).thenAnswer(call -> { assertEquals(20L, BusinessContextHolder.getRequiredBusinessId()); return CommonResult.success(List.of()); });
        service.getStatistics(7L);assertEquals(20L, BusinessContextHolder.getRequiredBusinessId());
    }

    @Test void invalidUserAndOrganizationCannotQueryAllData() {
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(null));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getOrgStores(7L, 0L));
        verifyNoInteractions(api);
    }

    @Test void nullOrDamagedRemoteDataDoesNotBecomeAFalseZeroResult() {
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(null));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
        var invalid = new StoreOrgSnapshotDTO();
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(invalid)));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of(store(1, Arrays.asList((StoreOrgPathNodeDTO)null)))));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
    }

    @Test void remoteFailureIsNotReportedAsNoStores() {
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.error(500, "system unavailable"));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getStatistics(7L));
    }

    @Test void appEndpointWorksWithoutMonthAndDoesNotReturnMonth() throws Exception {
        var controller = new AnomalyAppController();ReflectionTestUtils.setField(controller, "storeOrgStatisticsService", service);
        when(api.listNormalStoreOrgData()).thenReturn(CommonResult.success(List.of()));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        try (var security = mockStatic(SecurityFrameworkUtils.class)) {
            security.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);
            mvc.perform(get("/analysis/app/anomaly-report/org-store-statistics").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.totalStoreCount").value(0)).andExpect(jsonPath("$.data.month").doesNotExist());
            mvc.perform(get("/analysis/app/anomaly-report/org-store-statistics").accept(org.springframework.http.MediaType.APPLICATION_JSON).param("month", "2026-08"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.month").doesNotExist());
        }
    }

    private StoreOrgSnapshotDTO store(long id, List<StoreOrgPathNodeDTO> path) {
        var store = new StoreOrgSnapshotDTO();store.setStoreId(id);store.setStoreName("门店"+id);store.setOrgPath(path);
        if (path != null && !path.isEmpty() && path.get(path.size()-1) != null) store.setOrgId(path.get(path.size()-1).getId());
        return store;
    }
    private StoreOrgPathNodeDTO node(long id, String name, long parent, int level, int sort) {
        var node = new StoreOrgPathNodeDTO();node.setId(id);node.setName(name);node.setParentId(parent);node.setLevel(level);node.setSort(sort);return node;
    }
}
