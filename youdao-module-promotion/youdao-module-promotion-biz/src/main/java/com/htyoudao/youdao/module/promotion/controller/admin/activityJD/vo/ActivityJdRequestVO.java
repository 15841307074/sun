package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 聚合请求参数对象 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityJdRequestVO {


    @Schema(description = "活动ID")
    private Long activityId;

}
