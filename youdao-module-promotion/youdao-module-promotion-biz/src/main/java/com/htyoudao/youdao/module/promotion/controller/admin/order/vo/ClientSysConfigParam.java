package com.htyoudao.youdao.module.promotion.controller.admin.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSysConfigParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 8059654987992959230L;
    /**
     * 参数主键
     */
    private Long configId;

    /**
     * 参数名称
     */
    private String configName;

    /**
     * 参数键名
     */
    private String configKey;

    /**
     * 参数键值
     */
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    private String configType;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    public ClientSysConfigParam(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public ClientSysConfigParam setConfigName(String configName) {
        this.configName = configName;
        return this;
    }

    public ClientSysConfigParam setConfigKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public String getConfigValue() {
        return configValue;
    }

    public ClientSysConfigParam setConfigValue(String configValue) {
        this.configValue = configValue;
        return this;
    }

}
