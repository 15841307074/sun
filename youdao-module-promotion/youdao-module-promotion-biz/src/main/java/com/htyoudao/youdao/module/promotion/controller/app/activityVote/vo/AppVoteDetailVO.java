package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppVoteDetailVO {

    @Schema(description = "活动id")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动状态 1未开始 2进行中 3已结束")
    private Integer activityStatus;

    @Schema(description = "活动封面图")
    private String activityImgUrl;

    @Schema(description = "活动背景图")
    private String activityBackground;

    @Schema(description = "投票背景图")
    private String voteBackground;

    @Schema(description = "排行榜背景图")
    private String rankingBackground;

    @Schema(description = "背景色")
    private String backgroundColor;

    @Schema(description = "投票次数限制 0不限制 1每天可投票 2最多可投票")
    private Integer voteCountFlag;

    @Schema(description = "投票次数")
    private Integer voteCount;

    @Schema(description = "投票按钮文案")
    private String voteBtnTitle;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "分享类型")
    private Integer shareType;

    @Schema(description = "公共展示 0开启 1关闭")
    private Integer publicButton;

    @Schema(description = "社群专享 1不开启 2开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "活动规则")
    private String activityRules;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @Schema(description = "当前用户剩余投票次数（-1表示不限）")
    private Integer remainVoteCount;

    @Schema(description = "当前用户是否已投过票")
    private Boolean hasVoted;

    @Schema(description = "投票选项列表")
    private List<OptionVO> optionList;

    @Data
    public static class OptionVO {
        @Schema(description = "选项id")
        private Long id;
        @Schema(description = "选项名称")
        private String optionName;
        @Schema(description = "选项主图")
        private String optionUrl;
        @Schema(description = "选项详情图")
        private String optionDetailUrl;
        @Schema(description = "详情")
        private String voteDetail;
        @Schema(description = "票数")
        private Long voteNum;
        @Schema(description = "票数占比（百分比）")
        private String votePercent;
    }
}
