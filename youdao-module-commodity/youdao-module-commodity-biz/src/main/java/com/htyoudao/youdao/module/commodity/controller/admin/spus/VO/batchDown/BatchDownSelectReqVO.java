package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchDownSelectReqVO {

    @Schema(description = "1wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不能为空")
    private Integer chooseView;

    @Schema(description = "单品 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> singleIds = new ArrayList<>();

    @Schema(description = "套餐 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> packageIds = new ArrayList<>();

}
