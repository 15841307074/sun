package com.htyoudao.youdao.module.promotion.service.activityJk;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogEventReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj.ActivityMjDO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;


public interface ActivityJkService extends IService<ActivityJkDO> {
    CommonResult<Integer> createActivityJk(@Valid ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO);

    CommonResult<Integer> updateActivityJk(@Valid ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO);

    CommonResult<Integer> deleteActivityJk(Long id);

    ActivityJkDetailRespVO selectInfo(Long id);

    CommonResult<Integer> copyActivityJk(ActivityJkSaveOrUpdateReqVO saveOrUpdateReqVO);

    Map<String, Object> getActivityJkAnalysis(String id);

    Map<String, Object> getActivityJkDailyAnalysis(ActivityJkLogEventReqVO activityJkLogEventReqVO);

    PageResult<ActivityCardLogRespVO> getActivityJkLogList(ActivityJkLogPageReqVO activityJkLogPageReqVO);

    ActivityJkStatisticsRespVO getActivityJkLogCount(ActivityJkLogPageReqVO activityJkLogPageReqVO);

    PageResult<ActivityExchangePageRespVO> getExchangeLogList(ActivityJkExchangePageReqVO activityJkExchangePageReqVO);

    ActivityJkStatisticsRespVO getExchangeCount(ActivityJkExchangePageReqVO activityJkExchangePageReqVO);

    Integer updateExpress(ActivityExchangeReqVO activityExchangeReqVO);


    void exportActivityJkLog(ActivityJkLogExportReqVO activityJkLogExportReqVO, HttpServletRequest request, HttpServletResponse response);

    void exportExchangeLog(ActivityJkExchangeExchangeReqVO activityJkLogExportReqVO, HttpServletRequest request, HttpServletResponse response);

    ActivityJkSpreadRespVO selectSpread(Long id);

    List<ActivityJkReqVO> getActivityJkList();

    void updateSpread(ActivityJkSpreadSaveReqVO activityJkSpreadSaveReqVO);

    Integer updateState(ActivityJkStateReqVO stateReqVO);
}
