package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO.UserCouponExpiredArchiveRespVO;

/**
 * 按实际过期时间归档历史用户券。
 */
public interface UserCouponExpiredArchiveService {

    UserCouponExpiredArchiveRespVO archive(UserCouponExpiredArchiveReqVO reqVO);
}
