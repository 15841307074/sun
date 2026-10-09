package com.htyoudao.youdao.framework.common.enums;

import lombok.Getter;

public enum PointsTypeEnum {
    POINTS_ACQUISITION(1,"积分获取"),
    INTEGRAL_CONSUMPTION(2,"兑换消耗"),
    EXPIRE(3,"过期积分");

    PointsTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Getter
    private int code;

    @Getter
    private String msg;

    public static String getMsgByCode(int code){
        PointsTypeEnum[] values = PointsTypeEnum.values();
        for (PointsTypeEnum value : values) {
            if (value.getCode()==code){
                return value.getMsg();
            }
        }
        return null;
    }
}
