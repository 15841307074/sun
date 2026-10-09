package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdEventType {
    AD_EXPOSURE("广告曝光","ad_exposure"),
    AD_CLICK("广告点击","ad_click"),
    AD_LEAVE("广告离开","ad_leave"),
    ;
    private final String name;
    private final String code;

    /**
     * 根据code查找广告事件类型
     *
     * @param code 事件编码
     * @return 匹配的广告事件类型，未找到返回null
     */
    public static AdEventType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AdEventType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为广告事件
     *
     * @param code 事件编码
     * @return true表示属于广告事件
     */
    public static boolean isAdEvent(String code) {
        return getByCode(code) != null;
    }
}
