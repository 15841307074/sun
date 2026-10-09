package com.htyoudao.youdao.module.promotion.api.goodcoupon;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.UpdateCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 优惠卷")
public interface GoodCouponApi {

    @Operation(summary = "更新优惠券（远程调用）")
    void updateGoodCoupon(@RequestBody GoodCouponCardVO cardVO);

    @Operation(summary = "获取优惠卷信息及门店列表")
    CommonResult<GoodCouponDataDTO> getByIdCoupon(@RequestParam("couponId") Long couponId);

    @Operation(summary = "批量获取优惠券名称")
    Map<Long, String> getCouponNameMap(List<Long> couponIds);

    Map<Long, Long> getCouponReceiveCountByStore(@RequestParam("couponIds") List<Long> couponIds,
                                                 @RequestParam("storeIds") List<Long> storeIds,
                                                 @RequestParam("startTime") LocalDateTime startTime,
                                                 @RequestParam("endTime") LocalDateTime endTime);

    /**
     * 修改优惠券和券包绑定的门店
     * @param goodCouponDTO goodCouponDTO
     */
    void updateCouponStore(GoodCouponStoreDTO goodCouponDTO);

    /**
     * 通过人群id获取优惠券数量
     * @param crowdId crowdId
     * @return Long
     */
    Long countByCrowdId(Long crowdId);

    /**
     * 获取社区二维码
     * @param couponId couponId
     * @return List<String>
     */
    List<String> getCommunityQrImage(Long couponId);


    /**
     * 通过门店id 标签id 更新优惠券门店
     */
    void updateCouponStoreByTagIdAndStoreId(List<UpdateCouponStoreDTO> couponStores, CouponStoreTagSqlTypeEnum sqlType);
}
