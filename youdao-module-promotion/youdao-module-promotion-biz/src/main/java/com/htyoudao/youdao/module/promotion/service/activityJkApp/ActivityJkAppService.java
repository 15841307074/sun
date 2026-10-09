package com.htyoudao.youdao.module.promotion.service.activityJkApp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkDO;

import java.util.List;

public interface ActivityJkAppService extends IService<ActivityJkDO> {

    // ==================== 活动相关接口 ====================
    List<ActivityJkListVO> getActivityJkList(Long storeId);

    ActivityJkDetailVO getActivityJkDetail(Long activityId, Long memberId);

    // ==================== 抽卡相关接口 ====================
    Boolean checkCardStock(Long activityId, Long cardId);

    DrawResultVO drawCard(DrawReqVO reqVO);

    // ==================== 任务相关接口 ====================
    List<TaskVO> getTaskList(ActivityJkReqVO reqVO);

    Boolean signTask(ActivityJkReqVO reqVO);

    Boolean browseTask(ActivityJkReqVO reqVO);

    Boolean shareTask(ActivityJkReqVO reqVO);
    Boolean shareCheck(ActivityJkReqVO reqVO);

    /**
     * 校验用户当前是否具备抽卡资格。
     *
     * @param reqVO 集卡活动请求参数
     * @return 是否具备抽卡资格
     */
    Boolean verifyDraw(ActivityJkReqVO reqVO);

    /**
     * 查询用户当前活动的可用集卡次数。
     *
     * @param reqVO 集卡活动请求参数
     * @return 当前可用集卡次数
     */
    Integer getDrawChanceCount(ActivityJkReqVO reqVO);

    /**
     * 刷新用户当前活动的抽卡次数缓存。
     *
     * @param activityId 活动ID
     * @param memberId 用户ID
     */
    void refreshDrawChanceCache(Long activityId, Long memberId);

    // ==================== 卡片相关接口 ====================
    MyCardListVO getMyCardList(ActivityJkReqVO reqVO);

    Boolean exchangeUniversalCard(UniversalExchangeReqVO reqVO);

    // ==================== 兑换相关接口 ====================
    ActivityJkPrizeListVO getPrizeList(ActivityJkReqVO reqVO);

    PrizeExchangeResultVO exchangePrize(PrizeExchangeReqVO reqVO);

    Boolean saveAddress(AddressSaveReqVO reqVO);

    // ==================== 记录相关接口 ====================
    PageResult<DrawRecordVO> getDrawRecord(ActivityJkReqVO reqVO);

    PageResult<ExchangeRecordVO> getExchangeRecord(ActivityJkReqVO reqVO);

    Long getCardCount(Long id, Long memberId);

    ActivityCollectAppShareVO getShareVO(Long activityId);

}


