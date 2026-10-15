package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 推送状态枚举
 *
 * <p>对应 {@code analysis_platform_message.push_status} 字段。
 * 这个字段是补偿机制的唯一入口——没有它就没法区分「还没推」和「推失败了」。
 */
@Getter
@AllArgsConstructor
public enum PushStatusEnum {

    /**
     * 待推送：消息已落库，还没调云函数
     */
    PENDING(0, "待推送"),

    /**
     * 推送成功：云函数返回 errCode == 0
     */
    SUCCESS(1, "推送成功"),

    /**
     * 推送失败：云函数返回错误 / 超时 / 异常。补偿重推时捞这个状态
     */
    FAILURE(2, "推送失败"),

    /**
     * 无需推送：该用户查不到设备号（CID），不调云函数
     */
    NO_DEVICE(3, "无需推送");

    /**
     * 状态值，存入数据库的 TINYINT
     */
    private final Integer status;

    /**
     * 描述，仅用于日志 / 展示
     */
    private final String description;

    /**
     * 按 status 值反查枚举，找不到返回 null
     */
    public static PushStatusEnum valueOfStatus(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }
}
