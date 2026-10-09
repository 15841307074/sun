package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MaterialTransferDeleteReq {


    @Schema(description = "调拨记录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

}
