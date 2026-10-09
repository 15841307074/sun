package com.htyoudao.youdao.module.promotion.api.activity.VO;

//import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 营销集点活动创建 Request VO")
@Data
public class ActivityJDRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "活动名称")
    private String activityName;

    // 图片地址
    @Schema(description = "活动背景图片地址")
    private String bgImageUrl;

    // 图片地址
    @Schema(description = "待集点图片地址")
    private String collectPointsImageUrl;

    // 图片地址
    @Schema(description = "集点图片地址")
    private String collectedPointsImageUrl;

    /**
     * 背景色（如#FFFFFF）
     */
    @Schema(description = "背景/按钮色（如#FFFFFF）")
    private String backgroundColor;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期）")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
//    @JSONField(format = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
//    @JSONField(format = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 活动备注（最多200字）
     */
    @Schema(description = "活动备注")
    private String activityRemark;

    /**
     * 集点模式
     */
    @Schema(description = "集点模式 （1 按订单集点，2按商品集点）")
    private Integer collectPointsType;

    /**
     * 集点门槛
     */
    @Schema(description = "集点门槛")
    private BigDecimal collectPointsThreshold;

    /**
     * 集点商品范围
     */
    @Schema(description = "集点商品范围 （1全部商品，2部分商品）")
    private Integer collectPointsCommodityType;

    @Schema(description = "部分商品")
    private List<ActivityJDCommodityRespVO>  activityJDCommodityRespList = new ArrayList<>();

    /**
     * 用户任务有效期
     */
    @Schema(description = "用户任务有效期(0 不限制  其余数字为固定天数)")
    private Integer validityPeriod;

    /**
     * 奖励发放
     */
    @Schema(description = "奖励发放(1 当日  2 次日)")
    private Integer distributeMode;


    /**
     * 活动门店（1全部门店 2部分门店）
     */
    @Schema(description = "活动门店（1全部门店 2部分门店）")
    private Integer activityStore = 0;

    /**
     * 是否开启（0不开启 1开启）
     */
    private Integer isEnabled = 0;

    // 分享图片
    @Schema(description = "分享图片")
    private String shareImageUrl;

    // 分享标题
    @Schema(description = "分享标题")
    private String shareTitle;

    // 分享描述
    @Schema(description = "分享描述")
    private String shareDescription;

    // 分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）
    @Schema(description = "分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）")
    private Integer shareSetting;

    @Schema(description = "集点推广集合")
    List<ActivityChannelRespVO>  activityChannelRespVOList = new ArrayList<>();

    /**
     * 活动规则（富文本内容）
     */
    @Schema(description = "活动规则（富文本内容）")
    private String activityRules;

    @Schema(description = "社群专享 1 不开启  2 开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

}
