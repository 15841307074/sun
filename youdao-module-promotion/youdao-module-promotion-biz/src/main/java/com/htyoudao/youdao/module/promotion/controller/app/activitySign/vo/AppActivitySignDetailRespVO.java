package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "小程序 - 签到活动详情 Response VO")
@Data
public class AppActivitySignDetailRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动状态：0未开始 1进行中 2已结束 3已停用")
    private Integer activityStatus;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动页背景图")
    private String activityBackgroundImage;

    @Schema(description = "未签到图")
    private String unsignedImage;

    @Schema(description = "已签到图")
    private String signedImage;

    @Schema(description = "活动详情长图")
    private String activityDetailImage;

    @Schema(description = "活动规则，富文本")
    private String activityRule;

    @Schema(description = "背景/按钮色，如#FF5A3C；活动规则、待完成、领取奖励、填写地址等字体颜色按后台配置颜色展示")
    private String themeColor;

    @Schema(description = "分享类型：1复制链接给好友 2生成海报 3复制链接给好友和生成海报")
    private Integer shareType;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享描述")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

//    @Schema(description = "今日是否已签到；详情页不返回，前端统一通过 /promotion/app/activity-sign/sign-record 判断")
//    private Boolean todaySigned;

    @Schema(description = "当前周期连续签到天数")
    private Integer continuousDays;

    @Schema(description = "当前周期累计签到天数")
    private Integer totalDays;

    @Schema(description = "距离下一个奖励还差几天；用于副标题‘还需签到xx天’；没有下一个奖励时为空")
    private Integer nextRewardNeedDays;

    @Schema(description = "是否已获得当前周期全部奖励；true时副标题展示恭喜获得全部奖励")
    private Boolean rewardFinished;

    @Schema(description = "当前自然周日期列表，最多7天，不支持左右切换；签到记录弹窗另走sign-record接口")
    private List<AppActivitySignWeekDateRespVO> weekDateList;

    @Schema(description = "奖励列表")
    private List<AppActivitySignPrizeRespVO> prizeList;
}
