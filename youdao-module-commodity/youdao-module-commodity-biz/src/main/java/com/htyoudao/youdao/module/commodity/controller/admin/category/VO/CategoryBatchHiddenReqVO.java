package com.htyoudao.youdao.module.commodity.controller.admin.category.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 商品分类批量隐藏 Request VO")
@Data
public class CategoryBatchHiddenReqVO {

    @Schema(description = "分类ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分类ID不能为空")
    private List<Long> categoryIds = new ArrayList<>();

    @Schema(description = "是否隐藏 1是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "隐藏状态不能为空")
    @Min(value = 0, message = "隐藏状态只能为0或1")
    @Max(value = 1, message = "隐藏状态只能为0或1")
    private Integer isHidden;
}
