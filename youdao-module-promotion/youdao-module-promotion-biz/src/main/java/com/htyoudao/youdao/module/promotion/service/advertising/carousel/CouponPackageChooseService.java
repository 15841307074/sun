package com.htyoudao.youdao.module.promotion.service.advertising.carousel;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChooseReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO.CouponPackageChoosedReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponChooseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel.CouponPackageChooseDO;

public interface CouponPackageChooseService {
    PageResult<CouponPackageChooseDO> getPage(CouponPackageChooseReqVO couponChooseReqVO);

    CouponPackageChooseDO getChoose(CouponPackageChoosedReqVO couponPackageChoosedReqVO);
}
