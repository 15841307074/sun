package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCqLogRespVO {

    /**
     * 记录id
     */

    @Schema(description = "记录id")
    private Long id;

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    private Long activityId;

    /**
     * 签码
     */

    @Schema(description = "签码")
    private String signCode;

    /**
     * 会员id
     */

    @Schema(description = "会员id")
    private Long memberId;

    /**
     * 会员昵称
     */

    @Schema(description = "会员昵称")
    private String memberName;

    /**
     * 联系方式
     */

    @Schema(description = "联系方式")
    private String memberMobile;

    /**
     * 性别：0、保密；1、男；2、女
     */
    @Schema(description = "性别：0、保密；1、男；2、女")
    private Integer gender;

    /**
     * 用户类别 0 微信 1支付宝
     */
    @Schema(description = "用户类别 0 微信 1支付宝")
    private Integer memberCategory;

    /**
     * 获取方式
     */

    @Schema(description = "获取方式")
    private Integer obtainType;

    /**
     * 获取方式名称
     */

    @Schema(description = "获取方式名称")
    private String obtainTypeName;

    /**
     * 抽签门店id
     */

    @Schema(description = "抽签门店id")
    private Long storeId;

    /**
     * 抽签门店
     */

    @Schema(description = "抽签门店")
    private String storeName;

    /**
     * 抽签时间
     */

    @Schema(description = "抽签时间")
    private LocalDateTime drawTime;

    /**
     * 结果状态
     */

    @Schema(description = "结果状态")
    private Integer resultStatus;

    /**
     * 结果状态名称
     */

    @Schema(description = "结果状态名称")
    private String resultStatusName;

    /**
     * 奖品id
     */

    @Schema(description = "奖品id")
    private Long prizeId;

    /**
     * 奖品类型
     */

    @Schema(description = "奖品类型")
    private Integer prizeType;

    /**
     * 奖品类型名称
     */

    @Schema(description = "奖品类型名称")
    private String prizeTypeName;

    /**
     * 奖品内容
     */

    @Schema(description = "奖品内容")
    private String prizeContent;

    /**
     * 奖品图片
     */

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    /**
     * 红包状态
     */

    @Schema(description = "红包状态")
    private Integer redPacketStatus;

    /**
     * 红包状态名称
     */

    @Schema(description = "红包状态名称")
    private String redPacketStatusName;

    /**
     * 奖品状态
     */

    @Schema(description = "奖品状态")
    private Integer prizeState;

    /**
     * 收件人
     */

    @Schema(description = "收件人")
    private String receiveUser;

    /**
     * 收件联系方式
     */

    @Schema(description = "收件联系方式")
    private String receiveMobile;

    /**
     * 收件地址
     */

    @Schema(description = "收件地址")
    private String receiveAddress;

    /**
     * 快递单号
     */

    @Schema(description = "快递单号")
    private String trackingNumber;

    /**
     * 快递公司
     */

    @Schema(description = "快递公司")
    private String expressCompany;
}
