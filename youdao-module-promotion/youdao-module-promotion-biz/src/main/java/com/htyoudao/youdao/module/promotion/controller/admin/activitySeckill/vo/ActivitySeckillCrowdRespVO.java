package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "返回人群对象集合")
public class ActivitySeckillCrowdRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "人群名称")
    private String crowdName;
}
