package com.htyoudao.youdao.module.order.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.order.controller.app.report.VO.ReportSelectRespVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.ReportStasticsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportStasticsMapper extends BaseMapperX<ReportStasticsDO> {

    @Select({
            "SELECT",
            "    MIN(id) AS k,",
            "    store_name AS v",
            "FROM report_stastics",
            "WHERE deleted = 0",
            "  AND store_name IS NOT NULL",
            "  AND store_name != ''",
            "GROUP BY store_name",
            "ORDER BY store_name"
    })
    List<ReportSelectRespVO> selectStoreList();
}
