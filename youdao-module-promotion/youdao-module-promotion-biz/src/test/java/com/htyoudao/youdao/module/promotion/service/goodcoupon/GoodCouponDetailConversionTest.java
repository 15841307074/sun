package com.htyoudao.youdao.module.promotion.service.goodcoupon;

import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 优惠券完整详情字段转换测试。
 */
class GoodCouponDetailConversionTest {

    /**
     * 验证非数字人群版本及多值枚举不会导致整个优惠券转换失败。
     */
    @Test
    void shouldConvertCompleteCouponFields() {
        GoodCouponDO source = new GoodCouponDO();
        source.setId(1L);
        source.setCouponCode("COUPON_1");
        source.setCouponType(1);
        source.setVersion("1001,1002");
        source.setUserRestrictions(3);
        source.setIsShare(2);
        source.setDoorsillType(2);
        source.setDoorsill(new BigDecimal("10.00"));
        source.setClaimTimeLimit(1);
        source.setClaimTimeSlot("2026-09-01#2026-09-30");
        source.setCouponImageUrl("https://example.com/coupon.png");

        PointsProductCouponDetailVO result =
                BeanUtils.toBean(source, PointsProductCouponDetailVO.class);

        assertThat(result).isNotNull();
        assertThat(result.getCouponCode()).isEqualTo("COUPON_1");
        assertThat(result.getCouponType()).isEqualTo(1);
        assertThat(result.getVersion()).isEqualTo("1001,1002");
        assertThat(result.getUserRestrictions()).isEqualTo(3);
        assertThat(result.getIsShare()).isEqualTo(2);
        assertThat(result.getDoorsillType()).isEqualTo(2);
        assertThat(result.getClaimTimeLimit()).isEqualTo(1);
        assertThat(result.getCouponImageUrl()).isEqualTo("https://example.com/coupon.png");
    }
}
