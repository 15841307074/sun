package com.htyoudao.youdao.module.promotion.api.test;

import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GetReduceAmountReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * details
 *
 * @author liuzhaowang
 */
@Tag(name = "测试")
public interface PromotionTestApi {

    @Operation(summary = "测试")
    CouponCalculateRespVO test(GetReduceAmountReqVO getReduceAmountReqVO);
}
