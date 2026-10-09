package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 积分商品附件信息。
 */
@Data
@Schema(description = "积分商品附件")
public class PointsProductAttachmentVO {

    @NotBlank(message = "附件地址不能为空")
    @Size(max = 2048, message = "附件地址长度不能超过2048个字符")
    @URL(message = "附件地址格式不正确")
    @Schema(description = "附件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @NotNull(message = "附件类型不能为空")
    @Min(value = 1, message = "附件类型只能为1视频或2图片")
    @Max(value = 2, message = "附件类型只能为1视频或2图片")
    @Schema(description = "附件类型：1视频，2图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;
}
