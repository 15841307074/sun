package com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 组织路径节点。 */
@Data
public class StoreOrgPathNodeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sort;
}
