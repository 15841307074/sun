package com.htyoudao.youdao.module.promotion.service.douyin;

import com.htyoudao.youdao.module.promotion.service.douyin.dto.CancelVerifyRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.CancelVerifyResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.Certificate;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.DouYinApiResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareRequest;
import static org.mockito.Mockito.*;

import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.QueryResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyResponse.VerifyResult;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DyClientTest {

    @InjectMocks
    private DyClient dyClient;

    @Mock
    private DouyinTokenManager tokenManager;

    @Mock
    private DouyinProperties properties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private RestTemplate restTemplate = new RestTemplate();

    public static String poiId = "7508966884516055055";

    @BeforeEach
    void setUp()  {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(properties.getBaseUrl()).thenReturn("https://open.douyin.com");
        when(properties.getClientKey()).thenReturn("CLOUD_SECRET_REQUIRED");
        when(properties.getClientSecret()).thenReturn("CLOUD_SECRET_REQUIRED");
        when(properties.getAccountId()).thenReturn("7485283719483066419");
        when(tokenManager.getAccessToken()).thenReturn("clt.2c433764b2de39e8d3509e144a7d8fb7zoBAj4IeQpsYod8Vy6jzxWDqFpg1_hl");
    }

    @Test
    void all(){
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setPoiId(poiId);
        prepareRequest.setCode("106138052926754");
        DouYinApiResponse<PrepareResponse> prepare = dyClient.prepare(prepareRequest);
        PrepareResponse prepareData = prepare.getData();
        System.out.println("======prepare success");

        Certificate certificate = prepareData.getCertificates().get(0);
        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setPoiId(poiId);
        verifyRequest.setEncryptedCodes(Arrays.asList(certificate.getEncryptedCode()));
        verifyRequest.setVerifyToken(prepareData.getVerifyToken());
        DouYinApiResponse<VerifyResponse> verify = dyClient.verify(verifyRequest);
        System.out.println("======verify success");

        VerifyResult verifyResult = verify.getData().getVerifyResults().get(0);
        CancelVerifyRequest request = new CancelVerifyRequest();
        request.setShopOrderId(verifyResult.getOrderId());
        request.setVerifyId(verifyResult.getVerifyId());
        request.setCertificateId(verifyResult.getCertificateId());
        request.setCancelToken(UUID.randomUUID().toString());
        DouYinApiResponse<CancelVerifyResponse> response = dyClient.cancelVerify(request);
        System.out.println("======cancel success");

    }


    @Test
    void prepare() {
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setCode("106138052926754");
        prepareRequest.setPoiId("7508966884516055055");
        DouYinApiResponse<PrepareResponse> prepare = dyClient.prepare(prepareRequest);

        System.out.println(prepare.getData());
    }

    @Test
    void verify() {
        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setPoiId("7508966884516055055");

        verifyRequest.setEncryptedCodes(Arrays.asList("CgYIASAHKAESLgosp/Kdu4Xya2Tqp8KKwHNGAAf0gfQKrniwadOVQ61U+5IJswrUQiTCe+EncksaAA=="));
        verifyRequest.setVerifyToken("eec9f821-8359-4572-8a12-d50059e7b729");
        DouYinApiResponse<VerifyResponse> prepare = dyClient.verify(verifyRequest);
        System.out.println(prepare.getData());
    }

    @Test
    void cancelVerify() {
        CancelVerifyRequest request = new CancelVerifyRequest();
        request.setShopOrderId("1086319727963540087");
        request.setVerifyId("7522670447704524834");
        request.setCertificateId("7522398798683045898");
        request.setCancelToken("cancelToken");
        DouYinApiResponse<CancelVerifyResponse> response = dyClient.cancelVerify(request);
        System.out.println(response);
    }



    @Test
    void query() {
        DouYinApiResponse<QueryResponse> response = dyClient.query("1086440554022424808");
        System.out.println(response);
    }
}