package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 商品属性对象 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityFlavorRespVO {


    @Schema(description = "属性ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long flavorId;


    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flavorName;


    @Schema(description = "key 属性值 value 属性是否上下架 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, Integer>> flavorValueListMap = new ArrayList<>();


    @Schema(description = "属性值列表，以逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flavorValue;


    @Schema(description = "属性类型 1 是单选 2 是多选 弃用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer flavorType;
}
