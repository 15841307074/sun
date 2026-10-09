package com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "小程序 - 模版关联标签 Response VO")
@Data
public class TemplateTagRespVO {

    private Long tagId;


    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tagName;

}
