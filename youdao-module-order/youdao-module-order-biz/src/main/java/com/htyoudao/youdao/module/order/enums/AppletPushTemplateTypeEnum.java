package com.htyoudao.youdao.module.order.enums;

import lombok.Getter;

/**
 * 小程序模板推送通知类型
 */
public enum AppletPushTemplateTypeEnum {
    PLACE_ORDER_NOTICE(1,"取餐通知"),
    PLACE_ACTIVITY_BEGIN(2,"活动开始提醒"),
    PLACE_COUPON_RECEIVE(3,"优惠券到账通知"),
    PLACE_POINTS_EXPIRE(4,"积分到期提醒"),
    PLACE_COUPON_EXPIRE(5,"优惠券到期提醒"),
    PLACE_ORDER_SUCCESS(6,"下单成功通知"),
    PLACE_COMPLAINT_DEAL(7,"下单成功通知");
    AppletPushTemplateTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    @Getter
    private Integer code;
    @Getter
    private String msg;
    public static String getMsgByCode(int code){
        AppletPushTemplateTypeEnum[] values = AppletPushTemplateTypeEnum.values();
        for (AppletPushTemplateTypeEnum value : values) {
            if (value.getCode().equals(code)){
                return value.getMsg();
            }
        }
        return null;
    }
}
