package com.htyoudao.youdao.module.promotion.service.lottery;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsDetailRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySpreadSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface LotterySettingService {

    PageResult<LotterySettingsRespVO> getLotterySettingsListPage(Integer pageNum, Integer pageSize, LotterySettingsReqVO lotterySettingsVO);

    CommonResult<Integer> addLotteryPrize(LotterySettingsReqVO lotterySettingsVO);

    CommonResult<Integer> updateLotteryState(LotterySettingsReqVO lotterySettingsVO);

    CommonResult<Integer> deleteLottery(long id);

    LotterySettingsDetailRespVO getLotteryDetails(long id);

    CommonResult<Integer> updateLottery(LotterySettingsReqVO lotterySettingsVO);

    CommonResult<Integer> copyLotteryPrize(LotterySettingsReqVO lotterySettingsVO);

    CommonResult<Integer> saveLottery(@Valid LotterySettingsAddReqVO lotterySettingsAddVO);

    CommonResult<Integer> updateLotteryData(@Valid LotterySettingsUpdateReqVO lotterySettingsUpdateReqVO);

    CommonResult<Integer> removeLottery(long id);

    LotterySettingsDetailDataRespVO getByDetail(long id);

    LotterySpreadRespVO selectSpread(Long id);

    void updateSpread(@Valid LotterySpreadSaveReqVO lotterySpreadSaveReqVO);

    CommonResult<Integer> updateState(LotterySettingsReqVO lotterySettingsVO);

    void lotteryPoolReset();
}
