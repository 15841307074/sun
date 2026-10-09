package com.htyoudao.youdao.module.member.api.pointsproduct;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.ApiConstants;
import com.htyoudao.youdao.module.member.api.pointsproduct.dto.PointsProductDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dht
 */
@FeignClient(name = "member-server") // TODO 0090：fallbackFactory =
@Tag(name = "积分商城")
public interface PointsProductApi {

    String PREFIX = ApiConstants.PREFIX + "member/points-product";

    /**
     * 更新最后下单时间
     * @param productId productId
     * @return PointsProductDTO
     */
    @GetMapping("/getById")
    CommonResult<PointsProductDTO> getById(@RequestParam("productId") Long productId);

    /**
     * 更新剩余数量
     * @param productId productId
     */
    @PutMapping("/getById")
    void updateRest(Long productId);

    /**
     * 积分商城优惠券的数量
     * @param couponCode couponCode
     * @return Long
     */
    CommonResult<Long> getCountByCouponCode(String couponCode);
}
