package com.htyoudao.youdao.module.system.service.org;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreTreeRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreSelectReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrgStoreSelectServiceScopeTest {

    private static final Pattern QUERY_VALUE = Pattern.compile("#\\{ew\\.paramNameValuePairs\\.([^}]+)}");
    private final OrgMapper organizations = mock(OrgMapper.class);
    private final SystemStoreInfoMapper stores = mock(SystemStoreInfoMapper.class);
    private final SystemStoreUserMapper managers = mock(SystemStoreUserMapper.class);
    private final OrgUserService orgUsers = mock(OrgUserService.class);
    private final PermissionService permissions = mock(PermissionService.class);
    private final OrgStoreSelectServiceImpl service = new OrgStoreSelectServiceImpl(
            organizations, stores, mock(SystemStoreTagMapper.class), mock(TagValueMapper.class),
            permissions, orgUsers, managers);
    private MockedStatic<SecurityFrameworkUtils> login;
    private JdbcTemplate jdbc;

    @BeforeAll
    static void initializeMappings() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), OrgDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                SystemStoreInfoDO.class);
    }

    @BeforeEach
    void initializeAccount() {
        BusinessContextHolder.setBusinessId(10L);
        login = mockStatic(SecurityFrameworkUtils.class);
        login.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(7L);
        DeptDataPermissionRespDTO permission = new DeptDataPermissionRespDTO();
        permission.setAll(true);
        when(permissions.getDeptDataPermission(7L, 10L)).thenReturn(permission);
        when(organizations.selectList(ArgumentMatchers.<LambdaQueryWrapper<OrgDO>>any())).thenReturn(List.of(
                organization(100L, 0L), organization(200L, 100L),
                organization(300L, 200L), organization(400L, 100L)));
        when(orgUsers.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of(200L));
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("""
                CREATE TABLE stores (store_id BIGINT, org_id BIGINT, business_id BIGINT,
                                     store_source VARCHAR(1), store_status INT, store_name VARCHAR(20))
                """);
        jdbc.update("""
                INSERT INTO stores VALUES
                (501,300,10,'0',0,'A'),(502,400,10,'0',0,'B'),(503,400,10,'0',0,'C'),
                (504,NULL,10,'0',0,'D'),(505,100,10,'0',0,'E'),(506,300,20,'0',0,'F'),
                (507,300,10,'0',1,'G'),(508,0,10,'0',0,'H'),(509,900,10,'0',0,'I')
                """);
        when(stores.selectList(any())).thenAnswer(invocation -> selectStores(invocation.getArgument(0)));
        when(stores.selectPage(any(OrgStoreSelectReqVO.class), any())).thenAnswer(invocation -> {
            List<SystemStoreInfoDO> result = selectStores(invocation.getArgument(1));
            return new PageResult<>(result, (long) result.size());
        });
    }

    @AfterEach
    void clearAccount() {
        login.close();
        BusinessContextHolder.clear();
    }

    @Test
    void appliesSameBranchAndManagerScopeToAllFiveEndpoints() {
        when(managers.getStoreIdsByUserId(7L, 10L)).thenReturn(List.of(502L, 504L, 506L, 508L, 509L));
        OrgStoreSelectReqVO request = new OrgStoreSelectReqVO();
        request.setScope(OrgStoreSelectReqVO.Scope.PROJECT);
        request.setFilterClosedStore(true);

        List<OrgStoreTreeRespVO> result = service.flatTree(request);

        assertEquals(List.of(200L, 300L, 501L, 502L), result.stream().map(OrgStoreTreeRespVO::getId).toList());
        assertEquals(List.of(0L, 200L, 300L, 0L), result.stream().map(OrgStoreTreeRespVO::getParentId).toList());
        assertEquals(1, result.get(0).getStoreNum());
        assertEquals(List.of(200L, 502L), service.tree(request).stream().map(node -> node.getId()).toList());
        assertEquals(List.of(501L, 502L), service.storeIds(request));
        assertEquals(List.of(501L, 502L), service.storeList(request).stream().map(store -> store.getStoreId()).toList());
        assertEquals(List.of(501L, 502L), service.storePage(request).getList().stream()
                .map(store -> store.getStoreId()).toList());
    }

    @Test
    void rejectsExplicitOrganizationOutsideAssignedBranch() {
        OrgStoreSelectReqVO request = new OrgStoreSelectReqVO();
        request.setOrgId(400L);

        assertTrue(service.flatTree(request).isEmpty());
        verifyNoInteractions(stores);
    }

    @Test
    void keepsDirectStorePermissionForAccountsWithoutOrganizationBindings() {
        DeptDataPermissionRespDTO permission = new DeptDataPermissionRespDTO();
        permission.setStoreIds(Set.of(502L));
        when(permissions.getDeptDataPermission(7L, 10L)).thenReturn(permission);
        when(orgUsers.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of());
        when(managers.getStoreIdsByUserId(7L, 10L)).thenReturn(List.of(502L));

        List<OrgStoreTreeRespVO> result = service.flatTree(new OrgStoreSelectReqVO());

        assertEquals(List.of(502L), result.stream().map(OrgStoreTreeRespVO::getId).toList());
        assertEquals(0L, result.get(0).getParentId());
    }

    @Test
    void returnsNoNodesForAccountsWithoutOrganizationOrManagedStore() {
        when(orgUsers.selectUserOrgIdsByBusinessId(7L, 10L)).thenReturn(List.of());

        assertTrue(service.flatTree(new OrgStoreSelectReqVO()).isEmpty());
        verifyNoInteractions(stores);
    }

    private List<SystemStoreInfoDO> selectStores(LambdaQueryWrapper<SystemStoreInfoDO> query) {
        var matcher = QUERY_VALUE.matcher(query.getCustomSqlSegment());
        Object[] values = matcher.results().map(match -> query.getParamNameValuePairs().get(match.group(1))).toArray();
        String sql = "SELECT store_id, org_id FROM stores " + matcher.replaceAll("?");
        return jdbc.query(sql, (row, index) -> store(row.getLong("store_id"), row.getLong("org_id")), values);
    }

    private static OrgDO organization(long id, long parentId) {
        OrgDO org = new OrgDO();
        org.setId(id);
        org.setParentId(parentId);
        org.setName("组织" + id);
        return org;
    }

    private static SystemStoreInfoDO store(long id, long orgId) {
        SystemStoreInfoDO store = new SystemStoreInfoDO();
        store.setStoreId(id);
        store.setOrgId(orgId);
        store.setStoreName("门店" + id);
        return store;
    }
}
