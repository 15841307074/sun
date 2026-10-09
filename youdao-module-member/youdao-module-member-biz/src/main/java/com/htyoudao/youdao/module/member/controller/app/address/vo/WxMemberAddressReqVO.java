package com.htyoudao.youdao.module.member.controller.app.address.vo;

import com.htyoudao.youdao.framework.common.enums.WxAddressTypeEnum;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "地址管理 - 个人收货地址信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxMemberAddressReqVO implements Serializable {

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @Schema(description = "是否默认", requiredMode = Schema.RequiredMode.REQUIRED)
    @InEnum(value = WxAddressTypeEnum.class, message = "收货地址状态必须是 {value}")
    int defaultAddress;

    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货人姓名不能为空")
    private String receiverName;

    @Schema(description = "收货人性别", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotNull(message = "收货人性别不能为空")
    private Integer receiverGender;

    @Schema(description = "收货地址标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址标签不能为空")
    private String addressLabel;

    @Schema(description = "收货人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货人电话不能为空")
    @Size(max = 11, message = "联系电话长度不能超过11个字符")
    private String receiverPhone;

    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址不能为空")
    private String address;

    @Schema(description = "收货地址经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址经度不能为空")
    private BigDecimal longitude;

    @Schema(description = "收货地址纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址纬度不能为空")
    private BigDecimal latitude;

    @Schema(description = "收货详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货详细地址不能为空")
    private String addressDetail;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "城市不能为空")
    private String cityName;
}
