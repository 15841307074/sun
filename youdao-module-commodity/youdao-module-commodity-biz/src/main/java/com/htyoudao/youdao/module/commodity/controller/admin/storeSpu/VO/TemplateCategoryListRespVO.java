package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 模版分类集合 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCategoryListRespVO extends TimeBase {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateId;

    @Schema(description = "分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long  categoryId;

    @Schema(description = "（原：类型   1 菜品分类 2 套餐分类）（分组属性 现：是否下单必选分组 1 是 0否）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "分类状态 0:禁用，1:启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @Schema(description = "字典键值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;


    @Schema(description = "分类里记录所有分类下商品的 ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityIds;

}
