package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群下拉")
@Data
public class CustomCrowdDropDownRespVO {

    @Schema(description = "人群id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "人群名称")
    private String crowdName;
}
