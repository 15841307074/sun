package com.htyoudao.youdao.module.promotion.controller.app.advertising.VO;


import com.baomidou.mybatisplus.annotation.TableField;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AdvertisingConfigReqVo {

    @Schema(description = "类型（1.首页 2.点餐 3.订单及结算 4.其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionType;
    @Schema(description = "广告详细位置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition; //广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.首页活动 5.点餐页弹屏 6.点餐页轮播图 7.点餐页全局轮播 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏 12 浮窗 13 订单详情弹窗 14 点餐页分组轮播）

    @Schema(description = "广告图片列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvertisingImageDO> imageDOList = new ArrayList<>();


    @Schema(description = "系统当前时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date CurrentTime;

    @TableField(exist = false)
    @Schema(description = "展示时长", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;







}
