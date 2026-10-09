package com.htyoudao.youdao.module.order.controller.app.order.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "跑腿订单列表响应 VO，用于接单大厅和我的配送订单")
public class ErrandOrderHallRespVO {

    @Schema(description = "订单号", example = "ORD202606041000000001")
    private String orderSn;

    @Schema(description = "下单用户头像", example = "https://example.com/avatar.jpg")
    private String memberAvatar;

    @Schema(description = "配送时间，对应订单 appointmentTime", example = "2026-06-04 12:30:00")
    private String appointmentTime;

    @Schema(description = "订单状态：110待接单 120已接单 50配送中 60已完成 70已退款", example = "110")
    private Integer orderState;

    @Schema(description = "取餐地址，对应 receiverAreaInfo；未满足跑腿员资格时返回 ****", example = "第一食堂一楼3号窗口")
    private String receiverAreaInfo;

    @Schema(description = "送餐地址，对应 receiverAddress；未满足跑腿员资格时返回 ****", example = "8号宿舍楼 302")
    private String receiverAddress;

    @Schema(description = "订单备注", example = "到楼下电话联系")
    private String orderRemark;

    @Schema(description = "赏金，用户支付赏金 + 门店补贴赏金", example = "4.00")
    private BigDecimal rewardAmount;

    @Schema(description = "用户支付跑腿赏金", example = "3.00")
    private BigDecimal errandRewardAmount;

    @Schema(description = "门店跑腿补贴金额", example = "1.00")
    private BigDecimal errandStoreSubsidyAmount;

    @Schema(description = "跑腿员姓名", example = "张三")
    private String deliveryName;

    @Schema(description = "跑腿员电话", example = "13800000000")
    private String deliveryPhone;

    @Schema(description = "点餐人电话")
    private String takeAwayTel;

    @Schema(description = "收货人电话")
    private String receiverMobile;

    @Schema(description = "门店电话")
    private String storePhone;

    @Schema(description = "商品数量")
    private Integer goodsNum;
}
