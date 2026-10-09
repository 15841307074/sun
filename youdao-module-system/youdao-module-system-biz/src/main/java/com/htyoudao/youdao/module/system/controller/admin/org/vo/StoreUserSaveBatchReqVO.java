package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 门店批量添加人 Request VO")
@Data
public class StoreUserSaveBatchReqVO {

    @Schema(description = "门店id", required = true, example = "1024")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "人员id", required = true, example = "1024")
    @NotNull(message = "人员id不能为空")
    private List<Long> userIds;
}
