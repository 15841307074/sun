package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialDataReqVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class MaterialTransferSaveReq {


    @Schema(description = "调拨记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "调出门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调出门店不能为空")
    private Long fromStoreId;

    @Schema(description = "调入门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调入门店不能为空")
    private Long toStoreId;

    @Schema(description = "调拨状态：1.调出未接收 2.调入未接收 3.已取消 4.调拨完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer transferType;

    @Schema(description = "调拨总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @Schema(description = "接收日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date receiveDate;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remark;

    @Valid
    private List<MaterialDataReqVo> materialList;



}
