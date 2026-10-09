package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "供应链商品详情请求 VO")
public class ScmCommodityInfoReqVO extends PageParam {

    @Schema(description = "仓库 ID")
    private Long warehouseId;

    @Schema(description = "商品编号")
    private String commodityCode;

    @Schema(description = "二级目录名称")
    private String stasticsName;


    @Schema(description = "门店 ID")
    private Long storeId;
}
