package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 收货地址保存请求VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "收货地址保存请求")
public class AddressSaveReqVO {

    @Schema(description = "兑换记录ID", required = true)
    @NotNull(message = "兑换记录ID不能为空")
    private Long id;

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @Schema(description = "收货人姓名", required = true)
    @NotBlank(message = "收货人姓名不能为空")
    private String memberNickName;

    @Schema(description = "收货人手机号", required = true)
    @NotBlank(message = "收货人手机号不能为空")
    private String memberMobile;
    @Schema(name = "receiveAddress", description = "收货地址")
    @NotBlank(message = "收货地址不能为空")
    private String receiveAddress;

}
