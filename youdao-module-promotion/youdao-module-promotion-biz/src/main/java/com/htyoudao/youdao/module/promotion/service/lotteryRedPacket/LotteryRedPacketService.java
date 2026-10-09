package com.htyoudao.youdao.module.promotion.service.lotteryRedPacket;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryTransferSceneReportVO;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotify;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotifyData;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.req.TransferToUserRequest;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.response.TransferToUserResponse;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result.TransferRedPacketResult;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result.TransferRequest;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result.TransferResult;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferSceneReportMapper;
import com.htyoudao.youdao.module.promotion.enums.LotteryTransferStatus;
import com.htyoudao.youdao.module.promotion.enums.TransferBillStatus;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;


@Service
@Slf4j
public class LotteryRedPacketService {

    @Autowired
    private LotteryWeChatPayConfig weChatPayConfig;

    @Autowired
    private LotteryTransferRecordMapper lotteryTransferRecordMapper;

    @Autowired
    private LotteryTransferSceneReportMapper lotteryTransferSceneReportMapper;


    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private LotteryLogMapper lotteryLogMapper;

    private static String HOST = "https://api.mch.weixin.qq.com";

//    private static String HOST = "https://10.200.0.1:8443";
    private static String METHOD = "POST";
    private static String PATH = "/v3/fund-app/mch-transfer/transfer-bills";

