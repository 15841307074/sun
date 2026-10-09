package com.htyoudao.youdao.module.promotion.service.market;

import java.util.List;
import java.util.Map;

/**
 * 短信服务
 * @author dht
 */
public interface SmsService {

    /**
     * 批量发送短信
     *
     * @param phoneNumbers   号码列表
     * @param signNames      签名列表
     * @param templateCode   模板代码
     * @param templateParams 模板参数列表
     * @return 发送回执 ID
     * @throws Exception 异常
     */
    String sendBatch(List<String> phoneNumbers, List<String> signNames, String templateCode,
                     List<Map<String, String>> templateParams) throws Exception;
}
