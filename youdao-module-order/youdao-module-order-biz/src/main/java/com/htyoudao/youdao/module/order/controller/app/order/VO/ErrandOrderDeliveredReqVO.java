package com.htyoudao.youdao.module.order.controller.app.order.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "跑腿订单送达请求 VO")
public class ErrandOrderDeliveredReqVO {

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202606041000000001")
    @NotBlank(message = "订单号不能为空")
    private String orderSn;

    @Schema(description = "送达照片 URL 列表，最多3张", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://img.example.com/a.jpg\"]")
    @NotEmpty(message = "送达照片不能为空")
    @Size(max = 3, message = "送达照片最多上传3张")
    private List<String> deliveryImages;
}
