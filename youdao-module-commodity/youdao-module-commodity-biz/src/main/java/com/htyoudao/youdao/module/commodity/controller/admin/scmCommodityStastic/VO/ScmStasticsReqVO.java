package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询二级目录请求参数对象
 */
@Schema(description = "查询二级目录请求参数对象")
@Data
public class ScmStasticsReqVO {

    @Schema(description = "二级目录名称名称")
    private String stasticsName;
    @Schema(description = "仓库 ID")
    private Long warehouseId;
}
