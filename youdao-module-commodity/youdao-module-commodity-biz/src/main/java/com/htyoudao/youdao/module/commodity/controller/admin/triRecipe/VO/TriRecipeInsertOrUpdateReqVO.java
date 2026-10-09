package com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriRecipeInsertOrUpdateReqVO extends PageParam {

    @Schema(description = "商品名称")
    @NotEmpty(message = "渠道不能为空")
    private String triCommodityName;

    @Schema(description = "三方渠道类型")
    @NotEmpty(message = "渠道不能为空")
    private String triType;

    @Schema(description = "是否设置配方")
    @NotNull(message = "渠道不能为空")
    private Integer recipeState;

}
