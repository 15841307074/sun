package com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 群成员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberPageReqVO extends PageParam {

    @Schema(description = "群id", example = "24849")
    private Long groupId;

    @Schema(description = "用户id", example = "22811")
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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] quitTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}