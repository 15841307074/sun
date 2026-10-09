package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreStatusLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店经营状态记录表 Mapper
 *
 * @author ssz
 */
@Mapper
public interface SystemStoreStatusLogMapper extends BaseMapperX<SystemStoreStatusLogDO> {
}