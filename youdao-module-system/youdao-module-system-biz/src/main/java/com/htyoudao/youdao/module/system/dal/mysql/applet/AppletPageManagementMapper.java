package com.htyoudao.youdao.module.system.dal.mysql.applet;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppletPageManagementMapper extends BaseMapperX<AppletPageManagementDO> {
    List<Long> selectAllStorePageIds();
}