    @Transactional
    public TransferToUser.TransferToUserResponse transferUser(LotteryRedPacketVo lotteryRedPacketVo) {
        LotteryTransferRecordDO record = saveTransferRecord(lotteryRedPacketVo);
        if (record.getStatus() == LotteryTransferStatus.WAIT_USER_CONFIRM
                || record.getStatus() == LotteryTransferStatus.SUCCESS) {
            TransferToUser.TransferToUserResponse response = new TransferToUser.TransferToUserResponse();
            response.setCode(200);
            response.setOutBillNo(record.getOutBillNo());
            response.setTransferBillNo(record.getTransferBillNo());
            response.setPackageInfo(record.getPackageInfo());
            response.setState(record.getStatus() == LotteryTransferStatus.SUCCESS
                    ? TransferToUser.TransferBillStatus.SUCCESS
                    : TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);
            return response;
        }
        TransferToUser client = new TransferToUser(
                weChatPayConfig.getMchId(),                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
                weChatPayConfig.getCertSerialNo(),         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
                weChatPayConfig.getPrivateKeyPath(),     // 商户API证书私钥文件路径，本地文件路径
                weChatPayConfig.getWechatPublicKeyId(),      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
                weChatPayConfig.getWechatPublicKeyPath()            // 微信支付公钥文件路径，本地文件路径
        );


        TransferToUser.TransferToUserRequest request = new TransferToUser.TransferToUserRequest();
        request.appid = weChatPayConfig.getAppId();
        //商户订单号默认用商户号+时间戳+4位随机数
        request.outBillNo = record.getOutBillNo();
        request.transferSceneId = weChatPayConfig.getTransferSceneId();
        request.openid = lotteryRedPacketVo.getOpenId();
//        request.userName = client.encrypt("user_name");
        request.transferAmount = lotteryRedPacketVo.getTransferAmount();
        request.transferRemark = lotteryRedPacketVo.getTransferRemark();
        request.notifyUrl = weChatPayConfig.getNotifyUrl();
//        request.userRecvPerception = "现金奖励";
        request.transferSceneReportInfos = new ArrayList<>();
        {
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem0 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem0.infoType = "活动名称";
            transferSceneReportInfosItem0.infoContent = "新会员有礼";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem0);
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem1 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem1.infoType = "奖励说明";
            transferSceneReportInfosItem1.infoContent = "注册会员抽奖一等奖";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem1);
        };

        try {
            TransferToUser.TransferToUserResponse response = client.run(request);
            // 4. 更新转账记录状态
            updateTransferRecord(record, response);
            response.setCode(200);
            return response;
        } catch (WXPayUtility.ApiException e) {
            e.printStackTrace();
            TransferToUser.TransferToUserResponse response = new TransferToUser.TransferToUserResponse();
            response.setCode(500);
            response.setMessage(e.getErrorMessage());
            record.setFailReason(e.getErrorMessage());
            record.setStatus(LotteryTransferStatus.FAIL);
            lotteryTransferRecordMapper.updateById(record);
            return response;
        }


    }
    /**
     * 发起转账
     */
    @Transactional
    public TransferRedPacketResult transferToUser(LotteryRedPacketVo lotteryRedPacketVo) {
        try {
            // 1. 保存转账记录
            LotteryTransferRecordDO record = saveTransferRecord(lotteryRedPacketVo);

            // 2. 构建请求参数
            TransferToUserRequest transferToUserRequest = buildTransferRequest(lotteryRedPacketVo);

            // 3. 调用微信API
            TransferToUserResponse response = callWeChatTransferAPI(transferToUserRequest);

            // 4. 更新转账记录状态
            updateTransferRecord(record, response);

            return TransferRedPacketResult.success(response, record.getOutBillNo());

        } catch (Exception e) {
            log.error("转账失败: {}", e.getMessage(), e);
            return TransferRedPacketResult.fail("转账失败: " + e.getMessage());
        }
    }

    /**
     * 保存转账记录
     */
    private LotteryTransferRecordDO saveTransferRecord(LotteryRedPacketVo lotteryRedPacketVo) {
        if (ObjectUtil.isNotEmpty(lotteryRedPacketVo.getOutBillNo())) {
            LotteryTransferRecordDO existing = lotteryTransferRecordMapper.selectOne(
                    new LambdaQueryWrapper<LotteryTransferRecordDO>()
                            .eq(LotteryTransferRecordDO::getOutBillNo, lotteryRedPacketVo.getOutBillNo())
                            .last("LIMIT 1"));
            if (existing != null) {
                return existing;
            }
        }
        LotteryTransferRecordDO record = new LotteryTransferRecordDO();
        if (ObjectUtil.isNotEmpty(lotteryRedPacketVo.getOutBillNo())) {
            record.setOutBillNo(lotteryRedPacketVo.getOutBillNo());
        } else if(ObjectUtil.isNotEmpty(lotteryRedPacketVo.getActivityId())){
            record.setOutBillNo(generateOutBillNoWithOriginalId(lotteryRedPacketVo.getActivityId()));
        }else{
            record.setOutBillNo(generateOutBillNo());
        }
        record.setAppId(weChatPayConfig.getAppId());
        record.setOpenId(lotteryRedPacketVo.getOpenId());
        record.setUserName(lotteryRedPacketVo.getUserName());
        record.setTransferAmount(lotteryRedPacketVo.getTransferAmount());
        record.setTransferRemark(lotteryRedPacketVo.getTransferRemark());
        record.setTransferSceneId(weChatPayConfig.getTransferSceneId());
        record.setUserRecvPerception(lotteryRedPacketVo.getUserRecvPerception());
        record.setNotifyUrl(weChatPayConfig.getNotifyUrl());
        record.setStatus(LotteryTransferStatus.INIT);

        // 保存主记录
        lotteryTransferRecordMapper.insert(record);

        // 保存场景报告信息
        if (lotteryRedPacketVo.getSceneReports() != null) {
            for (LotteryTransferSceneReportDO reportInfo : lotteryRedPacketVo.getSceneReports()) {
                LotteryTransferSceneReportDO report = new LotteryTransferSceneReportDO();
                report.setOutBillNo(record.getOutBillNo());
                report.setInfoType(reportInfo.getInfoType());
                report.setInfoContent(reportInfo.getInfoContent());
                lotteryTransferSceneReportMapper.insert(report);
            }
        }

        return record;
    }

    /**
     * 生成符合微信支付规范的商户单号
     * 格式: 商户号前缀 + 时间戳 + 随机数 + 业务标识
     */
    private String generateOutBillNo() {
        String mchId = weChatPayConfig.getMchId(); // 1729793817


        return generateStandardOutBillNo(mchId);
    }

    /**
     * 标准商户单号生成
     * 格式: 商户号后4位 + 时间戳(秒) + 6位随机数
     * 示例: 3817_1735623456_123456
     */
    private String generateStandardOutBillNo(String mchId) {
        // 取商户号后4位作为前缀
        String mchPrefix = mchId.length() > 4 ?
                mchId.substring(mchId.length() - 4) : mchId;

        // 使用秒级时间戳，避免毫秒太长
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        // 6位随机数字
        String random = RandomStringUtils.randomNumeric(16);

        return mchPrefix+timestamp + random;
    }

    /**
     * 构建转账请求
     */
    private TransferToUserRequest buildTransferRequest(LotteryRedPacketVo lotteryRedPacketVo) throws Exception {
        TransferToUserRequest transferRequest = new TransferToUserRequest();
        transferRequest.setAppid(weChatPayConfig.getAppId());
        transferRequest.setOutBillNo(generateOutBillNo());
        transferRequest.setTransferSceneId(weChatPayConfig.getTransferSceneId());
        transferRequest.setOpenid(lotteryRedPacketVo.getOpenId());

        // 加密用户姓名
        if (lotteryRedPacketVo.getUserName() != null) {
            String encryptedUserName = encrypt(lotteryRedPacketVo.getUserName());
            transferRequest.setUserName(encryptedUserName);
        }

        transferRequest.setTransferAmount(lotteryRedPacketVo.getTransferAmount());
        transferRequest.setTransferRemark(lotteryRedPacketVo.getTransferRemark());
//        transferRequest.setNotifyUrl(weChatPayConfig.getNotifyUrl());
        transferRequest.setUserRecvPerception(lotteryRedPacketVo.getUserRecvPerception());

        // 设置场景报告信息
        if (lotteryRedPacketVo.getSceneReports() != null) {
            List<LotteryTransferSceneReportDO> sceneReports = lotteryRedPacketVo.getSceneReports();
            if(sceneReports.size()>0){
                List<LotteryTransferSceneReportVO> lotteryTransferSceneReportVOS = new ArrayList<>();
                for (LotteryTransferSceneReportDO sceneReport : sceneReports) {
                    LotteryTransferSceneReportVO lotteryTransferSceneReportVO = new LotteryTransferSceneReportVO();
                    BeanUtils.copyProperties(sceneReport, lotteryTransferSceneReportVO);
                    lotteryTransferSceneReportVOS.add(lotteryTransferSceneReportVO);

                }
                transferRequest.setTransferSceneReportInfos(lotteryTransferSceneReportVOS);
            }

        }

        return transferRequest;
    }

    /**
     * 调用微信转账API
     */
    private TransferToUserResponse callWeChatTransferAPI(TransferToUserRequest req) throws Exception {
        String uri = PATH;



        TransferToUser.TransferToUserRequest request = new TransferToUser.TransferToUserRequest();
        request.appid = req.getAppid();
        //商户订单号默认用商户号+时间戳+4位随机数
        request.outBillNo = "order85966544536";
        request.transferSceneId = req.getTransferSceneId();
        request.openid = req.getOpenid();
        request.userName = req.getUserName();
        request.transferAmount = req.getTransferAmount();
        request.transferRemark = req.getTransferRemark();
//        request.notifyUrl = "https://www.weixin.qq.com/wxpay/pay.php";
//        request.userRecvPerception = req.getUserRecvPerception();
        request.transferSceneReportInfos = new ArrayList<>();
        {
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem0 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem0.infoType = "抽奖活动";
            transferSceneReportInfosItem0.infoContent = "红包";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem0);
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem1 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem1.infoType = "中奖";
            transferSceneReportInfosItem1.infoContent = "恭喜中奖";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem1);
        };


        String reqBody = WXPayUtility.toJson(request);

        // 构建HTTP请求
        Request.Builder reqBuilder = new Request.Builder().url(HOST + uri);
        reqBuilder.addHeader("Accept", "application/json");
        reqBuilder.addHeader("Wechatpay-Serial", weChatPayConfig.getWechatPublicKeyId());
        reqBuilder.addHeader("Authorization", buildAuthorization(uri, reqBody));
        reqBuilder.addHeader("Content-Type", "application/json");

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), reqBody);
        reqBuilder.method(METHOD, requestBody);

        Request httpRequest = reqBuilder.build();

        // 发送HTTP请求
        OkHttpClient client = new OkHttpClient.Builder().build();
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            String respBody = WXPayUtility.extractBody(httpResponse);

            if (httpResponse.code() >= 200 && httpResponse.code() < 300) {
                PublicKey publicKey = WXPayUtility.loadPublicKeyFromPath(weChatPayConfig.getWechatPublicKeyPath());
                // 2XX 成功，验证应答签名
                WXPayUtility.validateResponse(weChatPayConfig.getWechatPublicKeyId(), publicKey,
                        httpResponse.headers(), respBody);

                // 从HTTP应答报文构建返回数据
                return WXPayUtility.fromJson(respBody, TransferToUserResponse.class);
            } else {
                throw new WXPayUtility.ApiException(httpResponse.code(), respBody, httpResponse.headers());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("调用微信转账API失败: " + uri, e);
        }
    }

    /**
     * 生成Authorization头
     */
    private String buildAuthorization(String uri, String body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = generateNonceStr();

        // 构建签名字符串
        String message = String.format("%s\n%s\n%s\n%s\n%s\n",
                METHOD, uri, timestamp, nonceStr, body);

        // 使用私钥签名 - 修正参数顺序
        PrivateKey privateKey = WXPayUtility.loadPrivateKeyFromPath(weChatPayConfig.getPrivateKeyPath());
        String signature = WXPayUtility.sign(message, "SHA256withRSA", privateKey);

        return String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",serial_no=\"%s\",signature=\"%s\"",
                weChatPayConfig.getMchId(),
                nonceStr,
                timestamp,
                weChatPayConfig.getCertSerialNo(),
                signature
        );
    }

    /**
     * 更新转账记录状态
     */
    private void updateTransferRecord(LotteryTransferRecordDO record, TransferToUserResponse response) {
        record.setTransferBillNo(response.getTransferBillNo());
        record.setPackageInfo(response.getPackageInfo());

        // 根据微信返回的状态更新本地状态
        LotteryTransferStatus status = convertWeChatStatus(response.getState());
        record.setStatus(status);

        lotteryTransferRecordMapper.updateById(record);
        log.info("转账记录状态更新: outBillNo={}, status={}", record.getOutBillNo(), status);
    }


    /**
     * 更新转账记录状态
     */
    private void updateTransferRecord(LotteryTransferRecordDO record, TransferToUser.TransferToUserResponse response) {
        record.setTransferBillNo(response.getTransferBillNo());
        record.setPackageInfo(response.getPackageInfo());

        // 根据微信返回的状态更新本地状态
        LotteryTransferStatus status = convertWeChatStatus(response.getState());
        record.setStatus(status);

        lotteryTransferRecordMapper.updateById(record);
        log.info("转账记录状态更新: outBillNo={}, status={}", record.getOutBillNo(), status);
    }

    /**
     * 转换微信状态到本地状态
     */
    private LotteryTransferStatus convertWeChatStatus(TransferToUser.TransferBillStatus wechatStatus) {
        if (wechatStatus == null) return LotteryTransferStatus.INIT;

        switch (wechatStatus) {
            case ACCEPTED:
                return LotteryTransferStatus.ACCEPTED;
            case PROCESSING:
                return LotteryTransferStatus.PROCESSING;
            case WAIT_USER_CONFIRM:
                return LotteryTransferStatus.WAIT_USER_CONFIRM;
            case SUCCESS:
                return LotteryTransferStatus.SUCCESS;
            case FAIL:
                return LotteryTransferStatus.FAIL;
            default:
                return LotteryTransferStatus.INIT;
        }
    }

    /**
     * 转换微信状态到本地状态
     */
    private LotteryTransferStatus convertWeChatStatus(TransferBillStatus wechatStatus) {
        if (wechatStatus == null) return LotteryTransferStatus.INIT;

        switch (wechatStatus) {
            case ACCEPTED:
                return LotteryTransferStatus.ACCEPTED;
            case PROCESSING:
                return LotteryTransferStatus.PROCESSING;
            case WAIT_USER_CONFIRM:
                return LotteryTransferStatus.WAIT_USER_CONFIRM;
            case SUCCESS:
                return LotteryTransferStatus.SUCCESS;
            case FAIL:
                return LotteryTransferStatus.FAIL;
            default:
                return LotteryTransferStatus.INIT;
        }
    }

    /**
     * 加密数据
     */
    private String encrypt(String plainText) throws Exception {
        PublicKey publicKey = WXPayUtility.loadPublicKeyFromPath(weChatPayConfig.getWechatPublicKeyPath());
        return WXPayUtility.encrypt(publicKey, plainText);
    }

