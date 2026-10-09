package com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 群成员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GroupMemberRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14925")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "群id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24849")
    @ExcelProperty("群id")
    private Long groupId;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22811")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "用户昵称", example = "李四")
    @ExcelProperty("用户昵称")
    private String userNickName;

    @Schema(description = "显示昵称备注", example = "李四")
    @ExcelProperty("显示昵称备注")
    private String remarkNickName;

    @Schema(description = "用户头像")
    @ExcelProperty("用户头像")
    private String headImage;

    @Schema(description = "显示群名备注", example = "0090")
    @ExcelProperty("显示群名备注")
    private String remarkGroupName;

    @Schema(description = "免打扰标识(do not disturb)  0:关闭   1:开启")
    @ExcelProperty("免打扰标识(do not disturb)  0:关闭   1:开启")
    private Boolean isDnd;

    @Schema(description = "是否已退出")
    @ExcelProperty("是否已退出")
    private Boolean quit;

    @Schema(description = "退出时间")
    @ExcelProperty("退出时间")
    private LocalDateTime quitTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}