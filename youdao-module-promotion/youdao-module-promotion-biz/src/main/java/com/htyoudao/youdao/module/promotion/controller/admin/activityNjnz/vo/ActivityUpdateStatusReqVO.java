package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author villky
 */
@Schema(description = "管理后台 - 营销活动修改状态 Request VO")
@Data
public class ActivityUpdateStatusReqVO {
    @Schema(description = "id")
    @NotNull
    private Long id;


    @Schema(description = "上下架状态是否上架(0不开启 1开启)")
    @NotNull(message = "状态不能为空")
    private Integer isEnabled;



}
