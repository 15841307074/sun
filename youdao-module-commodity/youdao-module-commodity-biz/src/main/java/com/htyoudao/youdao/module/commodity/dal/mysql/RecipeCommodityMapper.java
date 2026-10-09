package com.htyoudao.youdao.module.commodity.dal.mysql;


import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeCommodityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 *
 * @author lbw
 * @date 2025-08-23
 */
@Mapper
public interface RecipeCommodityMapper extends BaseMapperX<RecipeCommodityDO> {
}
