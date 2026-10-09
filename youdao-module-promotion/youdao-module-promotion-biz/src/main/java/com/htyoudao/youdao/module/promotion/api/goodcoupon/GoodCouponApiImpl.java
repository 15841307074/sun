package com.htyoudao.youdao.module.promotion.api.goodcoupon;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.UpdateCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.service.couponstore.CouponStoreService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@DubboService
@Validated
public class GoodCouponApiImpl implements GoodCouponApi {
    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private CouponStoreService couponStoreService;

    @Override
    public void updateGoodCoupon(GoodCouponCardVO cardVO) {
        goodCouponService.updateGoodCoupon(cardVO);
    }

    @Override
    public CommonResult<GoodCouponDataDTO> getByIdCoupon(Long couponId) {
        return CommonResult.success(goodCouponService.getByIdCoupon(couponId));
    }

    @Override
    public Map<Long, String> getCouponNameMap(List<Long> couponIds) {
        if (couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        return goodCouponService.getByIds(couponIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getCouponName() == null ? "" : item.getCouponName(),
                        (left, right) -> left));
    }

    @Override
    public Map<Long, Long> getCouponReceiveCountByStore(List<Long> couponIds, List<Long> storeIds,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        return goodCouponService.getCouponReceiveCountByStore(couponIds, storeIds, startTime, endTime);
    }

    @Override
    public void updateCouponStore(GoodCouponStoreDTO goodCouponDTO) {
        couponStoreService.updateCouponStore(goodCouponDTO);
    }

    @Override
    public Long countByCrowdId(Long crowdId) {
        return goodCouponService.countByCrowdId(crowdId);
    }

    @Override
    public List<String> getCommunityQrImage(Long couponId) {
        return goodCouponService.getCommunityQrImage(couponId);
    }

    @Override
    public void updateCouponStoreByTagIdAndStoreId(List<UpdateCouponStoreDTO> couponStores, CouponStoreTagSqlTypeEnum sqlType) {
        couponStoreService.updateCouponStoreByTagIdAndStoreId(couponStores, sqlType);
    }
}
