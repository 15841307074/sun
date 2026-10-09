package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.WxPointsLogDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WxPointsLogAndAllPointsDTO {


    @Schema(description = "会员id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "会员昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberNickName;

    @Schema(description = "用户名（登录名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;

    @Schema(description = "总积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long allPoints;

    @Schema(description = "冻结积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long freezePoints;

    @Schema(description = "即将过期的积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long overduePoints;

    @Schema(description = "积分记录", requiredMode = Schema.RequiredMode.REQUIRED)
    List<WxPointsLogDTO> wxPointsLogDTOList =new ArrayList<>();


}
