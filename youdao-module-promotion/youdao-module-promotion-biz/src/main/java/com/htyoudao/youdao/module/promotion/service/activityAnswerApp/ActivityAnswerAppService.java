package com.htyoudao.youdao.module.promotion.service.activityAnswerApp;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo.*;

import java.util.List;

/**
 * 有奖问答小程序服务。
 */
public interface ActivityAnswerAppService {

    /**
     * 查询有奖问答活动详情。
     */
    ActivityAnswerAppDetailRespVO getDetail(Long activityId, Long storeId, Long memberId);

    /**
     * 查询当前可用答题次数。
     */
    ActivityAnswerChanceCountRespVO getChanceCount(ActivityAnswerBaseReqVO reqVO);

    /**
     * 开始或继续答题。
     */
    ActivityAnswerJoinRespVO join(ActivityAnswerBaseReqVO reqVO);

    /**
     * 提交完整答题结果。
     */
    ActivityAnswerSubmitRespVO submit(ActivityAnswerSubmitReqVO reqVO);

    /**
     * 取消本次答题。
     */
    Boolean cancel(ActivityAnswerCancelReqVO reqVO);

    /**
     * 查询任务列表。
     */
    List<ActivityAnswerTaskRespVO> getTaskList(ActivityAnswerBaseReqVO reqVO);

    /**
     * 完成签到任务。
     */
    ActivityAnswerTaskCompleteRespVO signTask(ActivityAnswerBaseReqVO reqVO);

    /**
     * 检测分享任务是否可完成。
     */
    Boolean shareCheck(ActivityAnswerBaseReqVO reqVO);

    /**
     * 完成分享任务。
     */
    ActivityAnswerTaskCompleteRespVO shareTask(ActivityAnswerBaseReqVO reqVO);

    /**
     * 完成浏览首页任务。
     */
    ActivityAnswerTaskCompleteRespVO browseHomeTask(ActivityAnswerBaseReqVO reqVO);

    /**
     * 处理下单获得答题次数任务。
     */
    void handleOrderTask(ActivityJkOrderReqDTO reqDTO);

    /**
     * 查询我的答题记录。
     */
    PageResult<ActivityAnswerMyRecordRespVO> getMyRecord(ActivityAnswerMyRecordPageReqVO reqVO);

    /**
     * 查询我的奖励记录。
     */
    PageResult<ActivityAnswerMyRewardRespVO> getMyReward(ActivityAnswerMyRewardPageReqVO reqVO);

    /**
     * 保存实物奖品收货地址。
     */
    Boolean saveRewardAddress(ActivityAnswerRewardAddressSaveReqVO reqVO);
}
