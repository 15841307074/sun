package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "填写收货地址")
public class AddressSaveReqVO {

    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID，不传时后端从 token 获取")
    private Long memberId;

//    @Schema(description = "收件人")
//    private String receiveUser;
//
//    @Schema(description = "收件联系方式")
//    private String receiveMobile;

    @Schema(description = "收货人姓名")
    @NotBlank(message = "收货人姓名不能为空")
    private String memberNickName;

    @Schema(description = "收货人手机号")
    @NotBlank(message = "收货人手机号不能为空")
    private String memberMobile;

    @Schema(description = "收件地址")
    private String receiveAddress;
}
