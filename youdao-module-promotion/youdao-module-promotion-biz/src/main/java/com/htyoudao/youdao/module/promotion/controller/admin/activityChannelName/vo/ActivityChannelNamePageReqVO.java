package com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 渠道名称分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ActivityChannelNamePageReqVO extends PageParam {

    @Schema(description = "渠道名称（模糊匹配）", example = "默认渠道")
    private String name;

    @Schema(description = "是否启用 1是 0否", example = "1")
    private Integer isEnable;


}

