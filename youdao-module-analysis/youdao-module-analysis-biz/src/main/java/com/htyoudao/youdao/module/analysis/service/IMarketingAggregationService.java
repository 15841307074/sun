package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MarketingRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingChannelDataVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingShopDataVO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 营销活动聚合服务接口
 * 提供营销活动相关的数据分析能力，包括活动概览、门店数据、渠道数据等维度的统计分析
 */
public interface IMarketingAggregationService {

    /**
     * 获取营销活动概览数据
     * 统计指定时间范围内的营销活动核心指标，包括订单数、优惠金额、参与店铺数等
     *
     * @param request 筛选条件，包含时间范围、活动类型、渠道等筛选条件
     * @return 营销活动概览数据，包含各项核心指标
     */
    MarketingOverviewVO getMarketingOverview(MarketingRequest request);


    /**
     * 获取营销活动门店数据底表
     * 分页查询各门店的营销活动数据明细
     *
     * @param request 筛选条件，包含时间范围、门店、分页等条件
     * @return 门店营销数据分页结果
     */
    PageResult<MarketingShopDataVO> getMarketingShopData(MarketingRequest request);

    /**
     * 获取营销活动渠道数据
     * 查询各渠道（微信、支付宝、点餐等）的营销活动数据统计
     *
     * @param request 筛选条件
     * @return 各渠道的营销数据列表
     */
    List<MarketingChannelDataVO> getMarketingChannelData(@Valid MarketingRequest request);


}
