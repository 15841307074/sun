package com.htyoudao.youdao.module.order.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.order.dal.DTO.OrderReportDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportTotalDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportTotalMapper extends BaseMapper<ReportTotalDO> {
}