package com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 活动的兑换记录新增/修改 Request VO")
@Data
public class ExchangeLogSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5732")
    private Long id;

    @Schema(description = "会员昵称", example = "赵六")
    private String memberNickName;

    @Schema(description = "联系电话")
    private String memberMobile;

    @Schema(description = "活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "28156")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "奖品类型 0 优惠券 1券包", example = "1")
    private Integer awardType;

    @Schema(description = "奖品名称", example = "张三")
    private String awardName;

    @Schema(description = "奖品图片")
    private String awardPic;

    @Schema(description = "项目标识", example = "17256")
    private Long businessId;

}