package com.htyoudao.youdao.module.system.controller.admin.orguser.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 组织和用户关联分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrgUserPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "21184")
    private Long userId;

    @Schema(description = "组织ID", example = "25486")
    private Long orgId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "类型 1. 负责人 0.普通", example = "1")
    private Integer type;

    @Schema(description = "项目 id", example = "6285")
    private Long businessId;

}