//    private String generateOutBillNo() {
//        return "TR" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(6);
//    }

    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }



    /**
     * 解析回调数据
     */
    public TransferNotify parseNotifyData(String notifyBody) throws Exception {
        Map<String, Object> notifyMap = objectMapper.readValue(notifyBody, Map.class);
        String eventType = (String) notifyMap.get("event_type");

        // 解密resource数据
        Map<String, Object> resource = (Map<String, Object>) notifyMap.get("resource");
        String ciphertext = (String) resource.get("ciphertext");
        String associatedData = (String) resource.get("associated_data");
        String nonce = (String) resource.get("nonce");

        String decryptedData = decryptAes256Gcm(ciphertext,
                weChatPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8),
                associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : null,
                nonce.getBytes(StandardCharsets.UTF_8));

        TransferNotify transferNotify = objectMapper.readValue(decryptedData, TransferNotify.class);
        transferNotify.setEvent_type(eventType);

        return transferNotify;
    }





    // AES-GCM解密方法
    public String decryptAes256Gcm(String ciphertext, byte[] key, byte[] associatedData, byte[] nonce)
            throws Exception {
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }

        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // 生成新单号：原始ID转36进制 + 时间戳（36进制）
    private String generateOutBillNoWithOriginalId(Long activityId) {
        // 将活动ID转为36进制（纯数字+字母）
        String encodedId = Long.toString(activityId, 36);
        // 添加时间戳的后8位（36进制）避免重复，并用下划线分隔（如果下划线不允许，可以用字母分隔）
        String timestamp = Long.toString(System.currentTimeMillis(), 36);
        return encodedId + "A" + timestamp; // 用字母A作为分隔符
    }




}
