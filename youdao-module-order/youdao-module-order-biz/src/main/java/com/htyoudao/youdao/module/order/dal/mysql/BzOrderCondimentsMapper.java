package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderCondimentsDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BzOrderCondimentsMapper extends BaseMapperX<BzOrderCondimentsDO> {

    @DS(DsNameConstants.MASTER)
    List<BzOrderCondimentsDO> selectSimulationList(@Param("tableFix") String tableFix,
                                                   @Param("orderSn") String orderSn);

    default List<BzOrderCondimentsDO> selectList(List<String> orderSns, LocalDateTime[] createTimes) {
        LambdaQueryWrapperX<BzOrderCondimentsDO> query = new LambdaQueryWrapperX<BzOrderCondimentsDO>()
                .inIfPresent(BzOrderCondimentsDO::getOrderSn, orderSns)
                .betweenIfPresent(BzOrderCondimentsDO::getCreateTime, createTimes);
        return selectList(query);
    }
}
