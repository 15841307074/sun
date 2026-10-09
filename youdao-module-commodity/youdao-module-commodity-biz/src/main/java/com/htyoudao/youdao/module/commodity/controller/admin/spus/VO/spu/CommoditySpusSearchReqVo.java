package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品模糊查询 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySpusSearchReqVo {


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


}
