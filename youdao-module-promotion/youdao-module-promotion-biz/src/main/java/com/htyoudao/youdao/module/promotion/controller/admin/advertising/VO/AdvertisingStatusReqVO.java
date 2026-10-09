package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdvertisingStatusReqVO {


    @NotBlank(message = "ID不能为空")
    private Long id;


//    @Schema(description = "广告位置(1.首页弹窗 2.首页顶部轮播 3.首页底部轮播 4.首页底部活动 5.点餐页弹窗 6.点餐页顶部轮播 7.点餐页分组轮播 8.结算页轮播 9.订单详情页轮播 10.订单列表轮播 11.开屏广告 12.浮窗)", requiredMode = Schema.RequiredMode.REQUIRED)
//    private Integer adInfoPosition;

    @Schema(description = "系统判定是否启用(1 启用 2禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isOpen;


}
