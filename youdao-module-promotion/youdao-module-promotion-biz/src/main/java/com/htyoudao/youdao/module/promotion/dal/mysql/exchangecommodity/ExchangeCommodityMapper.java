package com.htyoudao.youdao.module.promotion.dal.mysql.exchangecommodity;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangecommodity.ExchangeCommodityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 兑换券下单时必选商品 Mapper
 *
 * @author lzw
 */
@Mapper
public interface ExchangeCommodityMapper extends BaseMapperX<ExchangeCommodityDO> {

}