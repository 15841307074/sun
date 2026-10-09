package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RoleUserReqVo {
    @Schema(description = "项目id", example = "项目id")
    private Long businessId;
    @Schema(description = "角色id", example = "角色id")
    private List<Long> roleId;
}
