package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "修改状态 VO")
@Data
public class ModifyStatusVO {

    @Schema(description = "ID")
    @NotNull(message = "ID 不能为空")
    private Long id;

    /**
     * 停用状态(0停用 1启用)
     */
    @Schema(description = "停用状态(0停用 1启用)")
    @NotNull(message = "停用状态(0停用 1启用) 不能为空")
    private Integer isEnable;
}
