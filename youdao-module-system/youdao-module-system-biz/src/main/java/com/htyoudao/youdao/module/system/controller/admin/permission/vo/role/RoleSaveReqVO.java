package com.htyoudao.youdao.module.system.controller.admin.permission.vo.role;

import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.system.framework.operatelog.core.SexParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 角色创建/更新 Request VO")
@Data
public class RoleSaveReqVO {

    @Schema(description = "角色编号", example = "1")
    private Long id;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过 30 个字符")
    @DiffLogField(name = "角色名称")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @DiffLogField(name = "状态", function = SexParseFunction.NAME)
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "备注", example = "我是一个角色")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
