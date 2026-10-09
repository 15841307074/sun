package com.htyoudao.youdao.module.commodity.controller.admin.template.VO;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 模版新增 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateSaveReqVo extends BusinessBaseDO {

    @Schema(description = "商品模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityTemplateId;


    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板名称不能为空")
    private String commodityTemplateName;


    @Schema(description = "模板状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityTemplateStatus;

    @Schema(description = "模板描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateDesc;

    @Schema(description = "模板属性（1.允许门店自己管理，2.品牌方统一管理）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板属性不能为空")
    private Integer templateFlavor;

    @Schema(description = "是否只允许修改价格 1 是 2 否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer choosePrice;
}
