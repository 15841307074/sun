package com.htyoudao.youdao.module.system.controller.admin.store.vo.background;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreBackgroundStatusReqVO {

    @NotNull(message = "模板编号不能为空")
    private Long backgroundId;

    @NotNull(message = "发布状态不能为空")
    @Schema(description = "发布状态：0关闭，1发布")
    private Integer publishStatus;
}
