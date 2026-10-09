package com.htyoudao.youdao.module.commodity.enums;


import lombok.Getter;

/**
 * 调拨状态
 */
@Getter
public enum TransferStatus {

    NOT_RECEIVED(0, "未接收"),
//    /**
//     * 调出未接收 - 调出方已发起调拨，但调入方尚未接收
//     */
//    OUTBOUND_NOT_RECEIVED(1, "调出未接收"),
//
//    /**
//     * 调入未接收 - 调出方已发货，调入方已收到但尚未确认接收
//     */
//    INBOUND_NOT_RECEIVED(2, "调入未接收"),

    /**
     * 已取消 - 调拨流程已被取消
     */
    CANCELLED(3, "已取消"),

    /**
     * 调拨完成 - 调拨流程已完成，调入方已确认接收
     */
    COMPLETED(4, "调拨完成");

    private final Integer code;
    private final String description;

    TransferStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     * @param code 状态代码
     * @return 对应的枚举值，如果找不到返回null
     */
    public static TransferStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransferStatus status : TransferStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取状态代码
     * @return 状态代码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取状态描述
     * @return 状态描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为指定代码
     * @param code 要检查的代码
     * @return 如果匹配返回true，否则返回false
     */
    public boolean isCode(Integer code) {
        if (code == null) {
            return false;
        }
        return this.code.equals(code);
    }

    @Override
    public String toString() {
        return this.description;
    }

}
