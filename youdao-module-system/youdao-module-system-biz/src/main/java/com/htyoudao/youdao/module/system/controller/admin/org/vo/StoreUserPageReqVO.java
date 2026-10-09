package com.htyoudao.youdao.module.system.controller.admin.org.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 门店人员查询 Request VO")
@Data
public class StoreUserPageReqVO extends PageParam {

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private String nickname;
}
