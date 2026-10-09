package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调拨接收状态
 */
@Data
public class MaterialTransferReceiveReq {


    @Schema(description = "调拨记录唯一ID")
    @NotNull(message = "调拨ID不能为空")
    private Long id;

    @Schema(description = "调拨状态：1.调出未接收 2.调入未接收 3.已取消 4.调拨完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调拨类型不能为空")
    private Integer transferType;


    @Schema(description = "门店ID")
    private Long storeId;

}
