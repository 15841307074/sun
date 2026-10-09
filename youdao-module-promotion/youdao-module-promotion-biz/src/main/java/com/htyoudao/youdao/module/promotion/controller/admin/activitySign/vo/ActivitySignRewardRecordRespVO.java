package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 发放记录 Response VO")
@Data
public class ActivitySignRewardRecordRespVO {

    @Schema(description = "发放记录ID；用于填写/修改快递单号")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "奖励配置ID")
    private Long prizeId;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "联系方式")
    private String memberMobile;

    @Schema(description = "发放时间")
    private LocalDateTime issueTime;

    @Schema(description = "奖品内容")
    private String prizeContent;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品类型，复用ActivityCqPrizeTypeEnum：1优惠券 2积分 3实物 5现金红包 6优惠券包")
    private Integer prizeType;

    @Schema(description = "收件人；非实物奖品为空")
    private String receiveUser;

    @Schema(description = "收件联系方式；非实物奖品为空")
    private String receiveMobile;

    @Schema(description = "收件地址；非实物奖品为空")
    private String receiveAddress;

    @Schema(description = "快递单号；非实物奖品为空，实物奖品暂未填写时为空")
    private String trackingNumber;

    @Schema(description = "快递公司；填写/修改快递单号时使用")
    private String expressCompany;

    @Schema(description = "红包状态：1未领取 2已领取 3已失效；非现金红包为空，页面展示-；已失效指现金红包超过24h未被领取")
    private Integer claimStatus;
}
