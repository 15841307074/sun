package com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "商品标签 VO")
@Data
public class CommodityTagReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签样式", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String style;

    @Schema(description = "标签背景", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "标签背景不能为空")
    private String image;
}
