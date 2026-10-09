package com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope;


import java.net.URL;
import org.junit.jupiter.api.Test;

class WXPayUtilityTest {

    @Test
    void loadPrivateKeyFromString() {
        URL resourceUrl = getClass().getClassLoader().getResource("apiclient_key.pem");
        WXPayUtility.loadPrivateKeyFromPath(resourceUrl.getPath());
    }

    @Test
    void loadPublicKeyFromString() {
        URL resourceUrl = getClass().getClassLoader().getResource("pub_key.pem");
        WXPayUtility.loadPublicKeyFromPath(resourceUrl.getPath());
    }
}