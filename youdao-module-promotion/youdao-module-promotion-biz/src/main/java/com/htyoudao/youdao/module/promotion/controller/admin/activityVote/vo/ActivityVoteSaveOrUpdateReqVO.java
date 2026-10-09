package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityVoteSaveOrUpdateReqVO {

    @Schema(description = "子表id")
    private Long id;

    @Schema(description = "活动id")
    private Long activityId;

    @Schema(description = "活动类型")
    private Integer activityType;

    @Schema(description = "活动名称")
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @Schema(description = "是否启用")
    private Integer isEnabled;

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
    @NotNull(message = "投票次数限制不能为空")
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

    @Schema(description = "分享类型 1不允许转发 2允许转发好友 3允许复制链接")
    private Integer shareType;

    @Schema(description = "公共展示 0开启 1关闭")
    private Integer publicButton;

    @Schema(description = "社群专享 1不开启 2开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @Schema(description = "活动备注")
    private String activityRemark;

    @Schema(description = "活动规则")
    private String activityRules;

    @Schema(description = "活动门店 0指定门店 1全部门店")
    @NotNull(message = "活动门店不能为空")
    private Integer activityStore;

    @Schema(description = "门店id集合")
    private List<Long> storeIds;

    @Schema(description = "投票选项")
    @NotEmpty(message = "投票选项不能为空")
    private List<ActivityVoteOptionReqVO> optionList;

    @Schema(description = "奖励设置")
    @Valid
    private List<ActivityVoteRewardReqVO> rewardList;
}
