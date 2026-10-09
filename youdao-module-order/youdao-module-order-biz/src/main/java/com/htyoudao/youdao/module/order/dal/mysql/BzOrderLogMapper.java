package com.htyoudao.youdao.module.order.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderLogDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BzOrderLogMapper extends BaseMapperX<BzOrderLogDO> {

    default List<BzOrderLogDO> selectList(List<String> orderSns, LocalDateTime[] createTimes) {
        LambdaQueryWrapperX<BzOrderLogDO> query = new LambdaQueryWrapperX<BzOrderLogDO>()
                .inIfPresent(BzOrderLogDO::getOrderSn, orderSns)
                .betweenIfPresent(BzOrderLogDO::getCreateTime, createTimes);
        return selectList(query);
    }
}