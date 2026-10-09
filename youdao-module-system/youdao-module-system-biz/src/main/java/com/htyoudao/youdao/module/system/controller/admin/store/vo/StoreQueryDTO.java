package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "门店查询请求参数")
public class StoreQueryDTO {

    @Schema(description = "门店名称（模糊查询）", example = "万达")
    private String storeName;

    @Schema(description = "门店ID列表", example = "[1, 2, 3]")
    private List<Long> storeIds;
}
