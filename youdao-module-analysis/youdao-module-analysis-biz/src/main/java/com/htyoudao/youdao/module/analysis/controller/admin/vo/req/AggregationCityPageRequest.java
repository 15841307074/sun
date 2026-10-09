package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class AggregationCityPageRequest extends PageParam {

    @Schema(description = "基础参数")
    private AggregationRequestVO baseParam;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "排序字段")
    private String orderField;

    @Schema(description = "排序方式 asc desc")
    private String orderType;
}
