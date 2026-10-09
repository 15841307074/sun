package com.htyoudao.youdao.module.order.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzSplicingOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-12-24
 */
@Mapper
public interface BzSplicingOrderMapper extends BaseMapperX<BzSplicingOrderDO> {
}
