package com.htyoudao.youdao.module.system.service.store;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StorePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StorePageManagerScopeTest {
    private SystemStoreInfoServiceImpl service;
    private SystemStoreInfoMapper stores;
    private SystemStoreUserMapper managers;
    private OrgUserService orgUsers;
    private OrgMapper orgs;
    private JdbcTemplate jdbc;
    private List<Long> selectedIds;

    @BeforeAll
    static void metadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SystemStoreUserDO.class);
    }

    @BeforeEach
    void setup() throws Exception {
        BusinessContextHolder.setBusinessId(10L);
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE system_store_info(store_id BIGINT, business_id BIGINT, org_id BIGINT, store_name VARCHAR(80), deleted BOOLEAN, store_source INT, store_status INT, open_status INT, campus_delivery_status INT, create_time TIMESTAMP)");
        jdbc.execute("CREATE TABLE system_store_user(store_id BIGINT, business_id BIGINT, user_id BIGINT, type INT, deleted BOOLEAN)");
        jdbc.execute("CREATE TABLE system_users(id BIGINT, nickname VARCHAR(80), mobile VARCHAR(32), deleted BOOLEAN)");
        jdbc.update("INSERT INTO system_users VALUES (7, 'manager', '13800000000', FALSE), (8, 'other', '13900000000', FALSE)");
        for (long id = 101; id <= 108; id++) {
            jdbc.update("INSERT INTO system_store_info VALUES (?, ?, ?, ?, ?, 0, 0, 0, 0, CURRENT_TIMESTAMP)",
                    id, id == 106 ? 20L : 10L, id == 102 ? 200L : 100L, "store" + id, id == 107);
        }
        jdbc.update("INSERT INTO system_store_user VALUES (101,10,7,1,FALSE),(102,10,7,1,FALSE),(103,10,8,1,FALSE),(104,10,7,0,FALSE),(105,10,7,1,TRUE),(106,20,7,1,FALSE),(107,10,7,1,FALSE),(108,20,7,1,FALSE)");
        service = new SystemStoreInfoServiceImpl();
        stores = mock(SystemStoreInfoMapper.class);
        managers = mock(SystemStoreUserMapper.class);
        orgUsers = mock(OrgUserService.class);
        orgs = mock(OrgMapper.class);
        when(orgUsers.selectUserOrgIds(7L)).thenReturn(List.of());
        when(managers.selectCount(any(Wrapper.class))).thenReturn(2L);
        when(orgs.getChildIdListByStore(any())).thenAnswer(call -> new HashSet<Long>());
        ReflectionTestUtils.setField(service, "systemStoreInfoMapper", stores);
        ReflectionTestUtils.setField(service, "systemStoreUserMapper", managers);
        ReflectionTestUtils.setField(service, "orgUserService", orgUsers);
        ReflectionTestUtils.setField(service, "orgMapper", orgs);

        try (var input = getClass().getResourceAsStream("/mapper/store/SystemStoreInfoMapper.xml")) {
            assertNotNull(input);
            var parser = new XPathParser(input, false, null, new XMLMapperEntityResolver());
            var configuration = new Configuration();
            var sqlSource = new XMLLanguageDriver().createSqlSource(configuration,
                    parser.evalNode("/mapper/select[@id='selectPageList']"), Map.class);
            when(stores.selectPageList(any(), any(), any(), any(), anyBoolean())).thenAnswer(call -> {
                Map<String, Object> params = new HashMap<>();
                params.put("record", call.getArgument(1)); params.put("businessId", call.getArgument(2));
                params.put("userId", call.getArgument(3)); params.put("managerOnly", call.getArgument(4));
                BoundSql sql = sqlSource.getBoundSql(params);
                List<Object> values = new ArrayList<>();
                for (var mapping : sql.getParameterMappings()) {
                    String property = mapping.getProperty();
                    values.add(sql.hasAdditionalParameter(property) ? sql.getAdditionalParameter(property)
                            : configuration.newMetaObject(params).getValue(property));
                }
                // H2 不识别 MySQL 的位字面量，仅在测试中替换为等价布尔值。
                String executableSql = sql.getSql().replace("b'0'", "FALSE").replace("B'0'", "FALSE");
                selectedIds = jdbc.query(executableSql, (rs, row) -> rs.getLong("store_id"), values.toArray());
                // 只隔离分页后的名称补充，权限 SQL 使用正式 XML 在数据库中执行。
                return new Page<StoreResVO>().setRecords(List.of()).setTotal(selectedIds.size());
            });
        }
    }

    @AfterEach
    void clear() {
        if (jdbc != null) jdbc.execute("SHUTDOWN");
        BusinessContextHolder.clear();
    }

    @Test
    void managerWithoutOrgSeesOnlyOwnLiveManagerRelationsInCurrentProject() {
        query(new StorePageReqVO());
        assertEquals(Set.of(101L, 102L), new HashSet<>(selectedIds));
        var where = ArgumentCaptor.forClass(Wrapper.class);
        verify(managers).selectCount(where.capture());
        var wrapper = (AbstractWrapper<?, ?, ?>) where.getValue();
        String sql = wrapper.getSqlSegment();
        assertTrue(sql.contains("business_id")); assertTrue(sql.contains("user_id"));
        assertTrue(sql.contains("type")); assertTrue(sql.contains("deleted"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(10L));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(7L));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(false));
    }

    @Test
    void searchAndStatusFiltersNeverRemoveManagerRestriction() {
        var request = new StorePageReqVO();
        request.setText("store103"); request.setStoreStatus(0); request.setOpenStatus(0);
        query(request);
        assertTrue(selectedIds.isEmpty());
        request.setText("store101");
        query(request);
        assertEquals(List.of(101L), selectedIds);
    }

    @Test
    void suppliedOrgRangeCannotGrantOtherStoresToManagerWithoutOrg() {
        var request = new StorePageReqVO();
        request.setOrgIds(Set.of(100L));
        query(request);
        assertEquals(Set.of(101L, 102L), new HashSet<>(selectedIds));
    }

    @Test
    void deletedOrgJoinReturningNullStillCountsAsNoOrg() {
        when(orgUsers.selectUserOrgIds(7L)).thenReturn(java.util.Arrays.asList((Long) null));
        query(new StorePageReqVO());
        assertEquals(Set.of(101L, 102L), new HashSet<>(selectedIds));
    }

    @Test
    void userWithOrgKeepsExistingOrgScopeAndDoesNotNeedManagerLookup() {
        when(orgUsers.selectUserOrgIds(7L)).thenReturn(List.of(100L));
        var request = new StorePageReqVO();
        request.setOrgIds(Set.of(100L)); request.setText("store103");
        query(request);
        assertEquals(List.of(103L), selectedIds);
        verifyNoInteractions(managers);
    }

    @Test
    void userWithOrgKeepsUnionOfOrgStoresAndManagedStoresOutsideOrg() {
        when(orgUsers.selectUserOrgIds(7L)).thenReturn(List.of(900L));
        when(orgs.getChildIdListByStore(900L)).thenReturn(new HashSet<>(Set.of(100L)));
        query(new StorePageReqVO());
        assertEquals(Set.of(101L, 102L, 103L, 104L, 105L, 108L), new HashSet<>(selectedIds));
        verifyNoInteractions(managers);
    }

    @Test
    void nonManagerWithoutOrgKeepsExistingBehavior() {
        when(managers.selectCount(any(Wrapper.class))).thenReturn(0L);
        var request = new StorePageReqVO(); request.setText("store103");
        query(request);
        assertEquals(List.of(103L), selectedIds);
    }

    private void query(StorePageReqVO request) {
        try (var login = mockStatic(WebFrameworkUtils.class)) {
            login.when(WebFrameworkUtils::getLoginUserId).thenReturn(7L);
            var result = service.getStorePage(request);
            assertEquals(selectedIds.size(), result.getTotal());
        }
    }
}
