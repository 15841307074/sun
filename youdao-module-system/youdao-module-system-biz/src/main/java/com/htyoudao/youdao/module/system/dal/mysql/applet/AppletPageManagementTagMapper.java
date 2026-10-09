package com.htyoudao.youdao.module.system.dal.mysql.applet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementTagDO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

@Mapper
public interface AppletPageManagementTagMapper extends BaseMapperX<AppletPageManagementTagDO> {

    @Options(fetchSize = Integer.MIN_VALUE)
    @ResultType(Long.class)
    @Select("SELECT applet_page_id FROM applet_page_management_tag ${ew.customSqlSegment}")
    void selectAppletPageIdStream(@Param("ew") LambdaQueryWrapper<?> wrapper, ResultHandler<Long> handler);

    List<Long> selectPageIdsByStoreIds(List<Long> storeIds);
}
