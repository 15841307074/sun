package com.htyoudao.youdao.module.analysis.api.inventory.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AggregationPageRequest extends AggregationRequestVO implements Serializable {

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "排序字段")
    private String orderField;

    @Schema(description = "排序方式 asc desc")
    private String orderType;

    @NotNull
    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer pageNo;

    @NotNull
    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pageSize;
}
