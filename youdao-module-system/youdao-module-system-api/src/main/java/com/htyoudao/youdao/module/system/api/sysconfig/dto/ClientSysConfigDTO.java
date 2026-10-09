package com.htyoudao.youdao.module.system.api.sysconfig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 参数配置表 sys_config
 *
 * @author zhangjihe
 */
@Schema(description = "RPC 服务 - 参数配置表 sys_config DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSysConfigDTO implements Serializable {

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

}
