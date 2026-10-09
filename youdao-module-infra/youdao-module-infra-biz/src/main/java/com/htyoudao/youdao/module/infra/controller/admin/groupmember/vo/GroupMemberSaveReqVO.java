package com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 群成员新增/修改 Request VO")
@Data
public class GroupMemberSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14925")
    private Long id;

    @Schema(description = "群id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24849")
    @NotNull(message = "群id不能为空")
    private Long groupId;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22811")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @Schema(description = "用户昵称", example = "李四")
    private String userNickName;

    @Schema(description = "显示昵称备注", example = "李四")
    private String remarkNickName;

    @Schema(description = "用户头像")
    private String headImage;

    @Schema(description = "显示群名备注", example = "0090")
    private String remarkGroupName;

    @Schema(description = "免打扰标识(do not disturb)  0:关闭   1:开启")
    private Boolean isDnd;

    @Schema(description = "是否已退出")
    private Boolean quit;

    @Schema(description = "退出时间")
    private LocalDateTime quitTime;

}