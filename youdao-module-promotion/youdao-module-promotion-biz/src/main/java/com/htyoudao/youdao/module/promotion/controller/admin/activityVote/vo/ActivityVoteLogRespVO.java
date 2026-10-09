package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVoteLogRespVO {

    @Schema(description = "记录id")
    private Long id;

    @Schema(description = "选项id")
    private Long optionId;

    @Schema(description = "选项名称")
    private String optionName;

    @Schema(description = "会员id")
    private Long memberId;

    @Schema(description = "会员手机号")
    private Long memberMobile;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "会员头像")
    private String memberAvatar;

    @Schema(description = "门店id")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "投票时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime voteTime;

    @Schema(description = "累计投票次数")
    private Integer totalVoteCount;
}
