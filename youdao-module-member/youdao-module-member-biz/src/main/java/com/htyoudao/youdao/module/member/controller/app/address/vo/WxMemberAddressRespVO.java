package com.htyoudao.youdao.module.member.controller.app.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "个人中心地址管理 - 用户收货地址 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxMemberAddressRespVO implements Serializable {

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "用户ID")
    private Long memberId;

    @Schema(description = "是否默认")
    int defaultAddress;

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人性别：0、男；1、女")
    private Integer receiverGender;

    @Schema(description = "收货地址标签")
    private String addressLabel;

    @Schema(description = "收货人电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "收货地址经度")
    private BigDecimal longitude;

    @Schema(description = "收货地址纬度")
    private BigDecimal latitude;

    @Schema(description = "收货详细地址")
    private String addressDetail;

    @Schema(description = "城市")
    private String cityName;
}
