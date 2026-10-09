package com.htyoudao.youdao.module.promotion.service.activityVote;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface ActivityVoteService extends IService<ActivityVoteDO> {

    CommonResult<Integer> createActivityVote(@Valid ActivityVoteSaveOrUpdateReqVO reqVO);

    CommonResult<Integer> updateActivityVote(@Valid ActivityVoteSaveOrUpdateReqVO reqVO);

    CommonResult<Integer> deleteActivityVote(Long id);

    ActivityVoteDetailRespVO selectDetail(Long id);

    CommonResult<Integer> copyActivityVote(ActivityVoteSaveOrUpdateReqVO reqVO);

    ActivityVoteAnalysisRespVO getActivityVoteAnalysis(String id);

    Map<String, Object> getActivityVoteDailyAnalysis(ActivityVoteLogEventReqVO reqVO);

    List<ActivityVoteOptionAnalysisVO> getActivityVoteOptionAnalysis(ActivityVoteLogEventReqVO reqVO);

    PageResult<ActivityVoteLogRespVO> getVoteLogList(ActivityVoteLogPageReqVO reqVO);

    ActivityVoteStatisticsRespVO getVoteLogCount(ActivityVoteLogPageReqVO reqVO);

    PageResult<ActivityVoteRewardLogRespVO> getRewardLogList(ActivityVoteRewardLogPageReqVO reqVO);

    ActivityVoteRewardStatisticsRespVO getRewardLogCount(ActivityVoteRewardLogPageReqVO reqVO);

    void exportVoteLog(ActivityVoteLogPageReqVO reqVO);

    void exportRewardLog(ActivityVoteRewardLogPageReqVO reqVO);

    Integer updateExpress(ActivityVoteUpdateExpressReqVO reqVO);

    ActivityVoteSpreadRespVO selectSpread(Long id);

    void updateSpread(ActivityVoteSpreadSaveReqVO reqVO);

    List<ActivityVoteDetailRespVO> getActivityVoteList();

    Integer updateState(ActivityVoteStateReqVO reqVO);
}
