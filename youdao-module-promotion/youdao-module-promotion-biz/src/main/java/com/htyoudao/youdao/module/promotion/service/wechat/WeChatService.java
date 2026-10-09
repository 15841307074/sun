package com.htyoudao.youdao.module.promotion.service.wechat;

/**
 * @author dht
 */
public interface WeChatService {
    /**
     * 获取微信token
     * @param b boolean
     * @param businessId businessId
     * @return String
     */
    String getWechatToken(boolean b, String businessId);
}
