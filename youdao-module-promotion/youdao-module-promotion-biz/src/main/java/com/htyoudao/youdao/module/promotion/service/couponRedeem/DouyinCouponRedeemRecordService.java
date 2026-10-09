package com.htyoudao.youdao.module.promotion.service.couponRedeem;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.app.couponRedeem.VO.RedeemDouyinCouponReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponRedeem.DouyinCouponRedeemRecordDO;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareResponse;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-07-02
 */
public interface DouyinCouponRedeemRecordService extends IService<DouyinCouponRedeemRecordDO> {

    void redeemDouyinCoupon(RedeemDouyinCouponReqVO reqVO);

    PrepareResponse getPrepareResponse(String douyinCouponCode, String shortLink, String douyinStoreId);
}
