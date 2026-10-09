package com.htyoudao.youdao.module.system.controller.admin.wxstore.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "企业微信 - 商家企业微信绑定表 Request VO")
@Data
public class StoreWecomImgReqVO {

    @Schema(description = "店长社群背景图")
    @DiffLogField(name = "店长社群背景图")
    private String communityManagerImg;

    @Schema(description = "门店社群背景图")
    @DiffLogField(name = "门店社群背景图")
    private String communityStoreImg;
}
