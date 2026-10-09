package com.htyoudao.youdao.module.system.service.complant;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.app.complaint.AppComplaintController;
import com.htyoudao.youdao.module.system.dal.dataobject.complaint.ComplaintDO;
import com.htyoudao.youdao.module.system.dal.mysql.complaint.ComplaintMapper;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComplaintRegionPendingCountTest {
    private ComplaintServiceImpl service;
    private ComplaintMapper mapper;
    private OrgService org;
    private StoreUserMapper bindings;
    private JdbcTemplate jdbc;
    private MockedStatic<WebFrameworkUtils> login;
    private Long previousBusiness;
    @BeforeEach void setup() {
        previousBusiness=BusinessContextHolder.getBusinessId();BusinessContextHolder.setBusinessId(10L);
        login=mockStatic(WebFrameworkUtils.class);login.when(WebFrameworkUtils::getLoginUserId).thenReturn(42L);
        org=mock(OrgService.class);mapper=mock(ComplaintMapper.class);bindings=mock(StoreUserMapper.class);service=new ComplaintServiceImpl();
        ReflectionTestUtils.setField(service,"orgService",org);ReflectionTestUtils.setField(service,"complaintMapper",mapper);
        ReflectionTestUtils.setField(service,"storeUserMapper",bindings);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"complaint-test"),ComplaintDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"store-user-test"),StoreUserDO.class);
        jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
        jdbc.execute("CREATE TABLE system_complaint(id BIGINT PRIMARY KEY,business_id BIGINT,store_id BIGINT,complaint_type INT,deleted BOOLEAN)");
        jdbc.execute("CREATE TABLE system_store_user(user_id BIGINT,business_id BIGINT,store_id BIGINT,type INT,deleted BOOLEAN)");
        // 本区域待处理2笔；已处理、其他区域、其他项目及软删除记录均不计入。
        jdbc.update("INSERT INTO system_complaint VALUES(1,10,101,0,FALSE),(2,10,102,0,FALSE),(3,10,101,1,FALSE),(4,10,999,0,FALSE),(5,11,101,0,FALSE),(6,10,101,0,TRUE)");
        when(mapper.selectCount(any())).thenAnswer(call->{
            AbstractWrapper<?,?,?> wrapper=call.getArgument(0);var expression=wrapper.getCustomSqlSegment();
            var matcher=Pattern.compile("#\\{ew\\.paramNameValuePairs\\.([^}]+)}").matcher(expression);
            var args=new ArrayList<Object>();var sql=new StringBuffer();
            while(matcher.find()){args.add(wrapper.getParamNameValuePairs().get(matcher.group(1)));matcher.appendReplacement(sql,"?");}
            matcher.appendTail(sql);return jdbc.queryForObject("SELECT COUNT(*) FROM system_complaint "+sql,Long.class,args.toArray());
        });
        when(bindings.selectList(any())).thenAnswer(call->{
            AbstractWrapper<?,?,?> wrapper=call.getArgument(0);var matcher=Pattern.compile("#\\{ew\\.paramNameValuePairs\\.([^}]+)}").matcher(wrapper.getCustomSqlSegment());
            var args=new ArrayList<Object>();var sql=new StringBuffer();
            while(matcher.find()){args.add(wrapper.getParamNameValuePairs().get(matcher.group(1)));matcher.appendReplacement(sql,"?");}
            matcher.appendTail(sql);return jdbc.queryForList("SELECT store_id FROM system_store_user "+sql,args.toArray()).stream().map(row->{
                var binding=new StoreUserDO();binding.setStoreId(((Number)row.get("STORE_ID")).longValue());return binding;
            }).toList();
        });
    }
    @AfterEach void cleanup(){login.close();BusinessContextHolder.setBusinessId(previousBusiness);}
    @Test void countsPendingAcrossAllAuthorizedDescendantStores(){
        when(org.getStoreIdsByUser()).thenReturn(Set.of(101L,102L));assertEquals(2L,service.getRegionPendingCount());
        verify(mapper,times(1)).selectCount(any());verify(mapper,never()).selectList(any());
    }
    @Test void emptyScopeReturnsZeroWithoutUnrestrictedQuery(){
        when(org.getStoreIdsByUser()).thenReturn(Set.of());assertEquals(0L,service.getRegionPendingCount());verifyNoInteractions(mapper);
    }
    @Test void nullAndInvalidScopeFailClosed(){
        when(org.getStoreIdsByUser()).thenReturn(null);assertEquals(0L,service.getRegionPendingCount());
        var scope=new HashSet<Long>();scope.add(null);scope.add(0L);scope.add(-1L);
        when(org.getStoreIdsByUser()).thenReturn(scope);assertEquals(0L,service.getRegionPendingCount());verifyNoInteractions(mapper);
    }
    @Test void newComplaintAndHandlingAreReflectedWithoutCache(){
        when(org.getStoreIdsByUser()).thenReturn(Set.of(101L,102L));assertEquals(2L,service.getRegionPendingCount());
        jdbc.update("INSERT INTO system_complaint VALUES(7,10,102,0,FALSE)");assertEquals(3L,service.getRegionPendingCount());
        jdbc.update("UPDATE system_complaint SET complaint_type=1 WHERE id=7");assertEquals(2L,service.getRegionPendingCount());
    }
    @Test void unauthenticatedCallsCannotLookUpScope(){
        login.when(WebFrameworkUtils::getLoginUserId).thenReturn(null);
        var error=assertThrows(ServiceException.class,()->service.getRegionPendingCount());assertEquals(401,error.getCode());verifyNoInteractions(org,mapper,bindings);
    }
    @Test void organizationAndManagerStoresAreUnionedAndDeduplicated(){
        when(org.getStoreIdsByUser()).thenReturn(Set.of(101L,102L));
        jdbc.update("INSERT INTO system_store_user VALUES(42,10,101,1,FALSE),(42,10,300,1,FALSE),(43,10,999,1,FALSE),(42,11,999,1,FALSE),(42,10,999,2,FALSE),(42,10,999,1,TRUE)");
        jdbc.update("INSERT INTO system_complaint VALUES(7,10,300,0,FALSE)");assertEquals(3L,service.getRegionPendingCount());
    }
    @Test void managerOnlyAccountCanCountWithoutAnyOrganization(){
        when(org.getStoreIdsByUser()).thenReturn(Set.of());jdbc.update("INSERT INTO system_store_user VALUES(42,10,101,1,FALSE)");
        assertEquals(1L,service.getRegionPendingCount());
    }
    @Test void controllerExposesNumericBadgeResponse() throws Exception {
        when(org.getStoreIdsByUser()).thenReturn(Set.of(101L,102L));var controller=new AppComplaintController();
        ReflectionTestUtils.setField(controller,"complaintService",service);
        MockMvcBuilders.standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter()).build()
                .perform(get("/system/complaint/getRegionPendingCount").param("orgId","999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data").value(2));
    }
}
