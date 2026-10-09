package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.framework.excel.core.annotations.DictFormat;
import com.htyoudao.youdao.framework.excel.core.convert.DictConvert;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.htyoudao.youdao.module.system.enums.DictTypeConstants;
import com.htyoudao.youdao.module.system.framework.operatelog.core.SexParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 用户信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserSimpleVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("用户编号")
    private Long id;
    @Schema(description = "显示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @ExcelProperty("显示名")
    private String nickname;
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @ExcelProperty("用户名称")
    private String username;
    @Schema(description = "用户电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @ExcelProperty("用户电话")
    private String userMobile;
    @Schema(description = "所属组织", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @ExcelProperty("所属组织")
    private String orgNames;
    @Schema(description = "部门", example = "1024")
    private Long deptId;
    @Schema(description = "部门名称", example = "1024")
    private String deptName;
    

}
