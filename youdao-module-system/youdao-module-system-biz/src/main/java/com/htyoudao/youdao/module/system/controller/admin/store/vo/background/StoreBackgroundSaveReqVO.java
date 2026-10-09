package com.htyoudao.youdao.module.system.controller.admin.store.vo.background;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StoreBackgroundSaveReqVO {

    @Schema(description = "模板编号，修改时必传")
    private Long backgroundId;

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "门店背景图片不能为空")
    private String backgroundImage;

    @Schema(description = "应用范围：1按门店，2按标签")
    private Integer appScope;

    @Schema(description = "门店范围：1全部门店，2部分门店")
    private Integer storeScope;

    @Schema(description = "门店编号，多选")
    private List<Long> storeIds;

    @Schema(description = "标签编号，多选，任意标签命中")
    private List<Long> tagIds;
}
