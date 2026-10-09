package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.htyoudao.youdao.framework.excel.core.annotations.DictFormat;
import com.htyoudao.youdao.framework.excel.core.convert.DictConvert;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessUserRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.htyoudao.youdao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.system.framework.operatelog.core.SexParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 用户信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserRespVO{

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @ExcelProperty("账号")
    private String username;

    @Schema(description = "显示名", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    @ExcelProperty("显示名")
    private String userNickname;
    @DiffLogField(name = "供应链显示名")
    private String supplyName;
    @Schema(description = "手机号", example = "15601691300")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "用户性别，参见 SexEnum 枚举类", example = "1")
    @DiffLogField(name = "用户性别", function = SexParseFunction.NAME)
    private Integer sex;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    @DiffLogField(name = "用户头像")
    private String avatar;
    @Schema(description = "状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer userStatus;
    @ExcelProperty("帐号状态")
    private String  userStatusStr;
    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("组织名称")
    private String orgName;
    @Schema(description = "对应项目存在角色")
    @ExcelProperty("角色")
    private String  roleName;
    @Schema(description = "对应项目存在角色List")
    private List<RoleSimpleRespVO> roleList = new ArrayList<>();
    @Schema(description = "所属门店数量")
    @ExcelProperty("门店数量")
    private Long storeCount = 0L;
    @Schema(description = "所在项目")
    private String businessNames;
    @Schema(description = "创建时间")
    private LocalDateTime userCreateTime;
    @Schema(description = "对应项目List")
    private List<BusinessSimpleRespVO> businessList = new ArrayList<>();
    @Schema(description = "门店名称")
    private List<String> storeList = new ArrayList<>();
    @Schema(description = "部门", example = "1024")
    private Long deptId;
    @Schema(description = "部门名称", example = "1024")
    @ExcelProperty("部门名称")
    private String deptName;
    @Schema(description = "是否是部门负责人")
    private Integer deptUserType;
    @Schema(description = "部门关系id", example = "1024")
    private Long userDeptId;
    @Schema(description = "是否是项目负责人", example = "1024")
    private Integer isProject;
    // 添加格式化方法
    public String getUserCreateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return userCreateTime != null ? userCreateTime.format(formatter) : null;
    }
}
