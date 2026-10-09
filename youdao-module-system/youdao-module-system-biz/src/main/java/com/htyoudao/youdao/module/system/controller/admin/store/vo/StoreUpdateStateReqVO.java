package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 46942
 */
@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
public class StoreUpdateStateReqVO implements Serializable {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;

    @Schema(description = "营业状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer openStatus;



}
