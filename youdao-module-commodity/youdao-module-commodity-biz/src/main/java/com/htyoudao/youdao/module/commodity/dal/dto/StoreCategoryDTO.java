package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
public class StoreCategoryDTO implements Serializable {

    @Schema(description = "分类信息")
    private CategoryDto category;

    @Schema(description = "分类下spu列表")
    private List<SpuDto> items = new ArrayList<>();

}
