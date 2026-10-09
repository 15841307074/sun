package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 批量修改 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityBatchEditReqVo extends TimeBase {


    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    @Schema(description = "批量规则 1上架 2下架 3删除 4移动分组 5改可售时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "批量规则不能为空")
    private Integer sortingRules;


    @Schema(description = "商品ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> commodityIds = new ArrayList<>();

    @Schema(description = "分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;

}
