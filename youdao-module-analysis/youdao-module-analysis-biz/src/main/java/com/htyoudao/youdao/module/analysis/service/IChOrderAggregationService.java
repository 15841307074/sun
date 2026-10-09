package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;

/**
 * 订单指标聚合查询 —— ClickHouse 数据源
 */
public interface IChOrderAggregationService {

    /**
     * 从 ClickHouse 查询订单指标（与 orderView 功能一致，数据来源改为 CH）
     */
    AnalysisVO<AnalysisOrderVO> queryFromCh(AggregationRequestVO requestVO);
}
