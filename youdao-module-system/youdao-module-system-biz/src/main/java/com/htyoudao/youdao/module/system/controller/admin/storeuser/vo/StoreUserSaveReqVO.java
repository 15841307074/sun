package com.htyoudao.youdao.module.system.controller.admin.storeuser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 门店和用户关联新增/修改 Request VO")
@Data
public class StoreUserSaveReqVO {

    @Schema(description = "自增编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4365")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26974")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6813")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "类型 1. 店长 ", example = "1")
    private Integer type;

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24728")
    @NotNull(message = "项目 id不能为空")
    private Long businessId;

}