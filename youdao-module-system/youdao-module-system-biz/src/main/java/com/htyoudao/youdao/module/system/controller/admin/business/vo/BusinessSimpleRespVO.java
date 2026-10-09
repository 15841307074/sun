package com.htyoudao.youdao.module.system.controller.admin.business.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessSimpleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7398")
    @ExcelProperty("ID")
    private Integer id;
    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("项目名称")
    private String name;
    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("组织名称")
    private String orgName;
    @Schema(description = "对应项目存在角色")
    private List<RoleSimpleRespVO> roleList = new ArrayList<>();
    @Schema(description = "所属门店数量")
    private Long storeCount;
    @Schema(description = "所属门店名称")
    private List<String> storeList = new ArrayList<>();
    @Schema(description = "创建时间")
    private LocalDateTime userCreateTime;
    @Schema(description = "部门", example = "1024")
    private Long deptId;
    @Schema(description = "部门名称", example = "1024")
    @ExcelProperty("部门名称")
    private String deptName;
    @Schema(description = "是否是部门负责人")
    private Integer deptUserType;
    @Schema(description = "部门关系id", example = "1024")
    private Long userDeptId;
    // 添加格式化方法
    public String getUserCreateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return userCreateTime != null ? userCreateTime.format(formatter) : null;
    }
}