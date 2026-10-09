package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 签到活动创建/修改 Request VO")
@Data
public class ActivitySignSaveReqVO {

    @Schema(description = "活动ID；创建不传，修改必传", example = "10086")
    private Long id;

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "6月签到有礼")
    @NotEmpty(message = "活动名称不能为空")
    private String activityName;

    @Schema(description = "活动开始时间；指定日期活动必填，长期有效可不填", example = "2026-06-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间；指定日期活动必填，长期有效可不填", example = "2026-06-30 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    @Schema(description = "活动日期类型：1指定日期 2长期有效", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动日期类型不能为空")
    private Integer dateType;

    @Schema(description = "是否启用：0停用 1启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "是否启用不能为空")
    private Integer enabled;

    @Schema(description = "活动门店类型：1全部门店 2部分门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "活动门店类型不能为空")
    private Integer activityStoreType;

    @Schema(description = "门店ID集合；activityStoreType=2时必传")
    private List<Long> storeIds;

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;

    @Schema(description = "未签到图")
    private String unsignedImage;

    @Schema(description = "已签到图")
    private String signedImage;

    @Schema(description = "活动详情长图")
    private String activityDetailImage;

    @Schema(description = "背景/按钮色，如#FF5A3C", example = "#FF5A3C")
    private String themeColor;

    @Schema(description = "活动备注，限制200中文字符")
    private String activityRemark;

    @Schema(description = "分享方式：1不允许转发 2允许转发好友 3允许复制链接", example = "2")
    private Integer shareType;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "是否开启周期及奖励重置：0否 1是", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "是否开启周期及奖励重置不能为空")
    private Integer resetEnabled;

    @Schema(description = "重置类型：1按周 2按月；resetEnabled=1时必传", example = "2")
    private Integer resetType;

    @Schema(description = "重置日期配置：resetType=1时存1~7，1周一...7周日；resetType=2时存1~31，32月末；逗号分隔", example = "1,15,32")
    private String resetDays;

    @Schema(description = "连签周期：1~365整数，用于统计用户满足连签周期的次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "连签周期不能为空")
    @Min(value = 1, message = "连签周期必须大于0")
    @Max(value = 365, message = "连签周期最多365天")
    private Integer continuousCycleDays;

    @Schema(description = "社群专享：0否 1是", example = "0")
    private Integer communityOnly;

    @Schema(description = "店长企微码引导图；社群专享开启时使用")
    private String storeManagerQrCodeGuideImage;

    @Schema(description = "门店群活码引导图；社群专享开启时使用")
    private String storeGroupQrCodeGuideImage;

    @Schema(description = "活动规则，富文本")
    private String activityRule;

    @AssertTrue(message = "指定日期活动开始时间和结束时间不能为空")
    public boolean isEndTimeValid() {
        // 如果活动日期类型是指定日期，开始时间和结束时间都必须传，长期有效则允许都不传。
        return !Integer.valueOf(1).equals(dateType) || (startTime != null && endTime != null);
    }

    @AssertTrue(message = "活动结束时间不能早于活动开始时间")
    public boolean getActivityTimeRangeValid() {
        // 如果开始时间或结束时间缺失，交给对应的必填校验处理；这里仅校验两个时间都有值时的先后顺序。
        return startTime == null || endTime == null || !endTime.isBefore(startTime);
    }

    @AssertTrue(message = "部分门店时门店ID集合不能为空")
    public boolean isStoreIdsValid() {
        // 如果活动适用范围选择部分门店，必须传门店ID集合，否则活动无法命中具体门店。
        return !Integer.valueOf(2).equals(activityStoreType) || (storeIds != null && !storeIds.isEmpty());
    }

    @AssertTrue(message = "开启重置时重置类型不能为空")
    public boolean isResetTypeValid() {
        // 如果开启周期及奖励重置，必须选择按周或按月重置。
        return !Integer.valueOf(1).equals(resetEnabled) || resetType != null;
    }

    @AssertTrue(message = "开启重置时重置日期不能为空")
    public boolean isResetDaysValid() {
        // 如果开启周期及奖励重置，必须传具体重置日期配置，例如按月月末传32。
        return !Integer.valueOf(1).equals(resetEnabled) || (resetDays != null && !resetDays.trim().isEmpty());
    }

    @Valid
    @NotEmpty(message = "奖励列表不能为空")
    @Schema(description = "奖励列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ActivitySignPrizeReqVO> prizeList;
}
