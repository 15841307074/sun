package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityMzStorePageRespVO;

import java.util.List;
import java.util.Map;

/**
 * 满赠活动数据分析聚合服务接口
 */
public interface IActivityMzAggregationService {

    /**
     * 门店下数据分析分页（含赠送商品数量）
     */
    PageResult<ActivityMzStorePageRespVO> mzStorePage(ActicitytyNjnzPageRequestVO requestVO);

    /**
     * 数据分析导出（含赠送商品数量）
     */
    Boolean mzExportData(ActicitytyNjnzPageRequestVO requestVO);

    /**
     * 从 bz_order_product 索引查询各门店的赠送商品数量
     *
     * @param activityId 活动ID
     * @param storeIds   门店ID列表
     * @return storeId -> 赠送商品数量
     */
    Map<Long, Long> queryGiftCommodityCount(Long activityId, List<Long> storeIds);
}
