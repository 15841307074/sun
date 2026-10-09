package com.htyoudao.youdao.module.system.controller.admin.permission.vo.role;


import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织机构/门店添加负责人弹窗分页 Request VO")
@Data
public class RoleUserPageReqVO extends PageParam {

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "电话号", example = "131111111")
    private String mobile;

    @Schema(description = "组织id", example = "123456")
    private Long orgId;

    @NotNull
    @Schema(description = "角色 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long roleId;

    @Schema(description = "选中的组织id 0是查询无组织用户 不传查全部的用户", example = "123456")
    private Long checkedOrgId;
}
