package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BzOrderPurchaseMapper extends BaseMapperX<BzOrderPurchaseDO> {

    @DS(DsNameConstants.MASTER)
    List<BzOrderPurchaseDO> selectSimulationList(@Param("tableFix") String tableFix,
                                                 @Param("orderSn") String orderSn);

    default List<BzOrderPurchaseDO> selectList(List<String> orderSns, LocalDateTime[] createTimes) {
        LambdaQueryWrapperX<BzOrderPurchaseDO> query = new LambdaQueryWrapperX<BzOrderPurchaseDO>()
                .inIfPresent(BzOrderPurchaseDO::getOrderSn, orderSns)
                .betweenIfPresent(BzOrderPurchaseDO::getCreateTime, createTimes);
        return selectList(query);
    }
}
