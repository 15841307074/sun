package com.htyoudao.youdao.module.system.controller.admin.org.vo;

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
@Schema(description = "管理后台 - 组织机构分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrgPageReqVO extends PageParam {

    @Schema(description = "组织名称", example = "张三")
    private String name;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "上级ID", example = "19880")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "组织类型", example = "2")
    private Integer type;

    @Schema(description = "组织编号")
    private String code;

    @Schema(description = "状态 0-未启用 1-启用", example = "2")
    private Integer status;

    @Schema(description = "层级编号")
    private Integer level;

    @Schema(description = "业务线ID", example = "27690")
    private Integer businessId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}