package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "管理后台 - 通过部门获取用户弹窗列表 Request VO")
public class DeptUserPageRespVO {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "显示名")
    private String nickname;

    @Schema(description = "电话号")
    private String mobile;

    @Schema(description = "角色")
    private String roleNames;
}
