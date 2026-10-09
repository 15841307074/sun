package com.htyoudao.youdao.module.member.api.crowd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dht
 */
@Data
public class CrowdNameDTO implements Serializable {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21898")
    private Long id;

    @Schema(description = "人群名称")
    private String crowdName;
}
