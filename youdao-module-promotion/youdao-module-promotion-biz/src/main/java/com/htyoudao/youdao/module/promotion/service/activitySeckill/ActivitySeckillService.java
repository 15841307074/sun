package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppShareVO;
import jakarta.validation.Valid;

public interface ActivitySeckillService {
    void createActivitySeckill(ActivitySeckillReqSaveVO activitySeckillReqSaveVO);

    void updateActivitySeckill(@Valid ActivitySeckillReqSaveVO activitySeckillReqSaveVO);

    ActivitySeckillRespVO selectInfo(Long id);

    ActivitySeckillSpreadRespVO selectSpread(Long id);

    void deleteActivitySeckill(Long id);

    void updateSpread(@Valid ActivitySeckillSpreadSaveReqVO activitySeckillSpreadReqVO);

    void updateEnabled(@Valid ActivitySeckillEnabledUpdateReqVO activitySeckillEnabledUpdateReqVO);


    ActivitySeckillAppShareVO getShareVO(Long activityId);
}
