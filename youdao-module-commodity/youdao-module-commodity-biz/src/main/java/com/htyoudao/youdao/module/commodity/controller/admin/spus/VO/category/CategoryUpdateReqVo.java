package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category;

import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品分类修改 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateReqVo extends TimeBase {


    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long id;

    @Schema(description = "类型 是否下单必选分组 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品分类不能为空")
    private String name;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "顺序不能为空")
    private Integer sort;

    @Schema(description = "分类状态 0:启用，1:禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "是否隐藏 1是 0否")
    private Integer isHidden;

    @Schema(description = "图片地址")
    private String url;

    @Schema(description = "字典键值")
    private String dictValue;


    @Schema(description = "分类里记录所有分类下商品的 ids")
    private String commoditySpusIds;


}
