package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 积分分页参数 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsLogExportReqVo {


    @Schema(description = "积分类型(1-获取,2-兑换商品消耗,3-过期,4-冻结)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsType;


    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品 ", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;


    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String trackingNumber;

    @Schema(description = "物流单号(0否,1是)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String isNumber;


    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberMobile;


    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logCode;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;


}
