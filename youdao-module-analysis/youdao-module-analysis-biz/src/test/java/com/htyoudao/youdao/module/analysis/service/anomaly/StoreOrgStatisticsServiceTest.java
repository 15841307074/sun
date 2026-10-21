package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.UserStoreSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.UserStoreSnapshotMapper;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
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
    @Mock
    private UserStoreSnapshotMapper userStoreSnapshotMapper;
    private AutoCloseable mocks;

    @BeforeAll
    static void initializeSnapshotMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), UserStoreSnapshotDO.class);
    }

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new StoreOrgStatisticsService();
        ReflectionTestUtils.setField(service, "snapshotMapper", snapshotMapper);
        ReflectionTestUtils.setField(service, "storeOrgSnapshotApi", storeOrgSnapshotApi);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(service, "userStoreSnapshotMapper", userStoreSnapshotMapper);
        BusinessContextHolder.setBusinessId(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        BusinessContextHolder.clear();
        mocks.close();
    }

    @Test
    void shouldUseCurrentUserOrganization() throws Exception {
        when(storeOrgSnapshotApi.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(200L));
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(200L, 300L)));
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of(
                snapshot(1L, "辽宁省", List.of(node(100L, "总部", 0L, 1, 1),
                        node(200L, "小组", 100L, 2, 1), node(300L, "省区", 200L, 3, 1)))));

        StoreOrgStatisticsRespVO result = service.getStatistics(7L, "2026-08");

        assertEquals("200", result.getOrgId());
        assertEquals("小组", result.getOrgName());
        assertEquals(1L, result.getTotalStoreCount());
        assertEquals("300", result.getItems().get(0).getOrgId());
    }

    @Test
    void shouldRejectUserWithoutDefaultOrganization() {
        when(storeOrgSnapshotApi.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(null));
        when(storeOrgSnapshotApi.listManageableOrgIds(7L)).thenReturn(CommonResult.success(Set.of()));

        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,
                () -> service.getStatistics(7L, "2026-08"));
        verifyNoInteractions(snapshotMapper);
    }

    @Test
    void shouldReturnDynamicOrganizationTreeFromUserOrganization() throws Exception {
        when(storeOrgSnapshotApi.getUserDefaultOrgId(7L))
                .thenReturn(CommonResult.success(100L), CommonResult.success(200L),
                        CommonResult.success(300L), CommonResult.success(400L));
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

        StoreOrgStatisticsRespVO result = service.getStatistics(7L, "2026-08");

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

        StoreOrgStatisticsRespVO groupResult = service.getStatistics(7L, "2026-08");
        assertEquals("王子彬组", groupResult.getOrgName());
        assertEquals(3L, groupResult.getTotalStoreCount());
        assertEquals(1L, groupResult.getDirectStoreCount());
        assertEquals("300", groupResult.getItems().get(0).getOrgId());

        StoreOrgStatisticsRespVO provinceResult = service.getStatistics(7L, "2026-08");
        assertEquals("辽宁省组织", provinceResult.getOrgName());
        assertEquals(2L, provinceResult.getTotalStoreCount());
        assertEquals(0L, provinceResult.getDirectStoreCount());
        assertEquals("400", provinceResult.getItems().get(0).getOrgId());

        StoreOrgStatisticsRespVO cityResult = service.getStatistics(7L, "2026-08");
        assertEquals("沈阳市", cityResult.getOrgName());
        assertEquals(2L, cityResult.getTotalStoreCount());
        assertEquals(2L, cityResult.getDirectStoreCount());
        assertTrue(cityResult.getItems().isEmpty());
        verify(storeOrgSnapshotApi, times(4)).getUserDefaultOrgId(7L);
    }

    @Test
    void shouldRejectOrganizationOutsideUserScope() {
        when(storeOrgSnapshotApi.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(100L));
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(200L)));

        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, "2026-08"));
        verifyNoInteractions(snapshotMapper);
    }

    @Test
    void shouldRejectMissingMonthSnapshot() {
        when(storeOrgSnapshotApi.getUserDefaultOrgId(7L)).thenReturn(CommonResult.success(100L));
        when(storeOrgSnapshotApi.listManageableOrgIds(7L))
                .thenReturn(CommonResult.success(Set.of(100L)));
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, "2026-08"));
    }

    @Test
    void shouldRejectInvalidMonthBeforeRemoteCall() {
        assertThrows(RuntimeException.class,
                () -> service.getStatistics(7L, "2026-8"));
        verifyNoInteractions(storeOrgSnapshotApi, snapshotMapper);
    }

    @Test
    void monthlyStoresIntersectHistoricalUserStoresWithTheWholeOrganizationPath() throws Exception {
        var direct = monthlyStore(30L, 200L, List.of(node(100L, "总部", 0L, 1, 0), node(200L, "小组", 100L, 2, 0)));
        var city = monthlyStore(10L, 400L, List.of(node(100L, "总部", 0L, 1, 0), node(200L, "小组", 100L, 2, 0),
                node(300L, "省区", 200L, 3, 0), node(400L, "城市", 300L, 4, 0)));
        var outside = monthlyStore(20L, 500L, List.of(node(100L, "总部", 0L, 1, 0), node(500L, "其他小组", 100L, 2, 1)));
        var notManaged = monthlyStore(40L, 200L, List.of());notManaged.setOrgPathJson("bad unrelated data");
        var unassigned = monthlyStore(50L, null, List.of());
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1)))
                .thenReturn(List.of(direct, city, outside, notManaged, unassigned, city));
        when(userStoreSnapshotMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(userStore(10L), userStore(20L), userStore(30L), userStore(50L)));
        assertEquals(List.of(10L, 30L), service.getMonthOrgStores(7L, "2026-08", 200L).stream().map(StoreOrgSnapshotDO::getStoreId).toList());
        assertEquals(List.of(10L), service.getMonthOrgStores(7L, "2026-08", 300L).stream().map(StoreOrgSnapshotDO::getStoreId).toList());
        assertEquals(List.of(10L), service.getMonthOrgStores(7L, "2026-08", 400L).stream().map(StoreOrgSnapshotDO::getStoreId).toList());
        assertEquals(List.of(10L, 20L, 30L), service.getMonthOrgStores(7L, "2026-08", 100L).stream().map(StoreOrgSnapshotDO::getStoreId).toList());
        assertTrue(service.getMonthOrgStores(7L, "2026-08", 999L).isEmpty());
        // 只读历史快照，人员调动和门店当前状态不改变历史结果。
        verifyNoInteractions(storeOrgSnapshotApi);
    }

    @Test
    void monthlyStoresUseExactlyTheRequestedMonthAndExplicitProjectScope() throws Exception {
        BusinessContextHolder.setBusinessId(20L);
        var row = monthlyStore(9L, 200L, List.of());row.setStoreName("当月门店名称");
        when(snapshotMapper.selectListByMonth(20L, LocalDate.of(2025, 12, 1))).thenReturn(List.of(row));
        when(userStoreSnapshotMapper.selectList(any(Wrapper.class))).thenReturn(List.of(userStore(9L)));
        assertEquals("当月门店名称", service.getMonthOrgStores(7L, "2025-12", 200L).get(0).getStoreName());
        ArgumentCaptor<Wrapper> query = ArgumentCaptor.forClass(Wrapper.class);
        verify(userStoreSnapshotMapper).selectList(query.capture());
        var where = (AbstractWrapper<?, ?, ?>) query.getValue();String sql = where.getSqlSegment();
        for (String column : List.of("business_id", "period_type", "period_start", "user_id", "deleted")) assertTrue(sql.contains(column));
        var values = where.getParamNameValuePairs().values();
        assertTrue(values.contains(20L));assertTrue(values.contains(7L));assertTrue(values.contains(2));
        assertTrue(values.contains(LocalDate.of(2025, 12, 1)));assertTrue(values.contains(false));
        verifyNoInteractions(storeOrgSnapshotApi);
    }

    @Test
    void monthlyStoresReturnEmptyWhenThePersonHasNoMonthlyStores() throws Exception {
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of(monthlyStore(1L, 200L, List.of())));
        when(userStoreSnapshotMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        assertTrue(service.getMonthOrgStores(7L, "2026-08", 200L).isEmpty());
        verifyNoInteractions(storeOrgSnapshotApi);
    }

    @Test
    void monthlyStoresRejectMissingOrganizationSnapshot() {
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of());
        var failure = assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,
                () -> service.getMonthOrgStores(7L, "2026-08", 200L));
        assertEquals(com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.STORE_ORG_SNAPSHOT_NOT_FOUND.getCode(), failure.getCode());
        verifyNoInteractions(userStoreSnapshotMapper, storeOrgSnapshotApi);
    }

    @Test
    void monthlyStoresRejectInvalidParametersBeforeQuerying() {
        for (String month : new String[]{null, "2026-8", "2026-13", "2026-08-01"}) {
            assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getMonthOrgStores(7L, month, 200L));
        }
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getMonthOrgStores(null, "2026-08", 200L));
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getMonthOrgStores(7L, "2026-08", 0L));
        BusinessContextHolder.clear();
        assertThrows(RuntimeException.class, () -> service.getMonthOrgStores(7L, "2026-08", 200L));
        verifyNoInteractions(snapshotMapper, userStoreSnapshotMapper, storeOrgSnapshotApi);
    }

    @Test
    void monthlyStoresRejectDamagedOrganizationPathInsteadOfUsingLiveOrganizations() {
        var row = new StoreOrgSnapshotDO();row.setStoreId(1L);row.setOrgId(400L);row.setOrgPathJson("bad json");
        when(snapshotMapper.selectListByMonth(10L, LocalDate.of(2026, 8, 1))).thenReturn(List.of(row));
        when(userStoreSnapshotMapper.selectList(any(Wrapper.class))).thenReturn(List.of(userStore(1L)));
        for (String broken : List.of("bad json", "null", "[null]")) {
            row.setOrgPathJson(broken);
            var failure = assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class, () -> service.getMonthOrgStores(7L, "2026-08", 200L));
            assertEquals(com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.STORE_ORG_SNAPSHOT_DATA_INVALID.getCode(), failure.getCode());
        }
        verifyNoInteractions(storeOrgSnapshotApi);
    }

    private StoreOrgSnapshotDO monthlyStore(Long storeId, Long orgId, List<StoreOrgPathNodeDTO> path) throws Exception {
        var row = snapshot(storeId, "辽宁省", path);row.setOrgId(orgId);return row;
    }

    private UserStoreSnapshotDO userStore(Long storeId) {
        var row = new UserStoreSnapshotDO();row.setStoreId(storeId);return row;
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
