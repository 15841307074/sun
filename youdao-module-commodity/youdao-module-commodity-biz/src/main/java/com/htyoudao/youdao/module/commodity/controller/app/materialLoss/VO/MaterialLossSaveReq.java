package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MaterialLossSaveReq {


    @Schema(description = "损耗记录唯一ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "损耗类型：1样余 2炸糊 3餐品不达标 4丢餐 5员工餐 6留样 7试餐、尝餐 8活动赠送 9外卖损耗", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "损耗类型不能为空")
    private Integer lossType;


    @Schema(description = "仓库 ID")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;



    @Valid
    private List<MaterialDataReqVo> materialList;



}
