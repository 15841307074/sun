package com.htyoudao.youdao.module.promotion.service.market;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsResponseBody;
import com.aliyun.teautil.models.RuntimeOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dht
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService{

    @Resource
    private Client smsClient;
    @Resource
    private RuntimeOptions smsRuntimeOptions;
    @Resource
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public String sendBatch(List<String> phoneNumbers, List<String> signNames, String templateCode,
                            List<Map<String, String>> templateParams) throws Exception {
        // 校验参数
        validateParams(phoneNumbers, signNames, templateParams);

        // 构建请求
        SendBatchSmsRequest request = new SendBatchSmsRequest()
                .setPhoneNumberJson(JSON.toJSONString(phoneNumbers))
                .setSignNameJson(JSON.toJSONString(signNames))
                .setTemplateCode(templateCode)
                .setTemplateParamJson(JSON.toJSONString(templateParams));

        // 批量发送
        SendBatchSmsResponse smsResponse = smsClient.sendBatchSmsWithOptions(request, smsRuntimeOptions);
        log.info("短信发送结果：{}", JSON.toJSONString(smsResponse));
        return handleResponse(smsResponse.body);
    }

    /**
     * 验证参数
     *
     * @param phones 号码列表
     * @param signs  签名列表
     * @param params 参数列表
     */
    private void validateParams(List<String> phones, List<String> signs, List<Map<String, String>> params) {
        final int maxPhoneNum = 100;
        if (phones.size() > maxPhoneNum) {
            throw new IllegalArgumentException("单次发送手机号不得超过100个");
        }
        if (phones.size() != signs.size() || phones.size() != params.size()) {
            throw new IllegalArgumentException("参数数量不一致");
        }
    }

    /**
     * 检查重复项
     *
     * @param phone        电话
     * @param templateCode 模板代码
     * @return boolean
     */
    public boolean checkDuplicate(String phone, String templateCode) {
        final String key = "sms:lock:" + phone + ":" + templateCode;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS));
    }

    private String handleResponse(SendBatchSmsResponseBody body) {
        final String ok = "OK";
        if (!ok.equalsIgnoreCase(body.getCode())) {
            log.error("短信发送失败 - Code: {}, Message: {}", body.getCode(), body.getMessage());
            return "fail";
        }
        return body.getBizId();
    }
}
