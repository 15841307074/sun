package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportAppSalesnumDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportTotalDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportAppSalesnumMapper extends BaseMapper<ReportAppSalesnumDO> {
}