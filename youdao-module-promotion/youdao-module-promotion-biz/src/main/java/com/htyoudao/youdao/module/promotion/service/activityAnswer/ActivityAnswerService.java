package com.htyoudao.youdao.module.promotion.service.activityAnswer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerDO;
import jakarta.validation.Valid;

import java.util.Map;

public interface ActivityAnswerService {

    /**
     * 创建有奖问答活动。
     */
    CommonResult<Integer> createActivityAnswer(@Valid ActivityAnswerSaveOrUpdateReqVO reqVO);

    /**
     * 修改有奖问答活动。
     */
    CommonResult<Integer> updateActivityAnswer(@Valid ActivityAnswerSaveOrUpdateReqVO reqVO);

    /**
     * 删除有奖问答活动。
     */
    CommonResult<Integer> deleteActivityAnswer(Long id);

    /**
     * 复制有奖问答活动。
     */
    CommonResult<Integer> copyActivityAnswer(@Valid ActivityAnswerSaveOrUpdateReqVO reqVO);


    /**
     * 查询有奖问答活动详情。
     */
    ActivityAnswerDetailRespVO selectDetail(Long id);

    /**
     * 分页查询有奖问答活动。
     */
    PageResult<ActivityAnswerDO> getActivityAnswerPage(ActivityAnswerPageReqVO reqVO);

    /**
     * 更新活动启用状态。
     */
    Integer updateState(ActivityAnswerStateReqVO reqVO);

    /**
     * 分页查询答题记录。
     */
    PageResult<ActivityAnswerRecordRespVO> getRecordPage(ActivityAnswerRecordPageReqVO reqVO);

    /**
     * 统计答题记录。
     */
    ActivityAnswerStatisticsRespVO getRecordCount(ActivityAnswerRecordPageReqVO reqVO);

    /**
     * 导出答题记录，题目列按活动题库动态生成。
     */
    void exportRecord(ActivityAnswerRecordPageReqVO reqVO);

    /**
     * 分页查询奖励发放记录。
     */
    PageResult<ActivityAnswerRewardLogRespVO> getRewardLogPage(ActivityAnswerRewardLogPageReqVO reqVO);

    /**
     * 统计奖励发放人数和发放次数。
     */
    ActivityAnswerRewardStatisticsRespVO getRewardLogCount(ActivityAnswerRewardLogPageReqVO reqVO);

    /**
     * 导出奖励发放记录。
     */
    void exportRewardLog(ActivityAnswerRewardLogPageReqVO reqVO);

    /**
     * 更新实物奖品快递信息。
     */
    Integer updateExpress(ActivityAnswerUpdateExpressReqVO reqVO);

    /**
     * 获取活动概览数据。
     */
    Map<String, Object> getActivityAnswerAnalysis(Long id);

    /**
     * 获取活动每日趋势数据。
     */
    Map<String, Object> getActivityAnswerDailyAnalysis(ActivityAnswerAnalysisReqVO reqVO);

    /**
     * 获取题目分析数据。
     */
    java.util.List<ActivityAnswerQuestionAnalysisRespVO> getQuestionAnalysis(ActivityAnswerAnalysisReqVO reqVO);

    /**
     * 查询有奖问答活动推广配置。
     */
    ActivityAnswerSpreadRespVO selectSpread(Long id);

    /**
     * 修改有奖问答活动推广配置。
     */
    void updateSpread(ActivityAnswerSpreadSaveReqVO reqVO);
}
