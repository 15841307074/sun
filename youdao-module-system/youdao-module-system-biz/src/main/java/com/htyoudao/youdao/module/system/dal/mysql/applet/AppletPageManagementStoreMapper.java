package com.htyoudao.youdao.module.system.dal.mysql.applet;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementStoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppletPageManagementStoreMapper extends BaseMapperX<AppletPageManagementStoreDO> {
    List<Long> selectPageIdsByStoreIds(List<Long> storeIds);
}
