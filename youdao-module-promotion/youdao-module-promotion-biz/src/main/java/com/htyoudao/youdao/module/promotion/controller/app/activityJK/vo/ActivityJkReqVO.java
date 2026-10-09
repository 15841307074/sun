package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动请求参数
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "活动请求参数")
public class ActivityJkReqVO extends PageParam {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "用户ID")
    private Long memberId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "邀请人用户ID")
    private Long inviterMemberId;
}

