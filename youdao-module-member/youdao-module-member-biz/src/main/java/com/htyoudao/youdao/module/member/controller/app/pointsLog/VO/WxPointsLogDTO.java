package com.htyoudao.youdao.module.member.controller.app.pointsLog.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class WxPointsLogDTO {
    //会员 id
    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId;
    @Schema(description = "日志编码", requiredMode = Schema.RequiredMode.REQUIRED)
    String logCode;
    //积分变动时间
    @Schema(description = "积分变动时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    Date pointLogCreateTime;
    //积分变动价格
    @Schema(description = "积分变动价格", requiredMode = Schema.RequiredMode.REQUIRED)
    Long pointPrice;
    //积分变动原因
    @Schema(description = "积分变动原因", requiredMode = Schema.RequiredMode.REQUIRED)
    String pointChangeValue;
    //订单编号
    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    String orderSn;
    //积分记录 id
    @Schema(description = "积分记录 id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long pointLogId;
}
