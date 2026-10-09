package com.htyoudao.youdao.module.infra.service.file;

import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.DescribeUploadTokenResponse;
import com.aliyun.green20220302.models.DescribeUploadTokenResponseBody;
import com.aliyun.green20220302.models.ImageModerationRequest;
import com.aliyun.green20220302.models.ImageModerationResponse;
import com.aliyun.green20220302.models.ImageModerationResponseBody;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.module.infra.framework.file.config.AliyunGreenProperties;
import com.htyoudao.youdao.module.infra.framework.file.core.utils.FileTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.FILE_IMAGE_MODERATION_FAILED;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.FILE_IMAGE_PORN_DETECTED;

/**
 * 阿里云内容安全增强版图片机器审核。
 * 图片先上传到内容安全提供的临时 OSS，再调用 ImageModeration 同步检测。
 */
@Service
@RefreshScope
@Slf4j
public class AliyunGreenImageModerationService {

    private static final int SUCCESS_CODE = 200;
    private static final long TOKEN_EXPIRATION_MARGIN_SECONDS = 60;

    private final AliyunGreenProperties properties;
    private final ObjectMapper objectMapper;
    private final Client client;
    private final Object tokenLock = new Object();

    private volatile DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData uploadToken;

    public AliyunGreenImageModerationService(AliyunGreenProperties properties, ObjectMapper objectMapper)
            throws Exception {
        this.properties = properties;
        this.objectMapper = objectMapper;
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setRegionId(properties.getRegionId())
                .setEndpoint(properties.getEndpoint());
        this.client = new Client(config);
    }

    public void check(String fileName, byte[] content) {
        if (!properties.isEnabled()) {
            return;
        }
        String contentType = FileTypeUtils.getMineType(content, fileName);
        if (contentType == null || !contentType.startsWith("image/")) {
            return;
        }
        if (isBlank(properties.getAccessKeyId()) || isBlank(properties.getAccessKeySecret())) {
            log.error("[check][阿里云内容安全 AccessKey 未配置]");
            throw exception(FILE_IMAGE_MODERATION_FAILED);
        }

        try {
            UploadedImage uploadedImage = uploadForModeration(fileName, content);
            validateResponse(scan(uploadedImage));
        } catch (ImageRejectedException ex) {
            throw exception(FILE_IMAGE_PORN_DETECTED);
        } catch (Exception ex) {
            log.error("[check][图片安全检测调用失败]", ex);
            throw exception(FILE_IMAGE_MODERATION_FAILED);
        }
    }

    private ImageModerationResponse scan(UploadedImage uploadedImage) throws Exception {
        Map<String, String> serviceParameters = Map.of(
                "ossBucketName", uploadedImage.bucketName(),
                "ossObjectName", uploadedImage.objectName(),
                "dataId", UUID.randomUUID().toString());
        ImageModerationRequest request = new ImageModerationRequest()
                .setService(properties.getService())
                .setServiceParameters(objectMapper.writeValueAsString(serviceParameters));
        return client.imageModerationWithOptions(request, new RuntimeOptions());
    }

    private void validateResponse(ImageModerationResponse response) {
        if (response == null || response.getStatusCode() == null || response.getStatusCode() != SUCCESS_CODE
                || response.getBody() == null) {
            throw new IllegalStateException("image moderation http status: "
                    + (response == null ? "null" : response.getStatusCode()));
        }
        ImageModerationResponseBody body = response.getBody();
        if (body.getCode() == null || body.getCode() != SUCCESS_CODE || body.getData() == null) {
            throw new IllegalStateException("image moderation failed, requestId=" + body.getRequestId()
                    + ", code=" + body.getCode() + ", message=" + body.getMsg());
        }

        String riskLevel = body.getData().getRiskLevel();
        List<ImageModerationResponseBody.ImageModerationResponseBodyDataResult> results =
                body.getData().getResult();
        if (riskLevel == null || results == null || results.isEmpty()) {
            throw new IllegalStateException("image moderation returned incomplete result, requestId="
                    + body.getRequestId());
        }

        String normalizedRiskLevel = riskLevel.toLowerCase(Locale.ROOT);
        if (properties.getBlockedRiskLevels().stream()
                .map(level -> level.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedRiskLevel::equals)) {
            log.warn("[check][图片机器审核未通过][requestId({}) riskLevel({}) labels({})]",
                    body.getRequestId(), riskLevel,
                    results.stream().map(ImageModerationResponseBody.ImageModerationResponseBodyDataResult::getLabel)
                            .toList());
            throw new ImageRejectedException();
        }
        if (!"none".equals(normalizedRiskLevel)) {
            log.info("[check][图片机器审核放行低风险结果][requestId({}) riskLevel({}) labels({})]",
                    body.getRequestId(), riskLevel,
                    results.stream().map(ImageModerationResponseBody.ImageModerationResponseBodyDataResult::getLabel)
                            .toList());
        }
    }

    private UploadedImage uploadForModeration(String fileName, byte[] content) throws Exception {
        DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData token = getUploadToken();
        String endpoint = properties.isInternal() ? token.getOssInternalEndPoint() : token.getOssInternetEndPoint();
        OSS ossClient = new OSSClientBuilder().build(endpoint, token.getAccessKeyId(), token.getAccessKeySecret(),
                token.getSecurityToken());
        try {
            String objectName = token.getFileNamePrefix() + UUID.randomUUID() + getSafeExtension(fileName);
            ossClient.putObject(token.getBucketName(), objectName, new ByteArrayInputStream(content));
            return new UploadedImage(token.getBucketName(), objectName);
        } finally {
            ossClient.shutdown();
        }
    }

    private DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData getUploadToken() throws Exception {
        DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData current = uploadToken;
        if (isTokenExpired(current)) {
            synchronized (tokenLock) {
                current = uploadToken;
                if (isTokenExpired(current)) {
                    DescribeUploadTokenResponse response = client.describeUploadToken();
                    if (response == null || response.getStatusCode() == null
                            || response.getStatusCode() != SUCCESS_CODE || response.getBody() == null
                            || response.getBody().getCode() == null
                            || response.getBody().getCode() != SUCCESS_CODE
                            || response.getBody().getData() == null) {
                        throw new IllegalStateException("get upload token failed, status="
                                + (response == null ? "null" : response.getStatusCode()) + ", code="
                                + (response == null || response.getBody() == null
                                ? "null" : response.getBody().getCode()));
                    }
                    uploadToken = current = response.getBody().getData();
                }
            }
        }
        return current;
    }

    private boolean isTokenExpired(
            DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData token) {
        return token == null || token.getExpiration() == null
                || token.getExpiration() <= System.currentTimeMillis() / 1000 + TOKEN_EXPIRATION_MARGIN_SECONDS;
    }

    private String getSafeExtension(String fileName) {
        if (isBlank(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(index + 1);
        return extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension.toLowerCase(Locale.ROOT) : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record UploadedImage(String bucketName, String objectName) {
    }

    private static final class ImageRejectedException extends RuntimeException {
    }

}
