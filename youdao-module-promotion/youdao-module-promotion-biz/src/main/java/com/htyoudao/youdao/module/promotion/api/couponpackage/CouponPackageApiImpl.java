package com.htyoudao.youdao.module.promotion.api.couponpackage;

import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * @author dht
 */
@DubboService
@Validated
public class CouponPackageApiImpl implements CouponPackageApi{

    @Resource
    private CouponPackageService couponPackageService;

    @Override
    public List<String> getCommunityQrImage(Long couponId) {
        return couponPackageService.getCommunityQrImage(couponId);
    }

    @Override
    public Map<Long, String> getPackageNameMap(List<Long> packageIds) {
        return couponPackageService.getPackageNameMap(packageIds);
    }
}
