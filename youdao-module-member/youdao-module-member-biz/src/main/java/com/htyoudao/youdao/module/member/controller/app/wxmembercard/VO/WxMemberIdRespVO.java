package com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WxMemberIdRespVO {
    /** 会员卡ID */
    @Schema(description = "会员卡ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberCardId;

    /** 会员卡背景图 */
    @Schema(description = "会员卡背景图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String backgroundImage;

    @Schema(description = "详情图片", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ImageRespVO> detailImageList = new ArrayList<>();
}