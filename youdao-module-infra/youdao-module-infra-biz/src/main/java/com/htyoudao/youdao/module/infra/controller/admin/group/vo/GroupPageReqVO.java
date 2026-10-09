package com.htyoudao.youdao.module.infra.controller.admin.group.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 群分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupPageReqVO extends PageParam {

    @Schema(description = "群名字", example = "李四")
    private String name;

    @Schema(description = "群主id", example = "4715")
    private Long ownerId;

    @Schema(description = "群头像")
    private String headImage;

    @Schema(description = "群头像缩略图")
    private String headImageThumb;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "是否被封禁 0:否 1:是")
    private Boolean isBanned;

    @Schema(description = "被封禁原因", example = "不好")
    private String reason;

    @Schema(description = "是否已解散")
    private Boolean dissolve;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}