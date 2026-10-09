package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PointsLogEditReqVO {


    @Schema(description = "积分商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotNull(message = "积分商品记录Id不能为空")
    private Long productId;

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsLogId;

    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;

    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 0, max = 100, message = "收货地址长度不能超过100")
    private String receiveAddress;

    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;


    @Schema(description = "快递公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;


}
