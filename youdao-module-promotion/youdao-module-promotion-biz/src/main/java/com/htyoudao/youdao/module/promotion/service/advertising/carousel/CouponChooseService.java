package com.htyoudao.youdao.module.promotion.service.advertising.carousel;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChoosedReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponPageRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;

public interface CouponChooseService {
    PageResult<CouponChooseDO> getPage(CouponChooseReqVO couponChooseReqVO);

    CouponChooseDO getChoose(CouponChoosedReqVO couponChoosedReqVO);

    PageResult<CouponChooseDO> asyncPage(CouponChooseReqVO couponChooseReqVO);
}
