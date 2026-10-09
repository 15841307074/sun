package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@TableName("raw_material_transfer_temporary")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialTransferTemporaryDO extends BusinessBaseDO{

    @Schema(description = "调拨记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rawMaterialId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long transferId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "0 未发送  1发送", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "调拨总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalPrice;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;


    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String brandName;




}
