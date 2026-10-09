package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;


import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Yangqinglin
 */
@Schema(description = "管理后台 - 营销活动 集卡活动新增/修改 Request VO")
@Data
public class ActivityJkStateReqVO {


    @Schema(description = "id")
    @NotNull
    private Long id;


    @Schema(description = "上下架状态是否上架(0不开启 1开启)")
    @NotNull(message = "状态不能为空")
    private Integer isEnabled;


}
