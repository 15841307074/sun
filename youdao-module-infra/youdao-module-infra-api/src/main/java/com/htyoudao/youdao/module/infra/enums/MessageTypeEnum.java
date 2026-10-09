package com.htyoudao.youdao.module.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    /**
     * 心跳
     */
    HEART_BEAT("hb"),
    /**
     * 强制下线
     */
    FORCE_LOGOUT("fl"),
    /**
     * 私聊消息
     */
    PRIVATE_MESSAGE("pm"),
    /**
     * 群发消息
     */
    GROUP_MESSAGE("gm"),
    /**
     * 系统消息
     */
    SYSTEM_MESSAGE("sm");

    private final String type;

    public static MessageTypeEnum fromType(String type) {
        for (MessageTypeEnum typeEnum : values()) {
            if (typeEnum.type.equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

}
