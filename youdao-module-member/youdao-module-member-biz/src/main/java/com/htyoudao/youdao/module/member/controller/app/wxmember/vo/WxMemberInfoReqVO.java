package com.htyoudao.youdao.module.member.controller.app.wxmember.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "app - 用户信息出参 Response VO")
@Data
public class WxMemberInfoReqVO {

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private Long businessId;

    @Schema(description = "memberId")
    private Long memberId;

    @Schema(description = "别传,后端用")
    private Integer shardingValue;

    @Schema(description = "微信用户标识")
    private String openid;
}
