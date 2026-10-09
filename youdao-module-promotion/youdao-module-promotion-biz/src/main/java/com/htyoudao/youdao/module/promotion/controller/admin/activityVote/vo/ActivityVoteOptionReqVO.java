package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityVoteOptionReqVO {

    @Schema(description = "选项id（修改时传）")
    private Long id;

    @Schema(description = "投票选项名称")
    @NotBlank(message = "选项名称不能为空")
    private String optionName;

    @Schema(description = "投票选项主图")
    private String optionUrl;

    @Schema(description = "投票选项详情图")
    private String optionDetailUrl;

    @Schema(description = "详情")
    private String voteDetail;
}
