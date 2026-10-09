package com.htyoudao.youdao.module.system.controller.admin.wxstore.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "企业微信 - 商家企业微信绑定表 Request VO")
@Data
public class StoreWecomConfigReqVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "企微二维码")
    @DiffLogField(name = "企微二维码")
    private String qrCode;

    @Schema(description = "企微社群二维码")
    @DiffLogField(name = "企微社群二维码")
    private String qrCommunityCode;

    @Schema(description = "门店经度")
    private double longitude;

    @Schema(description = "门店维度")
    private double latitude;

    @Schema(description = "企微二维码类型  0 福利官 1社群")
    @DiffLogField(name = "企微二维码类型")
    private Integer qrType;
    @Schema(description = "店长社群背景图")
    @DiffLogField(name = "店长社群背景图")
    private String communityManagerImg;

    @Schema(description = "门店社群背景图")
    @DiffLogField(name = "门店社群背景图")
    private String communityStoreImg;
}
