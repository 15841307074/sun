package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityCqUpdateExpressReqVO {

    /**
     * 记录id
     */

    @Schema(description = "记录id")
    @NotNull
    private Long id;

    /**
     * 奖品状态
     */

    @Schema(description = "奖品状态")
    private Integer prizeState;

    /**
     * 收件人
     */

    @Schema(description = "收件人")
    private String receiveUser;

    /**
     * 收件联系方式
     */

    @Schema(description = "收件联系方式")
    private String receiveMobile;

    /**
     * 收件地址
     */

    @Schema(description = "收件地址")
    private String receiveAddress;

    /**
     * 快递单号
     */

    @Schema(description = "快递单号")
    private String trackingNumber;

    /**
     * 快递公司
     */

    @Schema(description = "快递公司")
    private String expressCompany;
}