package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.RangeTimeRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationCurrentRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.GeneralRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.general.AnalysisEntryVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.OrderAmountRangeVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

/**
 * 订单聚合服务接口
 * 提供订单相关数据的聚合分析能力，包括订单金额、订单数量、时间分布等指标统计
 */
public interface IOrderAggregationService {

    /**
     * 订单指标聚合查询
     * 查询指定时间范围内的订单各项指标数据，支持同比分析
     *
     * @param requestVO 查询请求参数，包含时间段、门店、订单来源等筛选条件
     * @return 包含当前时间段和同比时间段各指标值的响应对象
     */
    AnalysisVO<AnalysisOrderVO> query(AggregationRequestVO requestVO);

    /**
     * 订单金额区间分布查询
     * 虽然名称是range，但实际是指标查询，统计各金额区间的订单分布
     *
     * @param requestVO 查询请求参数
     * @return 订单金额区间分布结果
     */
    OrderAmountRangeVO orderAmountRange(@Valid AggregationCurrentRequestVO requestVO);

    /**
     * 订单创建时间段分布查询
     * 按时间区间进行订单数据聚合，统计各时间段的订单分布
     *
     * @param requestVO      查询参数，包含时间范围等筛选条件
     * @param metricsConfig  聚合指标配置
     * @return 各时间段的订单数据分布结果
     */
    AnalysisRatioResult getOrderTimeRangeVO(AggregationRequestVO requestVO, MetricsConfig metricsConfig);

    /**
     * 订单实付金额区间分布查询
     * 按实付金额区间进行订单数据聚合，统计各金额区间的订单分布
     *
     * @param requestVO      查询参数，包含时间范围等筛选条件
     * @param metricsConfig  聚合指标配置
     * @return 各实付金额区间的订单分布结果
     */
    AnalysisRatioResult orderPayRange(@Valid AggregationRequestVO requestVO, MetricsConfig metricsConfig);

    /**
     * 订单时间区间分布查询
     * 综合时间维度进行订单数据聚合分析
     *
     * @param requestVO 查询参数
     * @return 时间区间分布结果
     */
    AnalysisRatioResult orderRangeTime(@Valid RangeTimeRequestVO requestVO);


    /**
     * 总览指标查询
     * 获取订单相关的基础指标总览数据
     *
     * @param requestVO 查询请求参数
     * @return 包含各项基础指标的响应对象
     */
    AnalysisVO<AnalysisEntryVO> generalView(@Valid GeneralRequestVO requestVO);


}
