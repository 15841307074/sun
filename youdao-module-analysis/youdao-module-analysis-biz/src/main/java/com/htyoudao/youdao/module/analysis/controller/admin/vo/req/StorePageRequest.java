package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class StorePageRequest extends AggregationPageRequest {

    @NotNull
    @Schema(description = "组织集合")
    private List<Long> orgIds;

    @Schema(description = "组织门店集合")
    private List<List<Long>> orgStoreIds;

    @NotNull
    @Schema(description = "组织名称")
    private List<String> orgNames;

}
