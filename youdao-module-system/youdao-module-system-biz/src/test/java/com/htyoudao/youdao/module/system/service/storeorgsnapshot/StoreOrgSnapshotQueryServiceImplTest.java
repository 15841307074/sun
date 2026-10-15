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
    void shouldFailWithoutBusinessContext() {
        assertThrows(NullPointerException.class, service::listNormalStoreOrgData);
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
