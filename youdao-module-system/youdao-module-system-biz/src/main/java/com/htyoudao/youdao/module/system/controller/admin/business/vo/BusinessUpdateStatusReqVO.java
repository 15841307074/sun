package com.htyoudao.youdao.module.system.controller.admin.business.vo;

import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 项目更新状态 Request VO")
@Data
public class BusinessUpdateStatusReqVO {

    @Schema(description = "项目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目 id不能为空")
    private Long id;

    @Schema(description = "状态，见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;

}
