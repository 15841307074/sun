package com.htyoudao.youdao.module.commodity.controller.admin.template.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommodityUpdateTemplateReqVO {

    @NotNull(message = "模板不能为空")
    @Schema(description = "模板 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityTemplateId;

    @NotBlank(message = "模板名称不能为空")
    private String commodityTemplateName;

    /**
     * 模板状态
     */
    private Long commodityTemplateStatus;
    /**
     * 模板描述
     */
    private String templateDesc;

    /**
     * 模板属性（1.允许门店自己管理，2.品牌方统一管理）
     */
    private Integer templateFlavor;

    /**
     * 是否只允许修改价格 1 是 2 否
     */
    private Integer choosePrice;
}
