package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 调拨记录分页入参
 */
@Data
public class MaterialTransferRecordPageReq extends PageParam {


    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "调出门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fromStoreId;

    @Schema(description = "调入门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toStoreId;

    @Schema(description = "关联的原材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @Schema(description = "调拨状态：1.调出未接收 2.调入未接收 3.已取消 4.调拨完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer transferType;


}
