package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 广告查询 Request VO")
@Data
public class AdvertisingPageReqVO extends PageParam {



    @Schema(description = "广告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adName;


    @Schema(description = "类型（1.首页 2.点餐 3.订单及结算 4.其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionType;
    @Schema(description = "广告详细位置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition; //广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.首页活动 5.点餐页弹屏 6.点餐页轮播图 7.点餐页全局轮播 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏 12 浮窗 13 订单详情弹窗 14 点餐页分组轮播）


    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orgId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;


    @Schema(description = "系统判定是否启用(1 启用 2禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> isOpen;
}
