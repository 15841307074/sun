package com.htyoudao.youdao.module.system.dal.mysql.storebackground;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundStoreDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店背景模板与门店关系数据访问接口。
 */
@Mapper
public interface StoreBackgroundStoreMapper extends BaseMapperX<StoreBackgroundStoreDO> {
}
