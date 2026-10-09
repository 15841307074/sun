package com.htyoudao.youdao.module.promotion.service.activityCq;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface ActivityCqService extends IService<ActivityCqDO> {

    /**
     * 创建抽签活动
     */
    CommonResult<Integer> createActivityCq(@Valid ActivityCqSaveOrUpdateReqVO reqVO);

    /**
     * 修改抽签活动
     */
    CommonResult<Integer> updateActivityCq(@Valid ActivityCqSaveOrUpdateReqVO reqVO);

    /**
     * 删除抽签活动
     */
    CommonResult<Integer> deleteActivityCq(Long id);

    /**
     * 查询抽签活动详情
     */
    ActivityCqDetailRespVO selectDetail(Long id);

    /**
     * 复制抽签活动
     */
    CommonResult<Integer> copyActivityCq(ActivityCqSaveOrUpdateReqVO reqVO);

    /**
     * 获取抽签活动概览数据
     */
    Map<String, Object> getActivityCqAnalysis(String id);

    /**
     * 获取抽签活动按天统计数据
     */
    Map<String, Object> getActivityCqDailyAnalysis(ActivityCqLogEventReqVO reqVO);

    /**
     * 分页查询抽签记录
     */
    PageResult<ActivityCqLogRespVO> getActivityCqLogList(ActivityCqLogPageReqVO reqVO);

    /**
     * 统计抽签参与人数和参与次数
     */
    ActivityCqStatisticsRespVO getActivityCqLogCount(ActivityCqLogPageReqVO reqVO);

    /**
     * 分页查询中签记录
     */
    PageResult<ActivityCqLogRespVO> getWinningLogList(ActivityCqLogPageReqVO reqVO);

    /**
     * 统计中签人数和中签次数
     */
    ActivityCqStatisticsRespVO getWinningCount(ActivityCqLogPageReqVO reqVO);

    /**
     * 导出抽签记录
     */
    void exportActivityCqLog(ActivityCqLogExportReqVO reqVO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 导出中签记录
     */
    void exportWinningLog(ActivityCqLogExportReqVO reqVO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 更新发货信息
     */
    Integer updateExpress(ActivityCqUpdateExpressReqVO reqVO);

    /**
     * 查询分享配置
     */
    ActivityCqSpreadRespVO selectSpread(Long id);

    /**
     * 修改分享配置
     */
    void updateSpread(ActivityCqSpreadSaveReqVO reqVO);

    /**
     * 获取抽签活动列表
     */
    List<ActivityCqReqVO> getActivityCqList();

    /**
     * 更新活动启用状态
     */
    Integer updateState(ActivityCqStateReqVO reqVO);
}
