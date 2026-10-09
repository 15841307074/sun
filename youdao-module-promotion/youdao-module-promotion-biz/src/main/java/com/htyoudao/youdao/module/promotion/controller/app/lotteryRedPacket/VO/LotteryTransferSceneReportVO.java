package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryTransferSceneReportVO {


    @Schema(name = "infoType", description = "信息类型")
    private String infoType;

    @Schema(name = "infoContent", description = "信息内容")
    private String infoContent;
}
