package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dht
 */
@Schema(description = "管理后台 - pc端查询小程序用户入参 Request VO")
@Data
public class WxMemberReqVO extends PageParam {

    @Schema(description = "memberId", example = "小红")
    private Long memberId;

    private String openid;

    @Schema(description = "电话号码")
    private String memberMobile;

    /**
     * 性别：0、保密；1、男；2、女
     */
    @Schema(description = "性别 1男 2女 0保密")
    private Integer gender;

    @Schema(description = "店铺id/组织id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    @Schema(description = "attribute")
    private Integer attribute;

    @Schema(description = "会员状态 0正常 1冻结 2待注销 3已注销")
    private Integer memberStatus;

    @Schema(description = "会员类型 1会员 2非会员")
    private Integer memberType;

    @Schema(description = "会员来源 0微信 1支付宝")
    private Integer memberCategory;

    /**
     * 会员等级
     */
    @Schema(description = "会员等级 12345")
    private Integer grade;

    @Schema(description = "注册开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerStartTime;

    @Schema(description = "注册结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerEndTime;

    @Schema(description = "最后登录开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginStartTime;

    @Schema(description = "最后登录结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginEndTime;

    @Schema(description = "最后下单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastOrderStartTime;

    @Schema(description = "最后下单结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastOrderEndTime;

    @Schema(description = "会员标签 对应原型1234567")
    private Integer memberLabel;

    @Schema(description = "下单间隔 0: 0-10天 1:11-20天  2：21-30天")
    private Integer orderInterval;


    @Schema(description = "下单频次 2：一单 1：2-3单 0：大于3单")
    private Integer orderFrequency;

    @Schema(description = "下单频次的时间 0:30天内 1:7天内 2：180天内 3：365天内")
    private Integer orderFrequencyTime;

    @Schema(description = "单均实付 0：0-8元 1：8-11元 2：11-14元 3：14-17元 4:17元+")
    private Integer orderAvg;

    @Schema(description = "下单频次的时间 0:30天内 1:7天内 2：180天内 3：365天内")
    private Integer orderAvgTime;

    @Schema(description = "0 是组织 1是门店")
    private Integer isStore;

    @Schema(description = "优惠券id")
    private Long couponId;

    @Schema(description = "优惠券发放数量")
    private Integer sendNum;

    @Schema(description = "人群id")
    private Long crowdId;

    @Schema(description = "标签id")
    private Long tagId;

    @Schema(description = "活跃度 012345")
    private Integer liveness;

    @Schema(description = "2在社群 1不在社群", example = "0")
    private Integer communityFlag;

    /**
     * 是否是配送员 0不是 1是
     */
    @Schema(description = "是否是配送员 0不是 1是", example = "0")
    private Integer errandFlag;
}
