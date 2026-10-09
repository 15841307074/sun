package com.htyoudao.youdao.module.commodity.controller.admin.sync.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Schema(description = "门店商品缓存重置 Request VO")
@Data
@ToString(callSuper = true)
public class CommodityCacheReloadReqVO {

    @Schema(description = "项目ID")
    @NotNull
    private Long businessId;

    @Schema(description = "门店id 集合")
    private List<Long> storeIds;
}
