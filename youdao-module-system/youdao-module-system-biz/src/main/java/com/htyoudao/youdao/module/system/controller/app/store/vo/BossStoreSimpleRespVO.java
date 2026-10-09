package com.htyoudao.youdao.module.system.controller.app.store.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 老板助手门店精简列表，兼容 id/name 和 storeId/storeName 两种字段名。 */
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "老板助手门店精简信息")
public class BossStoreSimpleRespVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "供应链门店使用状态：1正常、2停用，可为空")
    private Integer useStatus;

    @Schema(description = "门店ID，与storeId相同")
    public Long getId() {
        return storeId;
    }

    @Schema(description = "门店名称，与storeName相同")
    public String getName() {
        return storeName;
    }
}
