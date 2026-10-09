package com.htyoudao.youdao.module.system.controller.admin.business.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusinessPageReqVO extends PageParam {

    @Schema(description = "项目编码;hk,ts")
    private String code;

    @Schema(description = "项目名称/编号  模糊搜索", example = "李四")
    private String keyword;

    @Schema(description = "项目名称", example = "李四")
    private String name;

    @Schema(description = "经营方式 0 自营 1 合作", example = "1")
    private Integer manageType;

    @Schema(description = "状态;0-未启用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "有效期时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] validityTime;

}