package com.htyoudao.youdao.module.system.service.store;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.controller.app.store.vo.BossStoreSimpleRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** 验证精简查询与原接口范围一致，避免优化时扩大门店权限。 */
class BossStoreSimpleQueryTest {

    @BeforeAll
    static void initTable() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                SystemStoreInfoDO.class);
    }

    @Test
    void normalStoreFiltersMatchForEveryScopeCombination() {
        List<Set<Long>> scopes = List.of(Set.of(), Set.of(101L, 102L));
        for (Set<Long> orgIds : scopes) {
            for (Set<Long> storeIds : scopes) {
                List<Wrapper<?>> queries = new ArrayList<>();
                SystemStoreInfoMapper mapper = capturingMapper(queries);
                mapper.selectBossStoreList(orgIds, storeIds);
                mapper.selectBossSimpleStoreList(orgIds, storeIds);
                if (orgIds.isEmpty() && storeIds.isEmpty()) {
                    assertTrue(queries.isEmpty());
                } else {
                    assertEquivalentFiltersAndNarrowProjection(queries);
                }
            }
        }
    }

    @Test
    void supplyStoreFiltersMatchIncludingMissingBusinessContext() {
        for (Long businessId : new Long[] {10L, 11L, null}) {
            List<Wrapper<?>> queries = new ArrayList<>();
            SystemStoreInfoMapper mapper = capturingMapper(queries);
            mapper.selectBossSupplyStoreList(7L, businessId);
            mapper.selectBossSimpleSupplyStoreList(7L, businessId);
            assertEquivalentFiltersAndNarrowProjection(queries);
        }
    }

    @Test
    void supplyPermissionAnnotationMatchesOriginal() throws Exception {
        assertEquals(SystemStoreInfoMapper.class.getMethod("selectBossSupplyStoreList", Long.class, Long.class)
                        .getAnnotation(DataPermission.class).enable(),
                SystemStoreInfoMapper.class.getMethod("selectBossSimpleSupplyStoreList", Long.class, Long.class)
                        .getAnnotation(DataPermission.class).enable());
        assertNull(SystemStoreInfoMapper.class.getMethod("selectBossSimpleStoreList", Set.class, Set.class)
                .getAnnotation(DataPermission.class));
    }

    @Test
    void responseContainsOnlyFiveFieldsIncludingUnsetStatus() {
        BossStoreSimpleRespVO response = new BossStoreSimpleRespVO();
        response.setStoreId(101L);
        response.setStoreName("测试门店");
        var node = new ObjectMapper().valueToTree(response);
        assertEquals(5, node.size());
        for (String field : List.of("id", "storeId", "name", "storeName", "useStatus")) {
            assertTrue(node.has(field));
        }
        assertEquals(node.get("storeId"), node.get("id"));
        assertEquals(node.get("storeName"), node.get("name"));
        assertEquals(101L, node.get("id").asLong());
        assertEquals("测试门店", node.get("name").asText());
        assertTrue(node.get("useStatus").isNull());
    }

    /** 执行真实默认查询方法，仅截获数据库入口，测试无需连接数据库。 */
    private SystemStoreInfoMapper capturingMapper(List<Wrapper<?>> queries) {
        return (SystemStoreInfoMapper) Proxy.newProxyInstance(SystemStoreInfoMapper.class.getClassLoader(),
                new Class<?>[] {SystemStoreInfoMapper.class}, (proxy, method, args) -> {
                    if (method.getName().equals("selectList") && args.length == 1) {
                        queries.add((Wrapper<?>) args[0]);
                        return List.of();
                    }
                    if (method.isDefault()) {
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private void assertEquivalentFiltersAndNarrowProjection(List<Wrapper<?>> queries) {
        assertEquals(2, queries.size());
        AbstractWrapper<?, ?, ?> original = (AbstractWrapper<?, ?, ?>) queries.get(0);
        AbstractWrapper<?, ?, ?> simple = (AbstractWrapper<?, ?, ?>) queries.get(1);
        assertEquals(original.getSqlSegment(), simple.getSqlSegment());
        assertEquals(original.getParamNameValuePairs(), simple.getParamNameValuePairs());
        assertEquals(Set.of("store_id", "store_name", "use_status"),
                Set.of(simple.getSqlSelect().split(",")));
    }
}
