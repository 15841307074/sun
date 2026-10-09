package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SysStoreExtendDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 店铺关系表 Mapper 接口
 * </p>
 *
 * @author dingyunfei
 * @since 2024-05-14
 */
@Mapper
public interface SysStoreExtendMapper extends BaseMapperX<SysStoreExtendDO> {
    void matchingField();
}
