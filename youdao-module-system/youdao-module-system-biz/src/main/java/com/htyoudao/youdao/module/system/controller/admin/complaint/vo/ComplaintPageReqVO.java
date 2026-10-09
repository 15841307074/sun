package com.htyoudao.youdao.module.system.controller.admin.complaint.vo;

import cn.hutool.core.date.DateTime;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - 投诉分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ComplaintPageReqVO extends PageParam {

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "所属组织")
    private Long orgId;
    @Schema(description = "是否是门店 0 否 1 是")
    private Integer isStore;

    private Set<Long> orgIds;

    private Long memberId;

    private Boolean isBoss =Boolean.FALSE;
}