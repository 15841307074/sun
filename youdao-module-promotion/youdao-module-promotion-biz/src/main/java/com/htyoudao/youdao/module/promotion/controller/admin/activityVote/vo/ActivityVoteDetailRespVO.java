package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityVoteDetailRespVO {

    @Schema(description = "子表id")
    private Long id;

    @Schema(description = "活动id")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动类型")
    private Integer activityType;

    @Schema(description = "是否启用")
    private Integer isEnabled;

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
    private Integer activityStore;

    @Schema(description = "门店id集合")
    private List<Long> storeIds;

    @Schema(description = "门店信息集合")
    private List<StoreSimpleResDto> storeInfoDTOS;

    @Schema(description = "投票选项列表")
    private List<OptionDetail> optionList;

    @Schema(description = "奖励列表")
    private List<RewardDetail> rewardList;

    @Data
    public static class OptionDetail {
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
    }

    @Data
    public static class RewardDetail {
        @Schema(description = "奖励id")
        private Long id;
        @Schema(description = "奖品类型")
        private Integer prizeType;
        @Schema(description = "奖品id")
        private Long prizeId;
        @Schema(description = "优惠券/包名称")
        private String couponName;
        @Schema(description = "奖品名称")
        private String prizeName;
        @Schema(description = "奖品图片")
        private String prizeImgUrl;
        @Schema(description = "奖品价值")
        private BigDecimal prizeValue;
        @Schema(description = "总库存")
        private Integer totalNum;
        @Schema(description = "已发库存")
        private Integer usedNum;
    }
}
