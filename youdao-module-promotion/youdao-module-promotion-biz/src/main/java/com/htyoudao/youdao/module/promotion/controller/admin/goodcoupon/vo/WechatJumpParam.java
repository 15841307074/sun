package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author dht
 */
@Builder
@Data
public class WechatJumpParam {
    private String path;
    private String query;
    private Long businessId;
    private String sign;
    private String timestamp;
}