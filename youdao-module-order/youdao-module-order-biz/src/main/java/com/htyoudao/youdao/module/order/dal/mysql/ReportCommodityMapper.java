package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportCommodityDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportCommodityMapper extends BaseMapperX<ReportCommodityDO> {
}