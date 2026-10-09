package com.htyoudao.youdao.module.system.api.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Schema(description = "RPC 服务 - 部门的数据权限 Response DTO")
@Data
public class DeptDataPermissionRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4305287308060416508L;

    @Schema(description = "是否可查看全部数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean all;

    @Schema(description = "项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Long businessId;

    @Schema(description = "可查看的组织id数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 3]")
    Set<Long> orgIds;

    @Schema(description = "可查看的门店id数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 3]")
    Set<Long> storeIds;


    public DeptDataPermissionRespDTO() {
        this.all = false;
        this.storeIds = new HashSet<>();
        this.orgIds = new HashSet<>();
    }

}
