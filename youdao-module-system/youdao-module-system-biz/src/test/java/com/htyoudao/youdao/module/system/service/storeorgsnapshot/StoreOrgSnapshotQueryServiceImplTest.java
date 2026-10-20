package com.htyoudao.youdao.module.system.service.storeorgsnapshot;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StoreOrgSnapshotQueryServiceImplTest {

    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SystemStoreInfoDO.class);
    }

    @InjectMocks
    private StoreOrgSnapshotQueryServiceImpl service;
    @Mock
    private SystemStoreInfoMapper storeMapper;
    @Mock
    private OrgMapper orgMapper;
    @Mock
    private OrgUserMapper orgUserMapper;

    @AfterEach
    void clearContext() {
        BusinessContextHolder.clear();
    }

    @Test
    void shouldFilterOrganizationAndDirectRelationsToNormalStores() {
        BusinessContextHolder.setBusinessId(10L);
        var api = org.mockito.Mockito.spy(new com.htyoudao.youdao.module.system.api.store.StoreApiImpl());
        var queryService = org.mockito.Mockito.mock(StoreOrgSnapshotQueryService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(api, "storeOrgSnapshotQueryService", queryService);
        StoreOrgSnapshotDTO normal = new StoreOrgSnapshotDTO();
        normal.setStoreId(900L);
        when(queryService.listNormalStoreOrgData()).thenReturn(List.of(normal));
        org.mockito.Mockito.doReturn(com.htyoudao.youdao.framework.common.pojo.CommonResult.success(
                Set.of(900L, 901L, 902L, 903L))).when(api).getAllStoreIdByUser(1L);
        var info = new com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO();
        info.setStoreId(900L);
        info.setStoreName("正常营业门店");
        org.mockito.Mockito.doReturn(List.of(info)).when(api).listStoresByIds(List.of(900L));
        var relations = api.listUserStoreRelations(List.of(1L), null);
        assertEquals(1, relations.size());
        assertEquals(900L, relations.get(0).getStoreId());
        assertEquals("正常营业门店", relations.get(0).getStoreName());
    }

    @Test
    void shouldBuildCompletePathAndParent() {
        BusinessContextHolder.setBusinessId(10L);
        when(storeMapper.selectList(any())).thenReturn(List.of(store(900L, 300L)));
        when(orgMapper.selectList(orgWrapper())).thenReturn(List.of(
                org(100L, "大区", 0L, 1, 1),
                org(200L, "小组", 100L, 2, 2),
                org(300L, "省区", 200L, 3, 3)));

        List<StoreOrgSnapshotDTO> result = service.listNormalStoreOrgData();

        assertEquals(1, result.size());
        StoreOrgSnapshotDTO dto = result.get(0);
        assertEquals(200L, dto.getParentOrgId());
        assertEquals("小组", dto.getParentOrgName());
        assertEquals(List.of(100L, 200L, 300L),
                dto.getOrgPath().stream().map(node -> node.getId()).toList());
    }

    @Test
    void shouldReturnDirectOrganizationsAndAllDescendantsOnly() {
        BusinessContextHolder.setBusinessId(10L);
        when(orgUserMapper.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of(200L));
        when(orgMapper.selectList(orgWrapper())).thenReturn(List.of(
                org(100L, "大区", 0L, 1, 1),
                org(200L, "小组", 100L, 2, 2),
                org(300L, "省区", 200L, 3, 3),
                org(400L, "其他小组", 100L, 2, 4)));

        Set<Long> result = service.listManageableOrgIds(7L);

        assertEquals(Set.of(200L, 300L), result);
    }

    @Test
    void shouldDefaultToOwnOrganizationWithStableOrder() {
        BusinessContextHolder.setBusinessId(20L);
        when(orgUserMapper.selectUserOrgIdsByBusinessId(7L, 20L)).thenReturn(List.of(400L, 300L, 200L, 999L));
        when(orgMapper.selectList(orgWrapper())).thenReturn(List.of(
                org(100L, "总部", 0L, 1, 1),
                org(400L, "小组二", 100L, 2, 2),
                org(300L, "省区", 200L, 3, 1),
                org(200L, "小组一", 100L, 2, 1)));

        assertEquals(200L, service.getUserDefaultOrgId(7L));
    }

    @Test
    void shouldReturnNullWithoutOrganizationMembership() {
        BusinessContextHolder.setBusinessId(10L);
        when(orgUserMapper.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of());

        assertNull(service.getUserDefaultOrgId(7L));
        verifyNoInteractions(orgMapper, storeMapper);
    }

    @Test
    void shouldIgnoreMembershipOfUnavailableOrganization() {
        BusinessContextHolder.setBusinessId(10L);
        when(orgUserMapper.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of(999L));
        when(orgMapper.selectList(orgWrapper())).thenReturn(List.of(org(100L, "总部", 0L, 1, 1)));

        assertNull(service.getUserDefaultOrgId(7L));
    }

    @Test
    void shouldFailWithoutBusinessContext() {
        assertThrows(NullPointerException.class, service::listNormalStoreOrgData);
        assertThrows(NullPointerException.class, () -> service.getUserDefaultOrgId(7L));
        verifyNoInteractions(orgUserMapper, orgMapper, storeMapper);
    }

    private SystemStoreInfoDO store(Long storeId, Long orgId) {
        SystemStoreInfoDO store = new SystemStoreInfoDO();
        store.setStoreId(storeId);
        store.setStoreName("门店" + storeId);
        store.setStoreProvince("辽宁省");
        store.setOrgId(orgId);
        return store;
    }

    private OrgDO org(Long id, String name, Long parentId, Integer level, Integer sort) {
        OrgDO org = new OrgDO();
        org.setId(id);
        org.setName(name);
        org.setParentId(parentId);
        org.setLevel(level);
        org.setSort(sort);
        return org;
    }

    @SuppressWarnings("unchecked")
    private Wrapper<OrgDO> orgWrapper() {
        return any(Wrapper.class);
    }
}
