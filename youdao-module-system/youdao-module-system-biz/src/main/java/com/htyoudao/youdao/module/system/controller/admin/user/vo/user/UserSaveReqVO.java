package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.validation.Mobile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.module.system.framework.operatelog.core.SexParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 用户创建/修改 Request VO")
@Data
public class UserSaveReqVO {

    @Schema(description = "id", example = "1024")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9]+$", message = "用户名由 中文、数字、字母 组成")
    @Length(min = 2, max = 20, message = "用户名长度为 2-20 位")
    @DiffLogField(name = "用户名")
    private String username;

    @Schema(description = "" +
            "", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    @DiffLogField(name = "用户昵称")
    private String nickname;
    @Schema(description = "" +
            "", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @Size(max = 30, message = "供应链显示名不能超过30个字符")
    @DiffLogField(name = "供应链显示名")
    private String supplyName;


    @Schema(description = "手机号码", example = "15601691300")
    @Mobile
    @DiffLogField(name = "手机号码")
    private String mobile;


    @Schema(description = "用户性别，参见 SexEnum 枚举类", example = "1")
    @DiffLogField(name = "用户性别", function = SexParseFunction.NAME)
    private Integer sex;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    @DiffLogField(name = "用户头像")
    private String avatar;


    // ========== 仅【创建】时，需要传递的字段 ==========

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "用户名由 数字、字母 组成")
    @Size(min = 4, max = 20, message = "密码名长度为 4-30 个字符")
    private String password;


    @Schema(description = "组织", example = "1024")
    private Long orgId;

    @Schema(description = "状态，参见 SexEnum 枚举类", example = "1")
    @DiffLogField(name = "状态", function = SexParseFunction.NAME)
    private Integer status;
    @Schema(description = "角色/项目" )
    private List<Long> roleList= new ArrayList<>();
    @Schema(description = "部门", example = "1024")
    private Long deptId;
    @Schema(description = "是否是负责人", example = "1024")
    private Integer isProject;
}
