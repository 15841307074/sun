package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 签到活动详情 Response VO")
@Data
public class ActivitySignRespVO {

    @Schema(description = "活动ID", example = "10086")
    private Long id;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动日期类型：1指定日期 2长期有效")
    private Integer dateType;

    @Schema(description = "是否启用：0停用 1启用")
    private Integer enabled;

    @Schema(description = "活动门店类型：1全部门店 2部分门店")
    private Integer activityStoreType;

    @Schema(description = "门店ID集合")
    private List<Long> storeIds;

    @Schema(description = "门店ID+门店名称列表；activityStoreType=2部分门店时返回")
    private List<ActivitySignStoreRespVO> storeList;

    @Schema(description = "活动封面图/推广展示图")
    private String activityCoverImage;

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;

    @Schema(description = "未签到图")
    private String unsignedImage;

    @Schema(description = "已签到图")
    private String signedImage;

    @Schema(description = "活动详情长图")
    private String activityDetailImage;

    @Schema(description = "分享方式：1不允许转发 2允许转发好友 3允许复制链接")
    private Integer shareType;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容/分享描述")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "活动规则，富文本")
    private String activityRule;

    @Schema(description = "背景/按钮色")
    private String themeColor;

    @Schema(description = "活动备注")
    private String activityRemark;

    @Schema(description = "是否开启周期及奖励重置：0否 1是")
    private Integer resetEnabled;

    @Schema(description = "重置类型：1按周 2按月")
    private Integer resetType;

    @Schema(description = "重置日期配置")
    private String resetDays;

    @Schema(description = "连签周期：1~365整数，用于统计用户满足连签周期的次数")
    private Integer continuousCycleDays;

    @Schema(description = "社群专享：0否 1是")
    private Integer communityOnly;

    @Schema(description = "店长企微码引导图")
    private String storeManagerQrCodeGuideImage;

    @Schema(description = "门店群活码引导图")
    private String storeGroupQrCodeGuideImage;

    @Schema(description = "奖励列表")
    private List<ActivitySignPrizeReqVO> prizeList;
}
