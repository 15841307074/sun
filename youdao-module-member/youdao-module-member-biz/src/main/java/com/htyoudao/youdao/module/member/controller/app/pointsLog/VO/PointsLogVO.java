package com.htyoudao.youdao.module.member.controller.app.pointsLog.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PointsLogVO {



    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "兑换记录ID，不传则查询全部兑换记录")
    private Long pointsLogId;

    @Schema(description = "兑换记录编号，不传则查询全部兑换记录")
    private String logCode;





}
