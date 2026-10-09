package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 商品属性对象 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityCondimentsRespVO {


    @Schema(description = "小料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long condimentId;


    @Schema(description = "小料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condimentName;


    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;


    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer number;


    @Schema(description = "状态 1启用 0禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;


    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageUrl;

}
