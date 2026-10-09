package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "中奖公示记录")
public class WinningRecordVO {

    @Schema(description = "用户手机号")
    private String memberMobile;

    @Schema(description = "奖品内容")
    private String prizeContent;

    @Schema(description = "签码")
    private String signCode;
}
