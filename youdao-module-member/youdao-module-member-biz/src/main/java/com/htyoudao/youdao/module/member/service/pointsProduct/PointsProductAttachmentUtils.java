package com.htyoudao.youdao.module.member.service.pointsProduct;

import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductAttachmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 积分商品附件 JSON 与历史图片字段转换工具。
 */
@Slf4j
public final class PointsProductAttachmentUtils {

    private PointsProductAttachmentUtils() {
    }

    /**
     * 解析附件 JSON；没有新数据或 JSON 异常时，兼容读取历史逗号分隔图片地址。
     */
    public static List<PointsProductAttachmentVO> parseAttachments(String attachmentJson, String legacyUrls) {
        if (StringUtils.hasText(attachmentJson)) {
            try {
                List<PointsProductAttachmentVO> attachments = JsonUtils.parseArray(
                        attachmentJson, PointsProductAttachmentVO.class);
                List<PointsProductAttachmentVO> validAttachments = sanitizeAttachments(attachments);
                if (!validAttachments.isEmpty()) {
                    return validAttachments;
                }
            } catch (RuntimeException exception) {
                log.warn("积分商品附件JSON解析失败，改用历史图片字段：{}", exception.getMessage());
            }
        }
        return parseLegacyImages(legacyUrls);
    }

    /**
     * 获取商品封面。独立封面优先，历史商品未配置封面时回退到第一张头图。
     */
    public static String resolveThumbnail(String productThumbnail,
                                          List<PointsProductAttachmentVO> headerAttachments) {
        List<PointsProductAttachmentVO> thumbnails = parseLegacyImages(productThumbnail);
        if (!thumbnails.isEmpty()) {
            return thumbnails.get(0).getUrl();
        }
        return sanitizeAttachments(headerAttachments).stream()
                .filter(attachment -> Objects.equals(attachment.getType(), 2))
                .map(PointsProductAttachmentVO::getUrl)
                .findFirst()
                .orElse("");
    }

    private static List<PointsProductAttachmentVO> sanitizeAttachments(
            List<PointsProductAttachmentVO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }
        return attachments.stream()
                .filter(Objects::nonNull)
                .filter(attachment -> StringUtils.hasText(attachment.getUrl()))
                .filter(attachment -> Objects.equals(attachment.getType(), 1)
                        || Objects.equals(attachment.getType(), 2))
                .peek(attachment -> attachment.setUrl(attachment.getUrl().trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<PointsProductAttachmentVO> parseLegacyImages(String legacyUrls) {
        if (!StringUtils.hasText(legacyUrls)) {
            return new ArrayList<>();
        }
        List<PointsProductAttachmentVO> attachments = new ArrayList<>();
        for (String url : legacyUrls.split(",")) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            PointsProductAttachmentVO attachment = new PointsProductAttachmentVO();
            attachment.setUrl(url.trim());
            attachment.setType(2);
            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * 将附件列表序列化为 JSON，空列表保存为 []。
     */
    public static String toJson(List<PointsProductAttachmentVO> attachments) {
        return JsonUtils.toJsonString(attachments == null ? Collections.emptyList() : attachments);
    }

    /**
     * 提取附件地址写回历史字段，保证旧接口和积分兑换记录仍可读取。
     */
    public static String toLegacyUrls(List<PointsProductAttachmentVO> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .filter(Objects::nonNull)
                .map(PointsProductAttachmentVO::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
    }
}
