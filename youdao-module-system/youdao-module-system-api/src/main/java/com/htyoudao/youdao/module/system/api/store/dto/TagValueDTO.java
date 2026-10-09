package com.htyoudao.youdao.module.system.api.store.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lbw
 * 门店标签数据
 */
@Data
public class TagValueDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4070844747636080688L;

    /**
     * 主键
     */
    private Long id;
    /**
     * 标签名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签组id
     */
    private Long tagGroupId;
}
