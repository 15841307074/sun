package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * 门店标签关系表 Mapper
 *
 * @author ssz
 */
@Mapper
public interface SystemStoreTagMapper extends BaseMapperX<SystemStoreTagDO> {

    @Options(fetchSize = Integer.MIN_VALUE)
    @ResultType(Long.class)
    @Select("SELECT store_id FROM system_store_tag ${ew.customSqlSegment}")
    void selectStoreIdByTag(@Param("ew") LambdaQueryWrapper<?> wrapper, ResultHandler<Long> handler);

    List<Long> selectStoreIdsByTagIds(List<Long> tagIds);
}