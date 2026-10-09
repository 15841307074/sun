package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyStore;


//@Configuration
@Slf4j
public class HttpClientConfig {
//    @Autowired
//    private LotteryWeChatPayConfig weChatPayConfig;
//
//    @Bean
//    @ConditionalOnBean(WeChatCertService.class)
//    public CloseableHttpClient weChatHttpClient(WeChatCertService weChatCertService) {
//        try {
//            log.info("开始初始化微信支付HTTP客户端...");
//            log.info("证书路径: {}", weChatPayConfig.getCertPath());
//            log.info("私钥路径: {}", weChatPayConfig.getKeyPath());
//
//            // 验证证书文件是否存在
//            File certFile = new File(weChatPayConfig.getCertPath());
//            File keyFile = new File(weChatPayConfig.getKeyPath());
//
//            if (!certFile.exists()) {
//                throw new RuntimeException("证书文件不存在: " + weChatPayConfig.getCertPath());
//            }
//            if (!keyFile.exists()) {
//                throw new RuntimeException("私钥文件不存在: " + weChatPayConfig.getKeyPath());
//            }
//
//            log.info("证书文件大小: {} bytes", certFile.length());
//            log.info("私钥文件大小: {} bytes", keyFile.length());
//
//            // 加载KeyStore
//            KeyStore keyStore = weChatCertService.loadPEMKeyStore();
//            log.info("KeyStore加载成功");
//
//            // 创建SSL上下文
//            SSLContext sslContext = SSLContexts.custom()
//                    .loadKeyMaterial(keyStore, weChatPayConfig.getMchId().toCharArray())
//                    .build();
//
//            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                    sslContext,
//                    new String[]{"TLSv1.2"},
//                    null,
//                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
//
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setSSLSocketFactory(sslsf)
//                    .build();
//
//            log.info("微信支付HTTP客户端初始化成功");
//            return httpClient;
//
//        } catch (Exception e) {
//            log.error("微信支付HTTP客户端初始化失败: {}", e.getMessage(), e);
//            throw new RuntimeException("微信支付HTTP客户端初始化失败: " + e.getMessage(), e);
//        }
//    }
}
