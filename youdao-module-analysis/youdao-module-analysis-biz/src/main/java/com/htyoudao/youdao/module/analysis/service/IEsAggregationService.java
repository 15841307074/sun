package com.htyoudao.youdao.module.analysis.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRecordsVO;
import com.htyoudao.youdao.module.analysis.service.dto.AggOrgDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 聚合服务接口
 * 提供基于 Elasticsearch 的数据聚合查询能力，支持多指标、分组、时间维度等多种聚合方式
 */
public interface IEsAggregationService {


    /**
     * 多指标聚合查询
     * 根据查询条件同时计算多个指标的值
     *
     * @param esAggDTO 查询参数，包含时间范围、门店筛选、订单来源等条件
     * @param metrics  指标配置列表，指定需要计算的指标类型
     * @return Map，key为指标code，value为指标计算结果
     */
    Map<String, Double> batchAggregateMetrics(EsAggDTO esAggDTO, List<MetricsConfig> metrics);


    /**
     * 多指标聚合查询（含UV）
     * 在多指标查询基础上额外计算UV（独立访客数）
     *
     * @param esAggDTO 查询参数，包含时间范围、门店筛选、订单来源等条件
     * @param metrics  指标配置列表，指定需要计算的指标类型
     * @return Map，key为指标code，value为指标计算结果（包含UV指标）
     */
    Map<String, Double> batchAggregateMetricsWithUV(EsAggDTO esAggDTO, List<MetricsConfig> metrics);


    /**
     * 按时间粒度聚合查询
     * 根据指定的时间粒度（天/小时）进行指标聚合，用于趋势分析
     *
     * @param request 查询参数，包含时间范围、门店筛选等条件
     * @param type    时间粒度类型：天(DAY)或小时(HOUR)
     * @param metric  指标配置，指定需要计算的指标类型
     * @return Map，key为时间点，value为该时间点的指标值
     */
    Map<String, Double> analyzeByTimeGranularity(EsAggDTO request, EsDateFormat type, MetricsConfig metric);

    /**
     * 分组分页聚合查询
     * 按指定字段进行分组，支持分页和自定义排序
     *
     * @param request      查询条件，包含时间范围、门店筛选等
     * @param metrics      指标配置列表
     * @param groupField   分组字段，如门店ID、商品ID等
     * @param orderMetric  排序指标，为空则不排序
     * @param order        排序方式（升序/降序）
     * @param pageNum      分页页码，从1开始
     * @param pageSize     每页记录数
     * @return 分页结果，包含分组后的各组指标数据
     */
    PageResult<AnalysisTopVO> paginatedGroupAggregation(
        EsAggDTO request,
        List<MetricsConfig> metrics,
        String groupField,
        MetricsConfig orderMetric,
        SortOrder order,
        Integer pageNum,
        Integer pageSize
        );


    /**
     * 下单频次聚合查询
     * 统计不同下单频次的用户或门店数量分布
     *
     * @param dto        查询参数
     * @param groupField 分组字段，通常为用户ID或门店ID
     * @return Map，key为分组ID，value为下单频次
     */
    Map<Long, Double> frequencyAggGroup(EsAggDTO dto, MetricsConfig groupField);

    /**
     * 范围聚合查询
     * 根据数值范围进行分组聚合，如按订单金额区间统计
     *
     * @param request     查询条件
     * @param rangeField  用于范围划分的字段，如订单金额
     * @param groupField  分组字段
     * @param list        范围条件列表，定义各范围区间
     * @return Map，key为范围区间标识，value为该范围内的聚合值
     */
    Map<String, Double> rangeList(EsAggDTO request,
        String rangeField,
        MetricsConfig groupField,
        List<RangeDTO> list
    );


    /**
     * 自定义范围聚合 + 多指标统计 + 分页支持
     * 支持自定义分组范围，同时计算多个业务指标
     *
     * @param dto          查询参数，包括时间、门店、来源等筛选条件
     * @param customGroups 自定义分组列表，每个分组包含一组groupField的值
     * @param metrics      多个业务指标配置
     * @param groupField  分组字段，比如 userId 或 storeId
     * @param orderMetric 排序指标
     * @param order       排序方式
     * @param pageNum     当前页码
     * @param pageSize    每页数量
     * @return 分页结果，每组的指标值集合
     */
    PageResult<AnalysisTopVO> paginatedRangeAggregation(
        EsAggDTO dto,
        List<AggOrgDTO> customGroups,
        List<MetricsConfig> metrics,
        String groupField,
        MetricsConfig orderMetric,
        SortOrder order,
        int pageNum,
        int pageSize
    );


    /**
     * 简化版分组聚合查询
     * 不支持排序的分组聚合查询
     *
     * @param request    查询条件
     * @param metrics    指标配置列表
     * @param groupField 分组字段
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    PageResult<AnalysisTopVO> paginatedGroupAggregation(
            EsAggDTO request,
            List<MetricsConfig> metrics,
            String groupField,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 秒杀活动会员分组聚合查询
     * 专门用于查询秒杀活动的会员相关数据
     *
     * @param request  查询条件
     * @param pageNum 页码
     * @param metrics 指标配置列表
     * @param pageSize 每页大小
     * @return 秒杀会员分页数据
     */
    PageResult<ActivitySeckillMemberVO> paginatedGroupAggregation(
            EsAggDTO request,
            Integer pageNum,
            List<MetricsConfig> metrics,
            Integer pageSize
    );

    /**
     * 秒杀分组聚合查询
     * 用于秒杀活动的订单数据聚合分析
     *
     * @param request    查询条件
     * @param metrics    指标配置列表
     * @param groupField 分组字段
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param type       秒杀类型
     * @return 分页结果
     */
    PageResult<AnalysisTopVO> paginatedGrouSeckillpAggregation(
            EsAggDTO request,
            List<MetricsConfig> metrics,
            String groupField,
            Integer pageNum,
            Integer pageSize,
            int  type
    );

    /**
     * 分组聚合列表查询（不分页）
     * 返回所有分组的结果列表
     *
     * @param currentRequest 查询条件
     * @param metrics        指标配置列表
     * @param groupField     分组字段
     * @return 所有分组的指标数据列表
     */
    List<AnalysisTopVO> paginatedGroupAggregationList(EsAggDTO currentRequest, List<MetricsConfig> metrics, String groupField);

    /**
     * 秒杀分组聚合列表查询（不分页）
     * 返回秒杀活动的所有分组结果
     *
     * @param currentRequest 查询条件
     * @param metrics        指标配置列表
     * @param groupField     分组字段
     * @return 秒杀分组指标数据列表
     */
    List<AnalysisTopVO> paginatedSeckillGroupAggregationList(EsAggDTO currentRequest, List<MetricsConfig> metrics, String groupField);

}
