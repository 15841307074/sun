package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdPositionStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdTrendPointVO;
import com.htyoudao.youdao.module.analysis.dal.es.AdEvent;
import com.htyoudao.youdao.module.analysis.service.dto.AdEventQueryDTO;
import java.util.List;

/**
 * 广告事件服务接口
 * 提供广告埋点（曝光、点击、离开）数据的写入与看板统计能力，用于曝光/点击/CTR/停留时长等分析指标的计算
 */
public interface IAdEventService {

    /**
     * 添加广告事件记录
     * 用于记录广告曝光、点击、离开等事件
     *
     * @param adEvent 广告事件数据对象
     */
    void add(AdEvent adEvent);

    /**
     * 批量添加广告事件记录
     * 适用于批量曝光场景，一次性写入多条事件，减少重复的索引名计算和格式化开销
     *
     * @param events 广告事件列表
     */
    void addAll(List<AdEvent> events);

    /**
     * 查询广告概览数据
     * 一次ES请求聚合出曝光PV、曝光UV、点击PV、平均停留时长、点击率CTR
     *
     * @param queryDTO 查询条件，包含时间范围、广告、广告位、门店、渠道等
     * @return 广告概览数据
     */
    AdOverviewVO queryOverview(AdEventQueryDTO queryDTO);

    /**
     * 按天查询广告趋势
     * 统计指定时间范围内每天的曝光PV、曝光UV、点击PV、点击率CTR
     *
     * @param queryDTO 查询条件
     * @return 每日趋势点列表（按日期升序）
     */
    List<AdTrendPointVO> queryTrendByDay(AdEventQueryDTO queryDTO);

    /**
     * 按广告位分组查询
     * 统计每个广告位的曝光PV、曝光UV、点击PV、点击率CTR
     *
     * @param queryDTO 查询条件
     * @return 各广告位统计数据列表
     */
    List<AdPositionStatVO> queryByPosition(AdEventQueryDTO queryDTO);

    /**
     * 按广告分组查询
     * 统计每个广告的曝光PV、曝光UV、点击PV、点击率CTR，并附带广告名称与广告位
     *
     * @param queryDTO 查询条件
     * @return 各广告统计数据列表
     */
    List<AdStatVO> queryByAd(AdEventQueryDTO queryDTO);
}
