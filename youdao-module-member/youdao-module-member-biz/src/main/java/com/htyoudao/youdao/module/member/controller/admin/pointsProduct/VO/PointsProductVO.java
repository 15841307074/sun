package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PointsProductVO {

    @Schema(description = "积分商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;


    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;




}
