package com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 自定义人群分页 Request VO")
@Data
public class CustomCrowdPageReqVO extends PageParam {

    @Schema(description = "人群名称")
    private String crowdName;

}