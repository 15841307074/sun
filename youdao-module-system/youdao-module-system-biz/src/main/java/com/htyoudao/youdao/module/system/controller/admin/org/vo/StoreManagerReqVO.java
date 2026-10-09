package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 设置/撤销门店店长,设置门店可见/不可见 Request VO")
@Data
@ToString(callSuper = true)
public class StoreManagerReqVO {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户id不能为空")
    private Long userId;
}
