package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MaterialLossDeleteReq {


    @Schema(description = "损耗记录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

}
