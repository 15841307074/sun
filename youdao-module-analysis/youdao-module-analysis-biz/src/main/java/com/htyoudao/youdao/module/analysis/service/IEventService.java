package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.dal.es.BaseEvent;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件服务接口
 * 提供用户事件（到店、下单等）的数据统计能力，用于UV/PV等分析指标的计算
 */
public interface IEventService {
    
    /**
     * 添加事件记录
     * 用于记录用户到店、点击等事件
     *
     * @param baseEvent 事件数据对象
     */
    void add(BaseEvent baseEvent);

    /**
     * 查询UV（独立访客数）
     * 统计指定条件下的独立访客数量
     *
     * @param queryDTO 查询条件，包含时间范围、门店、事件类型等
     * @return 独立访客数量
     */
    Long queryUV(EventQueryDTO queryDTO);

    /**
     * 查询PV（页面访问量）
     * 统计指定条件下的页面访问次数
     *
     * @param queryDTO 查询条件
     * @return 页面访问次数
     */
    Long queryPV(EventQueryDTO queryDTO);

    /**
     * 按门店批量查询 PV
     *
     * @param queryDTO 查询条件
     * @return storeId -> pv
     */
    Map<Long, Long> queryPVByStore(EventQueryDTO queryDTO);
    
    /**
     * 按天查询PV
     * 统计指定时间范围内每天的页面访问量
     *
     * @param queryDTO 查询条件
     * @return 日期到PV的映射
     */
    Map<String, Long> queryPVByDay(EventQueryDTO queryDTO);
    
    /**
     * 按天查询UV
     * 统计指定时间范围内每天的独立访客数
     *
     * @param queryDTO 查询条件
     * @return 日期到UV的映射
     */
    Map<String, Long> queryUVByDay(EventQueryDTO queryDTO);
    
    /**
     * 批量查询事件UV
     * 一次性查询多个事件的独立访客数
     *
     * @param query多个eventId
DTO 查询条件，包含     * @return eventId到UV的映射
     */
    Map<String, Long> batchQueryUV(EventQueryDTO queryDTO);

    /**
     * 按门店和事件ID批量查询UV
     *
     * @param queryDTO 查询条件
     * @return storeId -> eventId -> uv
     */
    Map<Long, Map<String, Long>> batchQueryUVByStoreAndEvent(EventQueryDTO queryDTO);
}
