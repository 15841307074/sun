package com.htyoudao.youdao.module.promotion.service.douyin.constants;

public enum CertificateStatus {
    INIT(0, "初始状态"),
    AVAILABLE(1, "未使用"),
    USED(2, "已使用"),
    REFUND_APPLYING(3, "退款申请中(待审核)"),
    REFUND_SUCCESS(4, "退款成功"),
    REFUND_FAILED(5, "退款失败"),
    REFUNDING(6, "退款中"),
    USING(10, "使用中（周期/储值卡激活）");

    private final int code;
    private final String description;

    CertificateStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 可选：通过状态码获取枚举实例的方法
    public static CertificateStatus fromCode(int code) {
        for (CertificateStatus status : CertificateStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }

    // 可选：通过状态码获取描述信息的方法
    public static String getDescriptionByCode(int code) {
        return fromCode(code).getDescription();
    }
}