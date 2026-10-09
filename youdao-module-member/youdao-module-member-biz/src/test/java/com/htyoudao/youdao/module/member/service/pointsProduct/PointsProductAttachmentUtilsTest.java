package com.htyoudao.youdao.module.member.service.pointsProduct;

import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductAttachmentVO;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductEditReqVo;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductSaveReqVo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 积分商品附件兼容与校验测试。
 */
class PointsProductAttachmentUtilsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldPreserveMediaTypeWhenReadingNewAttachmentJson() {
        PointsProductAttachmentVO video = attachment("https://example.com/product.mp4", 1);

        List<PointsProductAttachmentVO> result = PointsProductAttachmentUtils.parseAttachments(
                PointsProductAttachmentUtils.toJson(List.of(video)), "https://example.com/legacy.png");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/product.mp4");
        assertThat(result.get(0).getType()).isEqualTo(1);
    }

    @Test
    void shouldFallbackToLegacyImagesWhenAttachmentJsonIsMissing() {
        List<PointsProductAttachmentVO> result = PointsProductAttachmentUtils.parseAttachments(
                null, "https://example.com/one.png,https://example.com/two.png");

        assertThat(result).extracting(PointsProductAttachmentVO::getUrl)
                .containsExactly("https://example.com/one.png", "https://example.com/two.png");
        assertThat(result).extracting(PointsProductAttachmentVO::getType)
                .containsOnly(2);
    }

    @Test
    void shouldFallbackToLegacyImagesWhenAttachmentJsonIsEmptyOrInvalid() {
        String legacyUrl = "https://example.com/legacy.png";

        assertThat(PointsProductAttachmentUtils.parseAttachments("[]", legacyUrl))
                .extracting(PointsProductAttachmentVO::getUrl)
                .containsExactly(legacyUrl);
        assertThat(PointsProductAttachmentUtils.parseAttachments("not-json", legacyUrl))
                .extracting(PointsProductAttachmentVO::getUrl)
                .containsExactly(legacyUrl);
        assertThat(PointsProductAttachmentUtils.parseAttachments(
                "[{\"url\":\"\",\"type\":2}]", legacyUrl))
                .extracting(PointsProductAttachmentVO::getUrl)
                .containsExactly(legacyUrl);
    }

    @Test
    void shouldResolveThumbnailFromLegacyHeaderAndReturnEmptyWhenNoImageExists() {
        List<PointsProductAttachmentVO> legacyHeaders = PointsProductAttachmentUtils.parseAttachments(
                null, "https://example.com/header.png");

        assertThat(PointsProductAttachmentUtils.resolveThumbnail(null, legacyHeaders))
                .isEqualTo("https://example.com/header.png");
        assertThat(PointsProductAttachmentUtils.resolveThumbnail(null,
                List.of(attachment("https://example.com/video.mp4", 1)))).isEmpty();
        assertThat(PointsProductAttachmentUtils.resolveThumbnail(null, null)).isEmpty();
    }

    @Test
    void shouldRejectVideoInProductDetailAttachments() {
        PointsProductSaveReqVo request = validSaveRequest();
        request.setProductDetailAttachments(List.of(
                attachment("https://example.com/product.mp4", 1)));

        Set<ConstraintViolation<PointsProductSaveReqVo>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("商品详情附件只能为图片");
    }

    @Test
    void shouldRejectInvalidAttachmentUrlAndType() {
        PointsProductAttachmentVO attachment = attachment("not-a-url", 3);

        Set<ConstraintViolation<PointsProductAttachmentVO>> violations = validator.validate(attachment);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("附件地址格式不正确", "附件类型只能为1视频或2图片");
    }

    /** 校验接口输出数字类型，且不接受英文附件类型。 */
    @Test
    void shouldSerializeNumericTypesAndRejectEnglishTypes() {
        String json = PointsProductAttachmentUtils.toJson(List.of(
                attachment("https://example.com/product.mp4", 1),
                attachment("https://example.com/product.jpg", 2)));
        assertThat(JsonUtils.parseTree(json).get(0).get("type").isIntegralNumber()).isTrue();
        assertThat(JsonUtils.parseTree(json).get(0).get("type").intValue()).isEqualTo(1);
        assertThat(JsonUtils.parseTree(json).get(1).get("type").intValue()).isEqualTo(2);
        for (String type : List.of("video", "image")) {
            assertThatThrownBy(() -> JsonUtils.parseObject(
                    "{\"url\":\"https://example.com/media\",\"type\":\"" + type + "\"}",
                    PointsProductAttachmentVO.class)).isInstanceOf(RuntimeException.class);
        }
    }

    /** 校验类型边界、图片详情以及修改接口的视频限制。 */
    @Test
    void shouldValidateNumericTypesForCreateAndEdit() {
        for (Integer type : new Integer[]{null, 0, 3}) {
            assertThat(validator.validate(attachment("https://example.com/media", type))).isNotEmpty();
        }
        for (int type : new int[]{1, 2}) {
            assertThat(validator.validate(attachment("https://example.com/media", type))).isEmpty();
        }
        PointsProductSaveReqVo request = validSaveRequest();
        request.setProductDetailAttachments(List.of(attachment("https://example.com/detail.jpg", 2)));
        assertThat(validator.validate(request)).isEmpty();
        PointsProductEditReqVo edit = new PointsProductEditReqVo();
        edit.setProductDetailAttachments(List.of(attachment("https://example.com/detail.mp4", 1)));
        assertThat(validator.validate(edit)).extracting(ConstraintViolation::getMessage)
                .contains("商品详情附件只能为图片");
    }

    private static PointsProductSaveReqVo validSaveRequest() {
        PointsProductSaveReqVo request = new PointsProductSaveReqVo();
        request.setProductName("测试商品");
        request.setProductSort(1L);
        request.setProductType(2);
        request.setProductPrice(100L);
        request.setProductInventory(10);
        return request;
    }

    private static PointsProductAttachmentVO attachment(String url, Integer type) {
        PointsProductAttachmentVO attachment = new PointsProductAttachmentVO();
        attachment.setUrl(url);
        attachment.setType(type);
        return attachment;
    }
}
