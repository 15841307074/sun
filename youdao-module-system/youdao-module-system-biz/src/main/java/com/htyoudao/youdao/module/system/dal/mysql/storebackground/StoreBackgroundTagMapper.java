package com.htyoudao.youdao.module.system.dal.mysql.storebackground;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTagDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店背景模板与标签关系数据访问接口。
 */
@Mapper
public interface StoreBackgroundTagMapper extends BaseMapperX<StoreBackgroundTagDO> {
}
