package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券分页 Request VO")
@Data
@ToString(callSuper = true)
public class ShareDetailReqVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "id")
    private String pageUrl;

    @Schema(description = "id")
    private String pageTitle;

    @Schema(description = "id")
    private Boolean isPermanent;

    @Schema(description = "id")
    private String query;
}
