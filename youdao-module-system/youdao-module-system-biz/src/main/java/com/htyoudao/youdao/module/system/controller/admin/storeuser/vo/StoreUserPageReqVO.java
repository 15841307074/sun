package com.htyoudao.youdao.module.system.controller.admin.storeuser.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门店和用户关联分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoreUserPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "26974")
    private Long userId;

    @Schema(description = "门店ID", example = "6813")
    private Long storeId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "类型 1. 店长 ", example = "1")
    private Integer type;

    @Schema(description = "项目 id", example = "24728")
    private Long businessId;

}