package com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "查询二级目录返回 VO")
@Data
public class ScmCommodityStasticRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "目录名称")
    private String stasticsName;

    @Schema(description = "数量")
    private Integer num;

}
