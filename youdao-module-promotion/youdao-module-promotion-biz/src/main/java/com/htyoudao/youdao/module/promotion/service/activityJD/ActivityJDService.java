package com.htyoudao.youdao.module.promotion.service.activityJD;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDRespVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDSpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActPointRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import jakarta.validation.Valid;

import java.io.IOException;

public interface ActivityJDService {
    /**
     * 创建集点活动
     * @param activityJDReqSaveVO
     */
    void createActivityJD(ActivityJDReqSaveVO activityJDReqSaveVO);

    ActivityJDSpreadRespVO selectSpread(Long id);

    void updateSpread(@Valid ActivityJDSpreadSaveReqVO activityJDSpreadSaveReqVO);

    ActivityCollectAppShareVO getShareVO(Long activityId);

    /**
     * 获取集点活动详情
     * @param id
     * @return
     */
    ActivityJDFullRespVO selectFullInfo(Long id);

    /**
     * 集点活动上下架
     * @param activityJDEnabledUpdateReqVO
     */
    void updateEnabled(@Valid ActivityJDEnabledUpdateReqVO activityJDEnabledUpdateReqVO);

    /**
     * 删除集点活动
     * @param id
     * @throws IOException
     */
    void deleteActivityJD(Long id) throws IOException;

    /**
     * 获取集点详情
     * @param activityId
     * @return
     */
    ActPointRespVO getPointsDetail(Long activityId,Long memberId);

    /**
     * 更新集点活动
     * @param activityJDReqSaveVO
     */
    void updateActivityJD(@Valid ActivityJDReqSaveVO activityJDReqSaveVO);

    /**
     * 增加会员积分
     * @param activityId activityId
     * @param memberId memberId
     * @param points points
     */
    void incrMemberPointsByMe(Long activityId, Long memberId, Integer points);

    /**
     * 更新集点活动优惠券缓存
     * @param id
     */
    void updateCoupon(Long id);

    /**
     * 更新集点活动优惠券包缓存
     * @param id
     */
    void updateCouponPackage(Long id);

    void createUrl(Long businessId,String sortPath);
}
