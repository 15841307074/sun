package com.htyoudao.youdao.module.system.controller.app.deptorg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 老板助手全部人员 + 部门 Response VO")
public class UserDeptInfoPageRespVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "1")
    private String username;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "1")
    private String nickname;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "1")
    private String mobile;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "1")
    private String deptNames;
}
