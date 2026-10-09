package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BzOrderProductMapper extends BaseMapperX<BzOrderProductDO> {

    @DS(DsNameConstants.MASTER)
    List<BzOrderProductDO> selectSimulationList(@Param("tableFix") String tableFix,
                                                @Param("orderSn") String orderSn);

    default List<BzOrderProductDO> selectList(List<String> orderSns, LocalDateTime[] createTimes) {
        LambdaQueryWrapperX<BzOrderProductDO> query = new LambdaQueryWrapperX<BzOrderProductDO>()
                .inIfPresent(BzOrderProductDO::getOrderSn, orderSns)
                .betweenIfPresent(BzOrderProductDO::getCreateTime, createTimes);
        return selectList(query);
    }
}
