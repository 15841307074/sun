package com.htyoudao.youdao.module.system.service.appversion;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.system.controller.admin.appversion.vo.AppVersionConfigUpdateReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.appversion.AppVersionConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.appversion.AppVersionConfigMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppVersionConfigServiceTest {
    private final AppVersionConfigMapper mapper = mock(AppVersionConfigMapper.class);
    private final AppVersionConfigService service = new AppVersionConfigService(mapper);

    @BeforeAll
    static void tableMapping() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                AppVersionConfigDO.class);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listRestrictsProjectsAndMapsLegacyDeleteColumn() {
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        assertTrue(service.list().isEmpty());
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectList(captor.capture());
        LambdaQueryWrapper<?> query = (LambdaQueryWrapper<?>) captor.getValue();
        assertTrue(query.getSqlSegment().contains("project_code IN"));
        assertTrue(query.getSqlSegment().contains("id DESC"));
        assertEquals(6, query.getParamNameValuePairs().size());
        assertTrue(query.getParamNameValuePairs().containsValue("com.boos.saas.old"));
        assertEquals("is_delete", TableInfoHelper.getTableInfo(AppVersionConfigDO.class)
                .getLogicDeleteFieldInfo().getColumn());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void updateIsPartialAndRetainsScopeAndAuditFields() {
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        AppVersionConfigUpdateReqVO req = new AppVersionConfigUpdateReqVO();
        req.setId(7L);
        req.setVersionCode("2.0.0");
        service.update(req);
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(isNull(), captor.capture());
        LambdaUpdateWrapper<?> update = (LambdaUpdateWrapper<?>) captor.getValue();
        assertTrue(update.getSqlSegment().contains("project_code IN"));
        assertTrue(update.getSqlSegment().contains("id ="));
        assertTrue(update.getSqlSet().contains("version_code="));
        assertTrue(update.getSqlSet().contains("update_time="));
        assertFalse(update.getSqlSet().contains("file_path="));
        assertFalse(update.getSqlSet().contains("project_owner_ship="));
        assertFalse(update.getSqlSet().contains("is_delete="));
    }

    @Test
    void unmatchedRecordFailsInsteadOfReportingSuccess() {
        AppVersionConfigUpdateReqVO req = new AppVersionConfigUpdateReqVO();
        req.setId(999L);
        assertThrows(ServiceException.class, () -> service.update(req));
    }
}
