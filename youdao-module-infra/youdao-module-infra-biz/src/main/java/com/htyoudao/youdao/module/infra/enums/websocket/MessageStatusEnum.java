package com.htyoudao.youdao.module.infra.enums.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * details
 *
 * @author liuzhaowang
 */
@Getter
@AllArgsConstructor
public enum MessageStatusEnum {
    /**
     * 等待推送(未送达)
     */
    PENDING(0, "等待推送"),
    /**
     * 已送达(未读)
     */
    DELIVERED(1, "已送达"),
    /**
     * 撤回
     */
    REVOKE(2, "撤回"),
    /**
     * 已读
     */
    READ(3, "已读");

    private final Integer code;

    private final String desc;

}
