package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.StorePageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店聚合服务接口
 * 提供门店维度的数据分析能力，包括门店指标统计、门店排名、门店分页查询等功能
 */
public interface IStoreAggService {

    /**
     * 门店指标聚合查询
     * 查询指定时间范围内各门店的指标数据，支持同比分析
     *
     * @param requestVO 查询请求参数，包含时间段、门店筛选等条件
     * @return 包含当前和同比时间段各门店指标数据的响应对象
     */
    AnalysisVO<AnalysisStoreVO> query(AggregationRequestVO requestVO);

    /**
     * 门店总览折线图
     *
     * @param requestVO 查询请求参数
     * @return 门店总览图表结果
     */
    AnalysisChartVO generalChart(@Valid LineChartRequestVO requestVO);

    /**
     * 门店分页列表查询
     * 分页查询门店维度的聚合数据
     *
     * @param requestVO 查询请求参数，包含分页、排序等条件
     * @return 分页的门店聚合数据结果
     */
    PageResult<AnalysisStorePageVO> storePage(@Valid AggregationPageRequest requestVO);

    /**
     * 门店TopN排名查询
     * 查询指定指标排名前N的门点
     *
     * @param requestVO      查询请求参数
     * @param n              返回数量
     * @param metricsConfig  排名指标
     * @param orderType      排序方式（asc/desc）
     * @return TopN门店排名结果
     */
    PageResult<AnalysisStorePageVO> storeTopN(AggregationRequestVO requestVO, Integer n, MetricsConfig metricsConfig, String orderType);

    /**
     * 门店TopN排名查询（多指标）
     * 查询指定多个指标排名前N的门点
     *
     * @param requestVO      查询请求参数
     * @param n              返回数量
     * @param metricsConfig  主要排名指标
     * @param orderType      排序方式
     * @param metrics        其他指标列表
     * @return TopN门店排名结果（含多指标）
     */
    PageResult<AnalysisStorePageVO> storeTopN(AggregationRequestVO requestVO, Integer n, MetricsConfig metricsConfig, String orderType, List<MetricsConfig> metrics);

    /**
     * 城市分页列表查询
     * 分页查询城市维度的聚合数据
     *
     * @param requestVO 查询请求参数
     * @return 分页的城市聚合数据结果
     */
    PageResult<AnalysisStorePageVO> cityPage(AggregationPageRequest requestVO);

    /**
     * 城市TopN排名查询
     * 查询指定指标排名前N的城市
     *
     * @param requestVO      查询请求参数
     * @param n              返回数量
     * @param metricsConfig  排名指标
     * @param orderType      排序方式
     * @return TopN城市排名结果
     */
    PageResult<AnalysisStorePageVO> cityTopN(AggregationRequestVO requestVO, Integer n, MetricsConfig metricsConfig, String orderType);

    /**
     * 城市TopN排名查询（多指标）
     * 查询指定多个指标排名前N的城市
     *
     * @param requestVO      查询请求参数
     * @param n              返回数量
     * @param metricsConfig  主要排名指标
     * @param orderType      排序方式
     * @param metrics        其他指标列表
     * @return TopN城市排名结果（含多指标）
     */
    PageResult<AnalysisStorePageVO> cityTopN(AggregationRequestVO requestVO, Integer n, MetricsConfig metricsConfig, String orderType, List<MetricsConfig> metrics);

    /**
     * 组织分页列表查询
     * 分页查询组织（部门）维度的门店聚合数据
     *
     * @param requestVO 查询请求参数
     * @return 分页的组织维度门店数据结果
     */
    PageResult<AnalysisStorePageVO> orgPage(@Valid StorePageRequest requestVO);

    /**
     * 获取门店总营业额
     * 查询指定条件下所有门店的订单总金额
     *
     * @param aggregationRequest 查询请求参数
     * @return 门店订单总金额
     */
    BigDecimal storeTotalAmount(InventoryAggregationRequest aggregationRequest);
}
