package com.htyoudao.youdao.module.system.controller.admin.business.vo;



import com.htyoudao.youdao.framework.common.validation.Mobile;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 项目新增/修改 Request VO")
@Data
public class BusinessSaveReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7398")
    private Long id;

    private Long oldId;

    @Schema(description = "项目编码;hk,ts", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "项目名称不能为空")
    @DiffLogField(name = "项目名称")
    private String name;

    @Schema(description = "经营方式 0 自营 1 合作", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "经营方式不能为空")
    @DiffLogField(name = "经营方式")
    private Integer manageType;

    @Schema(description = "状态;0-启用 1-未启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态;0-启用 1-未启用")
    @DiffLogField(name = "状态")
    private Integer status;

    @Schema(description = "有效期开始时间")
    private LocalDateTime validityStartTime;

    @Schema(description = "有效期结束时间")
    private LocalDateTime validityEndTime;

    @Schema(description = "项目logo", example = "https://www.iocoder.cn")
    @NotEmpty(message = "logo不能为空")
    @DiffLogField(name = "项目logo")
    private String logoUrl;

    @Schema(description = "点餐登录页背景")
    @DiffLogField(name = "点餐登录页背景")
    private String dcUrl;

    @Schema(description = "描述")
    @DiffLogField(name = "描述")
    private String comment;

    @NotEmpty
    @Schema(description = "菜单编号列表", example = "1,3,5")
    @DiffLogField(name = "菜单列表")
    private Set<Long> menuIds = Collections.emptySet(); // 兜底


    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @NotBlank(message = "用户账号不能为空")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String nickname;

    @Schema(description = "手机号码", example = "15601691300")
    @Mobile
    private String mobile;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "部门id", example = "123456")
    private Long deptId;

}