package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonRequest;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonResult;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BzOrderProductSonMapper extends BaseMapperX<BzOrderProductSonDO> {

    @DS(DsNameConstants.MASTER)
    List<BzOrderProductSonDO> selectSimulationList(@Param("tableFix") String tableFix,
                                                   @Param("orderSn") String orderSn);

    default List<BzOrderProductSonDO> selectList(List<Long> productIds, LocalDateTime[] createTimes) {
        LambdaQueryWrapperX<BzOrderProductSonDO> query = new LambdaQueryWrapperX<BzOrderProductSonDO>()
                .inIfPresent(BzOrderProductSonDO::getParentGoodsId, productIds)
                .betweenIfPresent(BzOrderProductSonDO::getCreateTime, createTimes);
        return selectList(query);
    }


    List<ProductSonResult> productSonList(@Param("tableFixs") List<String> tableFixs, @Param("param") ProductSonRequest singleRequest);

}
