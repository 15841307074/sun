package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "抽签活动请求参数")
public class ActivityCqReqVO extends PageParam {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "用户ID，不传时后端从 token 获取")
    private Long memberId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "邀请人用户ID")
    private Long inviterMemberId;
}
