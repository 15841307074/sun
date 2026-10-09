package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "供应链商品分页请求 VO")
public class ScmCommodityPageReqVO extends PageParam {

    @Schema(description = "仓库 ID")
    private Long warehouseId;

    @Schema(description = "商品名称/编号")
    private String commodityName;

    @Schema(description = "二级目录名称")
    @NotNull(message = "二级目录名称 不能为空")
    private String stasticsName;
}
