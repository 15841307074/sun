package com.htyoudao.youdao.module.system.dal.mysql.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店用户关系表 Mapper
 *
 * @author ssz
 */
@Mapper
public interface SystemStoreUserMapper extends BaseMapperX<SystemStoreUserDO> {

    default List<Long> getStoreIdsByUserId(Long userId){
        LambdaQueryWrapper<SystemStoreUserDO>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemStoreUserDO::getUserId, userId);
        queryWrapper.eq(SystemStoreUserDO::getType, 1);
        return selectList(queryWrapper).stream().map(SystemStoreUserDO::getStoreId).toList();
    }
}