package com.htyoudao.youdao.module.commodity.controller.admin.sync.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommodityTemplateToStoreSyncReqVO {

    /**
     * 同步到的门店ID集合
     */
    @NotEmpty(message = "没有门店信息")
    @Schema(description = "同步到的门店ID集合 key:id value:name")
    private Map<Long, String> storeIds;

    @NotNull(message = "模板id不能为空")
    @Schema(description = "模板id")
    private Long commodityTemplateId;

}
