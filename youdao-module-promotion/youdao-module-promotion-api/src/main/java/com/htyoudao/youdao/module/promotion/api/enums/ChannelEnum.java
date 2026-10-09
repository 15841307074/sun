package com.htyoudao.youdao.module.promotion.api.enums;

/**
 * 渠道枚举类
 */
public enum ChannelEnum {

    COMMUNITY_SHARE(1_000_000_001L, "community_share", "社群分享"),
    USER_MINI_PROGRAM(1_000_000_002L, "user_mini_program", "用户小程序"),
    ORDERING_MACHINE(1_000_000_003L, "ordering_machine", "点餐机"),
    CUSTOM_PROMOTION(1_000_000_004L, "custom_promotion", "自定义推广渠道");

    private final Long id;
    private final String code;
    private final String description;

    ChannelEnum(Long id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据ID获取枚举
     */
    public static ChannelEnum getById(int id) {
        for (ChannelEnum channel : ChannelEnum.values()) {
            if (channel.getId() == id) {
                return channel;
            }
        }
        return null;
    }

    /**
     * 根据code获取枚举
     */
    public static ChannelEnum getByCode(String code) {
        for (ChannelEnum channel : ChannelEnum.values()) {
            if (channel.getCode().equals(code)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.code;
    }
}