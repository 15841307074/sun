package com.htyoudao.youdao.module.promotion.util.wechatLink;

import lombok.Builder;
import lombok.Data;

/**
 * @author villky
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
