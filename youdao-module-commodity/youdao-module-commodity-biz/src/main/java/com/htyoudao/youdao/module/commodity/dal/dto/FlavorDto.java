package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "app - 商品 属性 VO")
public class FlavorDto implements Serializable {

    @Schema(description = "属性ID")
    private Long flavorId;

    @Schema(description = "属性名称")
    private String flavorName;

    @Schema(description = "属性值")
    private List<String> flavorValues;

    @Schema(description = "key 属性值 value 属性是否上下架 1 上架 0 下架")
    private List<Map<String, Integer>> flavorValueListMap = new ArrayList<>();

}
