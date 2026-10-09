package com.htyoudao.youdao.module.system.controller.admin.user.vo.user;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPageReqVO extends PageParam {
    @Schema(description = "手机号码/用户昵称/用户账号，模糊匹配", example = "youdao")
    private String text;
    @Schema(description = "展示状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer status;
    @Schema(description = "角色", example = "1024")
    private List<Long> roleList = new ArrayList<>();
    @Schema(description = "组织，同时筛选子部门", example = "1024")
    private Set<Long> orgList = new HashSet<>();
    @Schema(description = "部门，同时筛选子部门", example = "1024")
    private Set<Long> deptList = new HashSet<>();
    @Schema(description = "项目id")
    private Long businessId;

}
