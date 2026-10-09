package com.htyoudao.youdao.module.promotion.controller.admin.order.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * pc端拼单配置
 * </p>
 *
 * @author zhangjihe
 * @since 2024-11-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplicingOrderConfigReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6898521379931280907L;

    @Schema(description = "banner图片")
    @NotBlank(message = "banner图片不能为空")
    @DiffLogField(name = "banner图片")
    private String bannerUrl;

    @Schema(description = "拼单折扣")
    @NotBlank(message = "拼单折扣不能为空")
    @DiffLogField(name = "拼单折扣")
    private String discount;
}
