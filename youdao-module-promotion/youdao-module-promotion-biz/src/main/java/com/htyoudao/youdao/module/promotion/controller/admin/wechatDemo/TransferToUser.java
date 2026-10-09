package com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo;



import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date;

/**
 * 发起转账
 */
@Slf4j
public class TransferToUser {
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(8, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(16, 5, TimeUnit.MINUTES))
            .build();
    private static String HOST = "https://api.mch.weixin.qq.com";

//    private static String HOST = "https://10.200.0.1:8443";
    private static String METHOD = "POST";
    private static String PATH = "/v3/fund-app/mch-transfer/transfer-bills";

    public static void main(String[] args) {

        // TODO: 请准备商户开发必要参数，参考：https://pay.weixin.qq.com/doc/v3/merchant/4013070756
        TransferToUser client = new TransferToUser(
                "1729793817",                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
                "7BA37A098376D524198F55F91061F95EFEADB716",         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
                "D:\\idea_work\\cloud-0090\\youdao-module-promotion\\youdao-module-promotion-biz\\src\\main\\resources\\apiclient_key.pem",     // 商户API证书私钥文件路径，本地文件路径
                "PUB_KEY_ID_0117297938172025101400382046001400",      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
                "D:\\idea_work\\cloud-0090\\youdao-module-promotion\\youdao-module-promotion-biz\\src\\main\\resources\\pub_key.pem"            // 微信支付公钥文件路径，本地文件路径
        );

        TransferToUserRequest request = new TransferToUserRequest();
        request.appid = "wx3b84773f5f12d87f";
        //商户订单号默认用商户号+时间戳+4位随机数
        request.outBillNo = "order859665445389";
        request.transferSceneId = "1000";
        request.openid = "oHVqn6ytaLh8aOxNfvtswLtNcYBU";
//        request.userName = client.encrypt("user_name");
        request.transferAmount = 10;
        request.transferRemark = "新会员开通有礼";
        request.notifyUrl = "https://test-saas.htyoudao.com/prod-api/app-api/promotion/redPacket/notify";
//        request.userRecvPerception = "现金奖励";
        request.transferSceneReportInfos = new ArrayList<>();
        {
            TransferSceneReportInfo transferSceneReportInfosItem0 = new TransferSceneReportInfo();
            transferSceneReportInfosItem0.infoType = "活动名称";
            transferSceneReportInfosItem0.infoContent = "新会员有礼";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem0);
            TransferSceneReportInfo transferSceneReportInfosItem1 = new TransferSceneReportInfo();
            transferSceneReportInfosItem1.infoType = "奖励说明";
            transferSceneReportInfosItem1.infoContent = "注册会员抽奖一等奖";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem1);
        };
        try {
            TransferToUserResponse response = client.run(request);
            // TODO: 请求成功，继续业务逻辑
            System.out.println(response);
        } catch (WXPayUtility.ApiException e) {
            // TODO: 请求失败，根据状态码执行不同的逻辑
            e.printStackTrace();
        }
    }

    public TransferToUserResponse run(TransferToUserRequest request) {
        String uri = PATH;
        String reqBody = WXPayUtility.toJson(request);

        Request.Builder reqBuilder = new Request.Builder().url(HOST + uri);
        log.info("请求的微信api"+HOST + uri);
        reqBuilder.addHeader("Accept", "application/json");
        reqBuilder.addHeader("Wechatpay-Serial", wechatPayPublicKeyId);
        reqBuilder.addHeader("Authorization", WXPayUtility.buildAuthorization(mchid, certificateSerialNo,privateKey, METHOD, uri, reqBody));
        reqBuilder.addHeader("Content-Type", "application/json");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqBody);
        reqBuilder.method(METHOD, requestBody);
        Request httpRequest = reqBuilder.build();

        // 发送HTTP请求
        OkHttpClient client = HTTP;
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            String respBody = WXPayUtility.extractBody(httpResponse);
            if (httpResponse.code() >= 200 && httpResponse.code() < 300) {
                // 2XX 成功，验证应答签名
                WXPayUtility.validateResponse(this.wechatPayPublicKeyId, this.wechatPayPublicKey,
                        httpResponse.headers(), respBody);

                // 从HTTP应答报文构建返回数据
                return WXPayUtility.fromJson(respBody, TransferToUserResponse.class);
            } else {
                throw new WXPayUtility.ApiException(httpResponse.code(), respBody, httpResponse.headers());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Sending request to " + uri + " failed.", e);
        }
    }

    /** 商户单号保持不变，查单不会发起新的转账。 */
    public TransferToUserResponse query(String outBillNo) {
        if (outBillNo == null || !outBillNo.matches("[A-Za-z0-9]{1,32}")) throw new IllegalArgumentException("Invalid merchant bill");
        String uri = PATH + "/out-bill-no/" + outBillNo;
        Request request = new Request.Builder().url(HOST + uri).get()
                .header("Accept", "application/json").header("Wechatpay-Serial", wechatPayPublicKeyId)
                .header("Authorization", WXPayUtility.buildAuthorization(mchid, certificateSerialNo, privateKey, "GET", uri, ""))
                .build();
        try (Response response = HTTP.newCall(request).execute()) {
            String body = WXPayUtility.extractBody(response);
            if (!response.isSuccessful()) throw new WXPayUtility.ApiException(response.code(), body, response.headers());
            WXPayUtility.validateResponse(wechatPayPublicKeyId, wechatPayPublicKey, response.headers(), body);
            return WXPayUtility.fromJson(body, TransferToUserResponse.class);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public boolean verifyCallback(String timestamp, String nonce, String signature, String serial, String body) {
        try {
            if (!wechatPayPublicKeyId.equals(serial) || Math.abs(java.time.Instant.now().getEpochSecond() - Long.parseLong(timestamp)) > 300) return false;
            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
            verifier.initVerify(wechatPayPublicKey);
            verifier.update((timestamp + "\n" + nonce + "\n" + body + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return verifier.verify(java.util.Base64.getDecoder().decode(signature));
        } catch (Exception e) { return false; }
    }

    private final String mchid;
    private final String certificateSerialNo;
    private final PrivateKey privateKey;
    private final String wechatPayPublicKeyId;
    private final PublicKey wechatPayPublicKey;

    public TransferToUser(String mchid, String certificateSerialNo, String privateKeyFilePath, String wechatPayPublicKeyId, String wechatPayPublicKeyFilePath) {
        this.mchid = mchid;
        this.certificateSerialNo = certificateSerialNo;
        this.privateKey = WXPayUtility.loadPrivateKeyFromPath(privateKeyFilePath);
        this.wechatPayPublicKeyId = wechatPayPublicKeyId;
        this.wechatPayPublicKey = WXPayUtility.loadPublicKeyFromPath(wechatPayPublicKeyFilePath);
    }

    public String encrypt(String plainText) {
        return WXPayUtility.encrypt(this.wechatPayPublicKey, plainText);
    }

    public static class TransferToUserRequest {
        @SerializedName("appid")
        public String appid;

        @SerializedName("out_bill_no")
        public String outBillNo;

        @SerializedName("transfer_scene_id")
        public String transferSceneId;

        @SerializedName("openid")
        public String openid;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("transfer_amount")
        public Integer transferAmount;

        @SerializedName("transfer_remark")
        public String transferRemark;

        @SerializedName("notify_url")
        public String notifyUrl;

        @SerializedName("user_recv_perception")
        public String userRecvPerception;

        @SerializedName("transfer_scene_report_infos")
        public List<TransferSceneReportInfo> transferSceneReportInfos = new ArrayList<TransferSceneReportInfo>();
    }

    @Data
    public static class TransferToUserResponse {

        public Integer code;

        public String message;
        @SerializedName("out_bill_no")
        public String outBillNo;

        @SerializedName("transfer_bill_no")
        public String transferBillNo;

        @SerializedName("create_time")
        public String createTime;

        @SerializedName("state")
        public TransferBillStatus state;

        @SerializedName("package_info")
        public String packageInfo;
    }

    public static class TransferSceneReportInfo {
        @SerializedName("info_type")
        public String infoType;

        @SerializedName("info_content")
        public String infoContent;
    }

    public enum TransferBillStatus {
        @SerializedName("ACCEPTED")
        ACCEPTED,
        @SerializedName("PROCESSING")
        PROCESSING,
        @SerializedName("WAIT_USER_CONFIRM")
        WAIT_USER_CONFIRM,
        @SerializedName("TRANSFERING")
        TRANSFERING,
        @SerializedName("SUCCESS")
        SUCCESS,
        @SerializedName("FAIL")
        FAIL,
        @SerializedName("CANCELING")
        CANCELING,
        @SerializedName("CANCELLED")
        CANCELLED
    }

}
