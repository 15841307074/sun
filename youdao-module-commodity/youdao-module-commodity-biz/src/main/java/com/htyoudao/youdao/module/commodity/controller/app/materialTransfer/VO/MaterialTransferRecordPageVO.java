package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialDataReqVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialDataRespVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class MaterialTransferRecordPageVO extends BusinessBaseDO {


    @Schema(description = "调拨记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "调出门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fromStoreId;

    @Schema(description = "调出门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fromStoreName;

    @Schema(description = "调入门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toStoreId;

    @Schema(description = "调入门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toStoreName;

    @Schema(description = "调拨状态：1.调出未接收 2.调入未接收 3.已取消 4.调拨完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer transferType;

    @Schema(description = "调拨总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @Schema(description = "接收日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date receiveDate;

    @Schema(description = "采购单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transferCode;

    @Schema(description = "项目名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessName;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remark;

    private List<MaterialDataRespVo> materialList;
}
