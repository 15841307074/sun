package com.htyoudao.youdao.module.promotion.api.test;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GetReduceAmountReqVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * details
 *
 * @author liuzhaowang
 */
@DubboService
@Slf4j
public class PromotionTestApiImpl implements PromotionTestApi {
    @SneakyThrows
    @Override
    @SentinelResource(value = "test", fallback = "testFallback")
    // @SentinelResource(value = "test", blockHandler = "exceptionHandler", fallback = "testFallback")
    public CouponCalculateRespVO test(GetReduceAmountReqVO getReduceAmountReqVO) {
        // log.info(getReduceAmountReqVO.toString());
        log.info("business-id : {}", BusinessContextHolder.getBusinessId());
        // int i = 1 / 0;
        // Thread.sleep(600);
        return new CouponCalculateRespVO();
    }


    /**
     * Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
     *
     * @return {@link CommonResult }<{@link String }>
     */
    public CouponCalculateRespVO testFallback(GetReduceAmountReqVO getReduceAmountReqVO, Throwable ex) {
        log.error(ex.getMessage(), ex);
        return new CouponCalculateRespVO().setErrorCode(ErrorCodeConstants.POINTS_PRODUCT_NOT_E);
    }

    /**
     * Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
     *
     * @param ex exception
     * @return {@link CommonResult }<{@link String }>
     */
    public CommonResult<String> exceptionHandler(BlockException ex) {
        log.error(ex.getMessage(), ex);
        return CommonResult.success("Oops, error occurred at test");
    }
}
