package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动发放记录填写/修改快递单号 Request VO")
@Data
public class ActivitySignUpdateExpressReqVO {

    @Schema(description = "发放记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "发放记录ID不能为空")
    private Long id;

    @Schema(description = "实物奖品状态：2待发货 3待收货 9超时未填写；填写快递单号时通常传3", example = "3")
    private Integer prizeState;

    @Schema(description = "收件人")
    private String receiveUser;

    @Schema(description = "收件联系方式")
    private String receiveMobile;

    @Schema(description = "收件地址")
    private String receiveAddress;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "快递公司")
    private String expressCompany;
}
