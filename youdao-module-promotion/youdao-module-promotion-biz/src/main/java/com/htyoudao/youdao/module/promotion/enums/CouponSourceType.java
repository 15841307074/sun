package com.htyoudao.youdao.module.promotion.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;

/**
 * @author dht
 * 优惠券领取方式枚举
 */
public enum CouponSourceType implements ArrayValuable<Integer> {
    MINI_PROGRAM_LINK(0, "小程序链接领券"),
    H5_LINK(1, "H5链接领券"),
    SMS_LINK(2, "短信链接领券"),
    MINI_PROGRAM_BANNER(3, "小程序内部领券(banner)"),
    MINI_PROGRAM_POPUP(4, "小程序内部领券(弹窗)"),
    MINI_PROGRAM_SHARE(5, "小程序内部领券(分享)"),
    PROMOTION(6, "推广"),
    POINTS_MALL(7, "积分商城"),
    PRIZE_DRAW(8, "抽奖"),
    TIKTOK(9, "douyin"),
    AUTOMATIC_DISTRIBUTION(10,"自动发放"),
    SECKILL(11,"秒杀"),
    COLLECT_POINTS(12,"集点兑换"),
    DRAW_LOTS(13,"抽签"),
    COLLECT_CARD(14,"集卡兑换"),
    SIGN_ACTIVITY(15,"签到活动"),
    SURVEY_REWARD(16,"问卷奖励"),
    ANSWER(17,"有奖问答"),
    VOTE_REWARD(18, "投票")
    ;


    private final Integer code;
    private final String description;

    CouponSourceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static CouponSourceType getByCode(int code) {
        for (CouponSourceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据description获取枚举
     */
    public static CouponSourceType getByDescription(String description) {
        for (CouponSourceType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        if (code != null) {
            for (CouponSourceType type : values()) {
                if (type.code == code) {
                    return type.description;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public Integer[] array() {
        return new Integer[0];
    }
}