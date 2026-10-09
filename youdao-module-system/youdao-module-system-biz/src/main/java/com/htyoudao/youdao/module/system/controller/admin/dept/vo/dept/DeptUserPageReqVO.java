package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 33483
 */
@Data
@Schema(description = "管理后台 - 通过部门获取用户弹窗列表 Request VO")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeptUserPageReqVO extends PageParam {

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "用户名/显示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private String nickname;

    @Schema(description = "电话号", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private String mobile;
}
