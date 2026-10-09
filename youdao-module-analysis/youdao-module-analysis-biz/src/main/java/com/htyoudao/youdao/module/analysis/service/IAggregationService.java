package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据分析聚合服务接口
 * 提供订单、商品、门店等数据的聚合查询能力，支持多指标统计、同比分析等功能
 */
public interface IAggregationService {

    /**
     * 多指标聚合查询
     * 用于查询当前时间段和同比时间段的各项指标数据
     *
     * @param requestVO 查询请求参数，包含时间段、门店、订单来源等筛选条件
     * @param metrics   指标配置列表，指定需要查询的指标类型
     * @param uv        是否查询UV（独立访客数）
     * @return 包含当前时间段和同比时间段各指标值的响应对象
     */
    AnalysisVO<Map<String,Double>> query(AggregationRequestVO requestVO, List<MetricsConfig> metrics, boolean uv);

    /**
     * 折线图数据查询
     * 按时间粒度查询指标数据，用于绘制趋势折线图
     *
     * @param requestVO 查询请求参数，包含指标code、查询类型（天/小时）、时间段等
     * @return 包含时间点、当期值、环比值的图表数据
     */
    AnalysisChartVO lineChart(@Valid LineChartRequestVO requestVO);

    /**
     * 分组聚合分页查询
     * 按指定字段（如门店ID）进行分组聚合，支持分页和排序
     *
     * @param requestVO  查询请求参数
     * @param groupField 分组字段，如门店ID、商品ID等
     * @param metrics    指标配置列表
     * @param showUv     是否返回UV数据
     * @return 分页结果，每页包含分组后的指标数据
     */
    PageResult<AnalysisStorePageVO> groupPage(AggregationPageRequest requestVO, String groupField, List<MetricsConfig> metrics, Boolean showUv);

    /**
     * 活动数据分组聚合分页查询
     * 专门用于查询活动相关数据的分组聚合
     *
     * @param requestVO 查询请求参数，包含活动ID等筛选条件
     * @param storeId   门店ID
     * @param metrics   指标配置列表
     * @param b         是否显示UV
     * @return 活动数据的分页结果
     */
    PageResult<ActivityNjnzStorePageVO> activityGroupPage(ActicitytyNjnzPageRequestVO requestVO, String storeId, List<MetricsConfig> metrics, Boolean b);

    /**
     * 活动数据查询
     * 查询活动相关的统计数据，用于导出等场景
     *
     * @param requestVO 查询请求参数，包含活动ID等筛选条件
     * @param storeId   门店ID
     * @param metrics   指标配置列表
     * @param b         是否显示UV
     * @return 活动数据统计结果
     */
    Boolean activityData(ActicitytyNjnzPageRequestVO requestVO, String storeId, List<MetricsConfig> metrics, Boolean b);

    /**
     * 查询库存相关的总金额
     * 用于库存分析模块，查询特定条件下的订单总金额
     *
     * @param aggregationRequest 查询请求参数
     * @param metrics            指标配置列表
     * @return 订单总金额
     */
    BigDecimal selectTotalAmount(InventoryAggregationRequest aggregationRequest, List<MetricsConfig> metrics);
}
