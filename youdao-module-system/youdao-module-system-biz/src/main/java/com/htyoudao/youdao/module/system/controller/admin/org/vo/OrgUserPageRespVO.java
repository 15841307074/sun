package com.htyoudao.youdao.module.system.controller.admin.org.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构树 Request VO")
@Data
@ToString(callSuper = true)
public class OrgUserPageRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private String username;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "6862")
    private String nickname;

    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String orgNames;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String roleNames;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Long parentId;

    @Schema(description = "上级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Long orgUserId;

    @Schema(description = "1是负责人 0是普通人", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer type;

    @Schema(description = "门店是否可见 0不可见 1可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "19880")
    private Integer visible;

    @Schema(description = "电话")
    private String mobile;
}
