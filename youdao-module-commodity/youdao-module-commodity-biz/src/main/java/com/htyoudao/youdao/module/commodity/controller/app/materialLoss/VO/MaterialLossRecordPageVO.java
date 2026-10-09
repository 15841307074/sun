package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class MaterialLossRecordPageVO extends BusinessBaseDO {


    @Schema(description = "损耗记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "损耗类型：1样余 2炸糊 3餐品不达标 4丢餐 5员工餐 6留样 7试餐、尝餐 8活动赠送 9外卖损耗", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer lossType;

    @Schema(description = "损耗总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @Schema(description = "盘点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long takeId;

    @Schema(description = "损耗率", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attritionRate;

    @Schema(description = "原料列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MaterialDataRespVo> materialList;

    @Schema(description = "是否变色 true 是红  false是绿", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isFlag;
}
