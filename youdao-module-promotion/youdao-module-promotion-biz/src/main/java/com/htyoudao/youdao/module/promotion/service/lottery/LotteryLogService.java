package com.htyoudao.youdao.module.promotion.service.lottery;

import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotify;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Map;

public interface LotteryLogService {
    PageResult<LotteryLogDO> getLotteryLogList(Integer pageNum, Integer pageSize, LotteryLogReqVO lotteryLogReqVO);

    Integer updateExpress(LotteryLogReqVO lotteryLogReqVO);

    /**
     * 退回积分、奖品
     */
    void returnReal();

    Map<String,Object> getLotteryLogAnalysis(String id);

    PageResult<LotteryLogDO> getByLotteryLogList(LotteryLogReqVO lotteryLogReqVO);

    LotteryLogStatisticsRespVO lotteryDrawStatistics(LotteryLogReqVO lotteryLogReqVO);

    void exportLotteryList(@Valid LotteryLogExportVO lotteryLogExportVO, HttpServletRequest request, HttpServletResponse response) throws ServerException;


    Map<String, Object> getLotteryLogDailyAnalysis(LotteryLogEventReqVO lotteryLogEventReqVO);


    public boolean processTransferNotify(TransferNotify notifyData);

    LotteryLogDO selectLotteryId(Long lotteryId);
}
