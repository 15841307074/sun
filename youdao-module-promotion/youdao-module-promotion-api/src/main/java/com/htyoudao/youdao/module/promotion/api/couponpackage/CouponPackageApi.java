package com.htyoudao.youdao.module.promotion.api.couponpackage;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - 优惠卷包")
public interface CouponPackageApi {

    /**
     * 获取社区二维码
     * @param couponId couponId
     * @return List<String>
     */
    List<String> getCommunityQrImage(Long couponId);

    /**
     * 根据券包 id 批量查询券包名称
     *
     * @param packageIds packageIds
     * @return Map<Long, String>
     */
    Map<Long, String> getPackageNameMap(List<Long> packageIds);
}
