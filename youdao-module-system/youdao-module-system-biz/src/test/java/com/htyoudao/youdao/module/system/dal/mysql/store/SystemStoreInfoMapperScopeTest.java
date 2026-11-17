package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SystemStoreInfoMapperScopeTest {

    @BeforeAll
    static void initializeMappings() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                SystemStoreInfoDO.class);
    }

    @Test
    void selectsStoreIdsByProjectAndMultipleOrganizations() {
        SystemStoreInfoMapper mapper = mock(SystemStoreInfoMapper.class, CALLS_REAL_METHODS);
        SystemStoreInfoDO store = new SystemStoreInfoDO();
        store.setStoreId(100L);
        doAnswer(invocation -> {
            LambdaQueryWrapper<SystemStoreInfoDO> query = invocation.getArgument(0);
            assertEquals("store_id", query.getSqlSelect());
            String sql = query.getSqlSegment();
            assertTrue(sql.contains("business_id ="), sql);
            assertTrue(sql.contains("org_id IN ("), sql);
            assertEquals(Set.of(10L, 21L, 22L), new HashSet<>(query.getParamNameValuePairs().values()));
            return List.of(store);
        }).when(mapper).selectList(any());

        assertEquals(List.of(100L), mapper.selectStoreIdsByOrgIds(10L, List.of(21L, 22L)));
    }

    @Test
    void skipsQueryWithoutProjectOrOrganizations() {
        SystemStoreInfoMapper mapper = mock(SystemStoreInfoMapper.class, CALLS_REAL_METHODS);

        assertTrue(mapper.selectStoreIdsByOrgIds(10L, List.of()).isEmpty());
        assertTrue(mapper.selectStoreIdsByOrgIds(10L, null).isEmpty());
        assertTrue(mapper.selectStoreIdsByOrgIds(null, List.of(21L)).isEmpty());
        verify(mapper, never()).selectList(any());
    }
}
