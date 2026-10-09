package com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

//第三方商品列表信息
@Data
public class SplitMaterialVo implements Serializable {


    @Serial
    private static final long serialVersionUID = -988955062859099349L;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;







}
