package com.htyoudao.youdao.module.member.controller.app.wxmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 保存用户订阅的消息入参 Response VO")
public class NoticeReserveMemberReqVO {

    private Long id;

    @Schema(description = "memberId")
    private Long memberId;

    @Schema(description = "模板类型")
    private String reserverTemplateType;

    @Schema(description = "openid")
    private String openid;

    @Schema(description = "不用传")
    private String projectOwnerShip;

    @Schema(description = "businessId")
    private String businessId;
}
